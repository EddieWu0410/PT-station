package tracker;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.*;
import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;
import com.querydsl.jpa.impl.JPAUpdateClause;
import entity.*;
import entity.config;
import java.util.Scanner;
import java.io.IOException;
public class Tracker implements TrackerInterface {
    private final EntityManagerFactory emf;
    // 默认构造：产线数据库
    public Tracker() {
        config cfg = new config();
        Map<String,Object> props = new HashMap<>();
        props.put("javax.persistence.jdbc.url",
                  "jdbc:mysql://" + cfg.SqlURL + "/" + cfg.Database);
        props.put("javax.persistence.jdbc.user", cfg.SqlUsername);
        props.put("javax.persistence.jdbc.password", cfg.SqlPassword);
        this.emf = Persistence.createEntityManagerFactory("myPersistenceUnit", props);
    }
    // 测试传入：测试库
    public Tracker(EntityManagerFactory emf) {
        this.emf = emf;
    }
    @Override
    public boolean AddUpLoad(String userid, int upload, String infoHash) {
        long newTotal = upload;  // convert to long
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            // 1) find the seedId by infoHash
            System.out.println("DEBUG AddUpLoad: Looking for infoHash: '" + infoHash + "' (length: " + infoHash.length() + ")");
            String seedId = em.createQuery(
                "SELECT s.seedId FROM SeedHash s WHERE s.infoHash = :ih", String.class)
                .setParameter("ih", infoHash)
                .getSingleResult();
            // 2) sum existing uploads for this user+seed
            Long sumSoFar = em.createQuery(
                "SELECT COALESCE(SUM(t.upload),0) FROM TransRecord t WHERE t.uploaduserid = :uid AND t.seedid = :sid",
                Long.class)
                .setParameter("uid", userid)
                .setParameter("sid", seedId)
                .getSingleResult();
            long delta = newTotal - sumSoFar;
            if (delta < 0L) {
                tx.rollback();
                return false;   // error: newTotal less than already recorded
            }
            if (delta == 0L) {
                tx.rollback();
                return false;  // nothing to do
            }
            // 3) persist a new TransRecord with only the delta
            TransRecord rd = new TransRecord();
            rd.taskid       = UUID.randomUUID().toString();
            rd.uploaduserid = userid;
            rd.seedid       = seedId;
            rd.upload       = delta;
            rd.maxupload    = newTotal;
            em.persist(rd);
            em.flush();
            // 4) 重新计算用户的总上传，确保与 TransRecord 完全一致
            Long totalUpload = em.createQuery(
                "SELECT COALESCE(SUM(t.upload),0) FROM TransRecord t WHERE t.uploaduserid = :uid",
                Long.class
            )
            .setParameter("uid", userid)
            .getSingleResult();

            Long PTuploadbefor = em.createQuery(
                "SELECT t.upload FROM UserPT t WHERE t.userid = :uid",
                Long.class
            ).setParameter("uid", userid)
            .getSingleResult();
            
        
            UserPT user = em.find(UserPT.class, userid);
            user.upload = totalUpload;
            em.merge(user);
            em.flush();
            tx.commit();

            Long PTuploadafter = em.createQuery(
                "SELECT t.upload FROM UserPT t WHERE t.userid = :uid",
                Long.class
            ).setParameter("uid", userid)
            .getSingleResult();

            System.out.println("------------------------------------------------");
            System.out.printf("thisadd:%d userptsofar:%d userptafter:%d totaluploadnow:%d delta:%d%n",upload, PTuploadbefor,PTuploadafter, totalUpload,delta);
            System.out.println("------------------------------------------------");
            return false;      // success
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
    
    @Override
    public boolean ReduceUpLoad(String userid, int upload){
        long uploadLong = upload;  // convert to long
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        try {
            // 1) fetch user and ensure enough upload to reduce
            UserPT user = em.find(UserPT.class, userid);
            long before = user.upload;
            if (uploadLong > before) {
                tx.rollback();
                return true;   // error: cannot reduce more than current total
            }
            // 2) subtract
            user.upload = before - uploadLong;
            em.merge(user);
            // (optional) record a negative TransRecord so sums stay in sync
            TransRecord rd = new TransRecord();
            rd.taskid       = UUID.randomUUID().toString();
            rd.uploaduserid = userid;
            rd.seedid       = null;
            rd.upload       = -uploadLong;
            rd.maxupload    = user.upload;
            em.persist(rd);
            tx.commit();
            return false;      // success
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
    @Override
    public boolean AddDownload(String userid, int download, String infoHash) {
        long newTotal = download;  // convert to long
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            // 1. 查 SeedHash
            System.out.println("DEBUG AddDownload: Looking for infoHash: '" + infoHash + "' (length: " + infoHash.length() + ")");
            TypedQuery<SeedHash> qsh = em.createQuery(
                "SELECT s FROM SeedHash s WHERE s.infoHash = :h", SeedHash.class);
            qsh.setParameter("h", infoHash);
            List<SeedHash> shl = qsh.getResultList();
            if (shl.isEmpty()) {
                System.out.println("seed没有被记录");
                return false;
            }
            String seedid = shl.get(0).seedId;

            // 2. 统计该用户在该种子上的已有 download
            TypedQuery<Long> qsum = em.createQuery(
                "SELECT COALESCE(SUM(t.download),0) FROM TransRecord t " +
                "WHERE t.seedid = :sid AND t.downloaduserid = :uid", Long.class);
            qsum.setParameter("sid", seedid);
            qsum.setParameter("uid", userid);
            long oldSeedSum = qsum.getSingleResult();

            long diff = newTotal - oldSeedSum;
            if (diff <= 0) return false;
            
            System.out.println("AddDownload: 该种子原有总量=" + oldSeedSum + ", 新总量=" + newTotal + ", 增量=" + diff);

            try {
                tx.begin();
                // 1. persist 增量记录
                TransRecord tr = new TransRecord();
                tr.taskid         = UUID.randomUUID().toString();
                tr.downloaduserid = userid;
                tr.seedid         = seedid;
                tr.download       = diff;
                tr.maxdownload    = newTotal;
                em.persist(tr);

                // 2. 全表重新累计该用户所有种子的 download，并更新 UserPT.download
                TypedQuery<Long> qTotal = em.createQuery(
                    "SELECT COALESCE(SUM(t.download),0) FROM TransRecord t WHERE t.downloaduserid = :uid",
                    Long.class
                )
                .setParameter("uid", userid);
                long userTotalDownload = qTotal.getSingleResult();
                QUserPT quser = QUserPT.userPT;
                new JPAUpdateClause(em, quser)
                    .where(quser.userid.eq(userid))
                    .set(quser.download, userTotalDownload)
                    .execute();

                tx.commit();
                return false;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                return true;
            } finally {
                em.close();
            }
        } catch (Exception e) {
            return true;
        }
    }
    @Override
    public boolean ReduceDownload(String userid, int download) {
        long downloadLong = download;  // convert to long
        EntityManager em = emf.createEntityManager();
        try {
            // 1. 预检查当前值
            TypedQuery<Long> qcurr = em.createQuery(
                "SELECT u.download FROM UserPT u WHERE u.userid = :uid", Long.class);
            qcurr.setParameter("uid", userid);
            long current = qcurr.getSingleResult();
            if (downloadLong > current) {
                em.close();
                return false;
            }
            // 2. 执行减法更新
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            QUserPT q = QUserPT.userPT;
            new JPAUpdateClause(em, q)
                .where(q.userid.eq(userid))
                .set(q.download, q.download.subtract(downloadLong))
                .execute();
            tx.commit();
            return false;
        } catch(Exception e) {
            return true;
        } finally {
            if (em.isOpen()) em.close();
        }
    }
    @Override
    public boolean AddMagic(String userid, int magic) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            QUserPT q = QUserPT.userPT;
            long updated = new JPAUpdateClause(em, q)
                .where(q.userid.eq(userid))
                .set(q.magic, q.magic.add(magic))
                .execute();
            tx.commit();
            return updated <= 0;
        } catch(Exception e) {
            if (tx.isActive()) tx.rollback();
            return true;
        } finally {
            em.close();
        }
    }
    @Override
    public boolean ReduceMagic(String userid, int magic) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            QUserPT q = QUserPT.userPT;
            long updated = new JPAUpdateClause(em, q)
                .where(q.userid.eq(userid))
                .set(q.magic, q.magic.subtract(magic))
                .execute();
            tx.commit();
            return updated <= 0;
        } catch(Exception e) {
            if (tx.isActive()) tx.rollback();
            return true;
        } finally {
            em.close();
        }
    }
    @Override
    public int SaveTorrent(String seedid, File TTorent){
        try {
            Path storageDir = Paths.get(config.TORRENT_STORAGE_DIR);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
            String filename = TTorent.getName();
            Path target = storageDir.resolve(seedid + "_" + filename);
            Files.copy(TTorent.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

            // Calculate infoHash using ISO_8859_1 encoding method to match qBittorrent
            String infoHash = null;
            try {
                infoHash = calculateInfoHashReencoding(target.toFile());
                System.out.println("InfoHash (ISO_8859_1): " + infoHash);
            } catch (Exception e) {
                System.err.println("Warning: could not parse torrent infoHash: " + e.getMessage());
                // Fallback to direct extraction method
                try {
                    infoHash = calculateInfoHashDirect(target.toFile());
                    System.out.println("InfoHash (Direct): " + infoHash);
                } catch (Exception e2) {
                    System.err.println("Warning: fallback infoHash calculation also failed: " + e2.getMessage());
                }
            }

            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Seed seed = em.find(Seed.class, seedid);
                seed.url = target.toString();
                em.merge(seed);

                // upsert SeedHash only if we have a valid infoHash
                if (infoHash != null) {
                    SeedHash sh = new SeedHash();
                    sh.seedId = seedid;
                    sh.infoHash = infoHash;
                    em.merge(sh);
                }
                tx.commit();
                return 0;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                return 1;
            } finally {
                em.close();
            }
        } catch (Exception e) {
            return 1;
        }
    }
    @Override
    public File GetTTorent(String seedid, String userid) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        File file = null;
        try {
            Seed seed = em.find(Seed.class, seedid);
            if (seed == null || seed.url == null) {
                return null;
            }
            file = new File(seed.url);
            if (!file.exists()) {
                return null;
            }
            tx.begin();
            SeedDownload sd = new SeedDownload();
            sd.seedId = seedid;
            sd.userId = userid;
            LocalDateTime now = LocalDateTime.now();
            sd.downloadStart = now;
            sd.downloadEnd = now;
            em.persist(sd);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            // ignore persistence errors and still return the file
        } finally {
            em.close();
        }
        return file;
    }
    @Override
    public int AddRecord(TransRecord rd){
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(rd);
            tx.commit();
            // 返回1表示插入成功
            return 1;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return -1;
        } finally {
            em.close();
        }
    }
    
    /**
     * Calculate infoHash by extracting the original info dictionary bytes
     * from the torrent file, rather than re-encoding the parsed data.
     * This method preserves the original binary representation.
     */
    private String calculateInfoHashDirect(File torrentFile) throws Exception {
        byte[] torrentData = Files.readAllBytes(torrentFile.toPath());
        
        // Find the info dictionary in the raw torrent data
        int infoStart = findInfoDictionary(torrentData);
        if (infoStart == -1) {
            throw new Exception("Could not find info dictionary in torrent file");
        }
        
        // Extract the info dictionary bytes directly from the original torrent
        byte[] infoBytes = extractInfoBytes(torrentData, infoStart);
        
        // Debug: print first few bytes of info dict
        System.out.print("Info dict starts with: ");
        for (int i = 0; i < Math.min(20, infoBytes.length); i++) {
            System.out.printf("%02x ", infoBytes[i] & 0xff);
        }
        System.out.println();
        
        // Calculate SHA1 hash
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] digest = sha1.digest(infoBytes);
        
        // Convert to hex string
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        
        return sb.toString();
    }
    
    /**
     * Correct method using ISO_8859_1 encoding for infohash calculation
     * This matches qBittorrent's calculation method
     */
    private String calculateInfoHashReencoding(File torrentFile) throws Exception {
        byte[] torrentData = Files.readAllBytes(torrentFile.toPath());
        
        // Use ISO_8859_1 charset for infohash calculation (as per BitTorrent specification)
        Bencode bencodeInfoHash = new Bencode(java.nio.charset.StandardCharsets.ISO_8859_1);
        
        @SuppressWarnings("unchecked")
        Map<String,Object> meta = bencodeInfoHash.decode(torrentData, Type.DICTIONARY);
        @SuppressWarnings("unchecked")
        Map<String,Object> info = (Map<String,Object>) meta.get("info");
        
        if (info == null) {
            throw new Exception("No info dictionary found");
        }
        
        // Re-encode the info dictionary using ISO_8859_1
        byte[] infoBytes = bencodeInfoHash.encode(info);
        
        // Calculate SHA1 hash
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] digest = sha1.digest(infoBytes);
        
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        
        return sb.toString();
    }
    
    /**
     * Find the position of "4:info" in the torrent data
     */
    private int findInfoDictionary(byte[] data) {
        byte[] pattern = "4:info".getBytes();
        
        for (int i = 0; i <= data.length - pattern.length; i++) {
            boolean found = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Extract the info dictionary bytes from the original torrent data
     */
    private byte[] extractInfoBytes(byte[] data, int infoStart) throws Exception {
        // Skip "4:info" to get to the actual dictionary content
        int dictStart = infoStart + 6; // "4:info".length()
        
        if (dictStart >= data.length || data[dictStart] != 'd') {
            throw new Exception("Invalid info dictionary format");
        }
        
        // Find the matching 'e' that closes the info dictionary
        int dictEnd = findMatchingEnd(data, dictStart);
        if (dictEnd == -1) {
            throw new Exception("Could not find end of info dictionary");
        }
        
        // Extract the info dictionary bytes (including 'd' and 'e')
        int length = dictEnd - dictStart + 1;
        byte[] infoBytes = new byte[length];
        System.arraycopy(data, dictStart, infoBytes, 0, length);
        
        return infoBytes;
    }
    
    /**
     * Find the matching 'e' for a dictionary that starts with 'd'
     */
    private int findMatchingEnd(byte[] data, int start) {
        if (start >= data.length || data[start] != 'd') {
            return -1;
        }
        
        int depth = 0;
        int i = start;
        
        while (i < data.length) {
            byte b = data[i];
            
            if (b == 'd' || b == 'l') {
                // Dictionary or list start
                depth++;
                i++;
            } else if (b == 'e') {
                // Dictionary or list end
                depth--;
                if (depth == 0) {
                    return i;
                }
                i++;
            } else if (b == 'i') {
                // Integer: i<number>e
                i++; // skip 'i'
                while (i < data.length && data[i] != 'e') {
                    i++;
                }
                if (i < data.length) i++; // skip 'e'
            } else if (b >= '0' && b <= '9') {
                // String: <length>:<string>
                int lengthStart = i;
                while (i < data.length && data[i] >= '0' && data[i] <= '9') {
                    i++;
                }
                if (i < data.length && data[i] == ':') {
                    // Parse length
                    String lengthStr = new String(data, lengthStart, i - lengthStart);
                    int length = Integer.parseInt(lengthStr);
                    i++; // skip ':'
                    i += length; // skip string content
                } else {
                    // Invalid format
                    return -1;
                }
            } else {
                // Unknown character
                i++;
            }
        }
        
        return -1;
    }

    /**
     * 从数据库中查询所有 info_hash（hex 字符串）
     */
    public List<String> getAllInfoHashes() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                "SELECT sh.infoHash FROM SeedHash sh", String.class
            ).getResultList();
        } finally {
            em.close();
        }
    }
}