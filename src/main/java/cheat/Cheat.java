package cheat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.apache.commons.lang3.tuple.Pair;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.querydsl.jpa.impl.JPAQueryFactory;

import entity.Seed;
import entity.Appeal;
import entity.QAppeal;
import entity.QSeed;
import entity.User;
import entity.config;
import entity.QUser;
import entity.TTorent;
import entity.QTransRecord;
import entity.TransRecord;

public class Cheat implements CheatInterfnterface {

    @PersistenceContext
    private EntityManagerFactory emf;

    public Cheat() {
        config cfg = new config();
        Map<String,Object> props = new HashMap<>();
        props.put("javax.persistence.jdbc.url",
                  "jdbc:mysql://" + cfg.SqlURL + "/" + cfg.Database);
        props.put("javax.persistence.jdbc.user", cfg.SqlUsername);
        props.put("javax.persistence.jdbc.password", cfg.SqlPassword);
        this.emf = Persistence.createEntityManagerFactory("myPersistenceUnit", props);
    }

    @Override
    @Transactional
    public boolean AddAppeal(Appeal appeal) {
        EntityManager entityManager = emf.createEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(appeal);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            return false;
        }
        return true;
    }

    @Override
    public Appeal GetAppeal(String appealid) {
        try {
            EntityManager entityManager = emf.createEntityManager();
            JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
            QAppeal qAppeal = QAppeal.appeal;
            Appeal appeal = queryFactory
                .selectFrom(qAppeal)
                .where(qAppeal.appealid.eq(appealid))
                .fetchOne();
            return appeal;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Appeal[] GetAppealList() {
        EntityManager entityManager = emf.createEntityManager();
        try {
            JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
            QAppeal qAppeal = QAppeal.appeal;
            List<Appeal> appeals = queryFactory
                .selectFrom(qAppeal)
                .fetch();
            return appeals.toArray(new Appeal[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    @Override
    public boolean HandleAppeal(String appealid, Integer status) {
        EntityManager entityManager = emf.createEntityManager();
        try {
            Appeal appeal = GetAppeal(appealid);
            if (appeal != null) {
                appeal.status = status;
                entityManager.getTransaction().begin();
                entityManager.merge(appeal);
                User user = entityManager.find(User.class, appeal.appealuserid);
                // if (user != null && user.accountstate != false) {
                if (user != null) {
                    if (status == 1) {
                        user.accountstate = false;
                    }
                    entityManager.merge(user);
                }
                entityManager.getTransaction().commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            if (emf.createEntityManager().getTransaction().isActive()) {
                emf.createEntityManager().getTransaction().rollback();
            }
            return false;
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    @Override
    public Pair<String, String>[] GetFakeSeed() {
        List<Pair<String, String>> fakeSeeds = new ArrayList<>();
        try {
            EntityManager entityManager = emf.createEntityManager();
            JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
            QSeed qSeed = QSeed.seed;
            List<com.querydsl.core.Tuple> results = queryFactory
                .select(qSeed.seedid, qSeed.seeduserid)
                .from(qSeed)
                .where(qSeed.faketime.gt(config.getFakeTime()))
                .fetch();
            for (com.querydsl.core.Tuple result : results) {
                String seedid = result.get(qSeed.seedid);
                String userid = result.get(qSeed.seeduserid);
                fakeSeeds.add(Pair.of(seedid, userid));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return fakeSeeds.toArray(new Pair[0]);
    }

    @Override
    public String[] GetPunishedUserList() {
        List<String> punishedUsers = new ArrayList<>();
        try {
            EntityManager entityManager = emf.createEntityManager();
            JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
            entity.QUser qUser = entity.QUser.user;
            List<String> results = queryFactory
                .select(qUser.userid)
                .from(qUser)
                .where(qUser.accountstate.isTrue())
                .fetch();
            for (String userid : results) {
                punishedUsers.add(userid);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return punishedUsers.toArray(new String[0]);
    }

    @Override
    public void DetectTrans() {
        EntityManager entityManager = emf.createEntityManager();
        try {
            JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
            QSeed qSeed = QSeed.seed;
            QTransRecord qTransRecord = QTransRecord.transRecord;
            
            // 获取所有种子
            List<Seed> seeds = queryFactory
                .selectFrom(qSeed)
                .fetch();

            // 存储每个种子的delta值
            List<Double> allDeltas = new ArrayList<>();
            Map<String, Double> seedDeltaMap = new HashMap<>();
            
            // 计算每个种子的delta值（只考虑account_status不为1的用户）
            for (Seed seed : seeds) {
                List<TransRecord> tasks = queryFactory
                    .selectFrom(qTransRecord)
                    .leftJoin(qTransRecord.uploader).fetchJoin()
                    .leftJoin(qTransRecord.downloader).fetchJoin()
                    .where(qTransRecord.seedid.eq(seed.seedid))
                    .fetch();

                // 过滤掉account_status为1的用户的记录
                List<TransRecord> validTasks = new ArrayList<>();
                for (TransRecord task : tasks) {
                    boolean shouldInclude = true;
                    
                    // 检查上传者
                    if (task.uploaduserid != null) {
                        User uploader = entityManager.find(User.class, task.uploaduserid);
                        if (uploader != null && uploader.accountstate) {
                            shouldInclude = false;
                        }
                    }
                    
                    // 检查下载者
                    if (task.downloaduserid != null) {
                        User downloader = entityManager.find(User.class, task.downloaduserid);
                        if (downloader != null && downloader.accountstate) {
                            shouldInclude = false;
                        }
                    }
                    
                    if (shouldInclude) {
                        validTasks.add(task);
                    }
                }

                if (validTasks.isEmpty()) continue;

                // 计算该种子的总delta
                double totalDelta = 0;
                for (TransRecord task : validTasks) {
                    long upload = task.upload != null ? task.upload : 0L;
                    long download = task.download != null ? task.download : 0L;
                    totalDelta += (upload - download);
                }

                allDeltas.add(totalDelta);
                seedDeltaMap.put(seed.seedid, totalDelta);
            }

            if (allDeltas.size() < 2) return; // 需要至少2个种子才能计算标准差

            // 计算平均值和标准差
            double sum = 0;
            for (double delta : allDeltas) {
                sum += delta;
            }
            double mean = sum / allDeltas.size();

            double variance = 0;
            for (double delta : allDeltas) {
                variance += Math.pow(delta - mean, 2);
            }
            double stdDev = Math.sqrt(variance / allDeltas.size());

            // 检测异常种子并处理参与者
            for (Seed seed : seeds) {
                Double seedDelta = seedDeltaMap.get(seed.seedid);
                if (seedDelta == null) continue;

                // 检查是否偏离1个标准差
                if (Math.abs(seedDelta - mean) > stdDev) {
                    // 获取该种子的所有传输记录参与者
                    List<TransRecord> suspiciousTasks = queryFactory
                        .selectFrom(qTransRecord)
                        .where(qTransRecord.seedid.eq(seed.seedid))
                        .fetch();

                    // 更新所有参与者的detectedCount
                    for (TransRecord task : suspiciousTasks) {
                        // 处理上传者
                        if (task.uploaduserid != null) {
                            User uploader = entityManager.find(User.class, task.uploaduserid);
                            if (uploader != null && !uploader.accountstate) {
                                uploader.detectedCount++;
                                uploader.lastDetectedTime = new java.util.Date();
                                entityManager.merge(uploader);
                            }
                        }
                        
                        // 处理下载者
                        if (task.downloaduserid != null) {
                            User downloader = entityManager.find(User.class, task.downloaduserid);
                            if (downloader != null && !downloader.accountstate) {
                                downloader.detectedCount++;
                                downloader.lastDetectedTime = new java.util.Date();
                                entityManager.merge(downloader);
                            }
                        }
                    }
                }
            }
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    @Override
    @Transactional
    public void DetectFakeSeed(){
        EntityManager entityManager = emf.createEntityManager();
        try {
            String torrentDir = entity.config.TORRENT_STORAGE_DIR;
            String scriptPath = "./src/main/java/cheat/fakeSeed.bash";
            
            ProcessBuilder processBuilder = new ProcessBuilder("bash", scriptPath, torrentDir);
            processBuilder.inheritIO();
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                System.out.println("FakeSeed detection completed successfully");
                
                // 处理检测结果
                String failedDir = torrentDir + "/failed_torrents";
                java.io.File failedDirFile = new java.io.File(failedDir);
                
                // 获取失败种子文件列表
                java.util.Set<String> failedTorrentNames = new java.util.HashSet<>();
                if (failedDirFile.exists() && failedDirFile.isDirectory()) {
                    java.io.File[] failedFiles = failedDirFile.listFiles((dir, name) -> name.endsWith(".torrent"));
                    if (failedFiles != null) {
                        for (java.io.File file : failedFiles) {
                            failedTorrentNames.add(file.getName());
                        }
                    }
                }
                
                JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
                QSeed qSeed = QSeed.seed;
                
                // 收集假种子的拥有者
                java.util.Set<String> fakeOwners = new java.util.HashSet<>();
                
                // 更新数据库中的fake_hits字段
                List<Seed> allSeeds = queryFactory
                    .selectFrom(qSeed)
                    .where(qSeed.url.isNotNull())
                    .fetch();
                
                for (Seed seed : allSeeds) {
                    java.io.File seedFile = new java.io.File(seed.url);
                    String fileName = seedFile.getName();
                    
                    if (failedTorrentNames.contains(fileName)) {
                        // 失败种子，fake_hits + 1
                        seed.faketime++;
                        seed.lastfakecheck = new java.util.Date();
                        fakeOwners.add(seed.seeduserid);
                        System.out.println("Found fake seed: " + seed.seedid + " (fake_hits: " + seed.faketime + ")");
                    } else {
                        // 正常种子，fake_hits 清零
                        if (seed.faketime > 0) {
                            seed.faketime = 0;
                            seed.lastfakecheck = new java.util.Date();
                            System.out.println("Reset fake_hits for seed: " + seed.seedid);
                        }
                    }
                    entityManager.merge(seed);
                }
                
                // 更新所有用户的 fakeDetectedCount 和 fakeLastDetectedTime
                entity.QUser qUser = entity.QUser.user;
                List<User> allUsers = queryFactory
                    .selectFrom(qUser)
                    .fetch();
                
                java.util.Date now = new java.util.Date();
                for (User user : allUsers) {
                    if (fakeOwners.contains(user.userid)) {
                        // 假种子拥有者，fakeDetectedCount + 1
                        user.fakeDetectedCount++;
                        System.out.println("Increased fake detection count for user: " + user.userid + " (count: " + user.fakeDetectedCount + ")");
                    } else {
                        // 其他用户，fakeDetectedCount 清零
                        if (user.fakeDetectedCount > 0) {
                            user.fakeDetectedCount = 0;
                            System.out.println("Reset fake detection count for user: " + user.userid);
                        }
                    }
                    // 更新所有用户的检测时间
                    user.fakeLastDetectedTime = now;
                    entityManager.merge(user);
                }
                
                System.out.println("Fake seed detection and database update completed");
            } else {
                System.err.println("FakeSeed detection failed with exit code: " + exitCode);
            }
        } catch (Exception e) {
            System.err.println("Error running FakeSeed detection script: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    @Override
    public boolean DetectFakeSeed(String seedid){
        return false;
    }

    @Override
    public void PunishUser(){
        EntityManager entityManager = emf.createEntityManager();
        try {
            JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
            entity.QUser qUser = entity.QUser.user;
            List<User> users = queryFactory
                .selectFrom(qUser)
                .where(qUser.detectedCount.gt(config.getCheatTime()) 
                    .or(qUser.fakeDetectedCount.gt(config.getFakeTime())))
                .fetch();
            if (users.isEmpty()) {
                System.out.println("No users to punish.");
                return;
            }
            entityManager.getTransaction().begin();
            for (User user : users) {
                user.accountstate = true;
                entityManager.merge(user);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
    }

    @Override
    public User[] GetCheatUsers() {
        EntityManager entityManager = emf.createEntityManager();
        List<User> cheatUsers = new ArrayList<>();
        try {
            JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
            QUser qUser = QUser.user;
            cheatUsers = queryFactory
                .selectFrom(qUser)
                .where(qUser.accountstate.eq(true))
                .fetch();
            return cheatUsers.toArray(new User[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (entityManager != null){
                entityManager.close();
            }
        }
    }

    @Override
    public User[] GetSuspiciousUsers() {
        EntityManager entityManager = emf.createEntityManager();
        List<User> suspiciousUsers = new ArrayList<>();
        try {
            JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
            QUser qUser = QUser.user;
            suspiciousUsers = queryFactory
                .selectFrom(qUser)
                .where((qUser.detectedCount.gt(0)
                    .or(qUser.fakeDetectedCount.gt(0)))
                    .and(qUser.accountstate.eq(false)))
                .fetch();
            return suspiciousUsers.toArray(new User[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (entityManager != null){
                entityManager.close();
            }
        }
    }
    
    @Override
    public int UnbanUser(String userid) {
        EntityManager entityManager = emf.createEntityManager();
        try {
            User user = entityManager.find(User.class, userid);
            if (user == null) {
                return 1; // 用户不存在
            }
            if (user.accountstate) {
                user.accountstate = false; // 解封用户
                entityManager.getTransaction().begin();
                entityManager.merge(user);
                entityManager.getTransaction().commit();
                return 0; // 成功解封
            }
            return 2; // 用户未被封禁
        } catch (Exception e) {
            e.printStackTrace();
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            return -1; // 出现异常
        } finally {
            if (entityManager != null){
                entityManager.close();
            }
        }
    }

    @Override
    public int BanUser(String userid) {
        EntityManager entityManager = emf.createEntityManager();
        try {
            User user = entityManager.find(User.class, userid);
            if (user == null) {
                return 1; // 用户不存在
            }
            if (!user.accountstate) {
                user.accountstate = true; // 封禁用户
                entityManager.getTransaction().begin();
                entityManager.merge(user);
                entityManager.getTransaction().commit();
                return 0; // 成功封禁
            }
            return 2; // 用户已被封禁
        } catch (Exception e) {
            e.printStackTrace();
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            return -1; // 出现异常
        } finally {
            if (entityManager != null){
                entityManager.close();
            }
        }
    }

    @Override
    public int SubmitAppeal(String userid, String content, File file) {
        EntityManager entityManager = emf.createEntityManager();
        try {
            User user = entityManager.find(User.class, userid);
            if (user == null) {
                return 1; // 用户不存在
            }
            Appeal appeal = new Appeal();
            appeal.appealid = java.util.UUID.randomUUID().toString();
            appeal.appealuserid = userid;
            appeal.content = content;
            appeal.user = user; // 设置关联的用户
            Path storageDir = Paths.get(config.APPEAL_STORAGE_DIR);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
            String filename = file.getName();
            Path target = storageDir.resolve(appeal.appealid + "_" + filename);
            Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            appeal.status = 0; // 初始状态为未处理
            appeal.fileURL = target.toString(); // 设置文件存储路径
            entityManager.getTransaction().begin();
            entityManager.persist(appeal);
            entityManager.getTransaction().commit();
            return 0; // 成功提交申诉
        } catch (Exception e) {
            e.printStackTrace();
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            return -1; // 出现异常
        } finally {
            if (entityManager != null){
                entityManager.close();
            }
        }
    }
}