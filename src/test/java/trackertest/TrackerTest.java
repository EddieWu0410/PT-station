package trackertest;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import entity.Seed;
import entity.TransRecord;
import entity.config;
import tracker.Tracker;
public class TrackerTest {
    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static List<String> userIds;
    private static Map<String, Long> originalUploads;
    private static Tracker tracker;
    private static List<String> infoHashes;
    @BeforeAll
    static void setup() throws Exception {
        // 强制加载 MySQL 驱动，否则无法建立连接
        Class.forName("com.mysql.cj.jdbc.Driver");
        config cfg = new config();
        Map<String,Object> props = new HashMap<>();
        // 添加时区和 SSL 参数
        String jdbcUrl = String.format(
            "jdbc:mysql://%s/%s?useSSL=false&serverTimezone=UTC",
            cfg.SqlURL, cfg.TestDatabase);
        props.put("javax.persistence.jdbc.url", jdbcUrl);
        props.put("javax.persistence.jdbc.user", cfg.SqlUsername);
        props.put("javax.persistence.jdbc.password", cfg.SqlPassword);
        props.put("javax.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
        emf = Persistence.createEntityManagerFactory("myPersistenceUnit", props);
        em = emf.createEntityManager();
        // 使用简单实体名而非带包名前缀
        userIds = em.createQuery(
            "select u.userid from UserPT u", String.class
        ).getResultList();
        // 保存初始 upload 值
        originalUploads = new HashMap<>();
        for (String uid : userIds) {
            Long up = em.createQuery(
                "select u.upload from UserPT u where u.userid = :uid", Long.class
            ).setParameter("uid", uid)
             .getSingleResult();
            originalUploads.put(uid, up != null ? up : 0L);
        }
        // fetch real infoHash values
        infoHashes = em.createQuery(
            "select s.infoHash from SeedHash s", String.class
        ).getResultList();
        tracker = new Tracker(emf);
    }
    @AfterAll
    static void teardown() {
        // 清理：删除测试过程中保存的所有 torrent 文件
        File storageDir = new File(config.TORRENT_STORAGE_DIR);
        if (storageDir.exists() && storageDir.isDirectory()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        f.delete();
                    }
                }
            }
        }
        if (em != null && em.isOpen()) em.close();
        if (emf != null && emf.isOpen()) emf.close();
    }
    
//     @TestFactory
//     Collection<DynamicTest> testAddUpLoad() {
//     Random rnd = new Random();
//     String ih = infoHashes.get(0);
//     return userIds.stream()
//         .map(uid -> DynamicTest.dynamicTest("AddUpLoad for user " + uid, () -> {
//             // 1) 获取该用户当前的总上传量
//             Long currentUserUpload = em.createQuery(
//                 "SELECT u.upload FROM UserPT u WHERE u.userid = :uid", Long.class)
//                 .setParameter("uid", uid)
//                 .getSingleResult();

//             // 2) 获取该用户在该种子上的当前上传量
//             Long currentSeedUpload = em.createQuery(
//                 "SELECT COALESCE(SUM(t.upload),0) FROM TransRecord t " +
//                 "WHERE t.uploaduserid = :uid AND t.seedid = " +
//                 "(SELECT s.seedId FROM SeedHash s WHERE s.infoHash = :ih)", Long.class)
//                 .setParameter("uid", uid)
//                 .setParameter("ih", ih)
//                 .getSingleResult();

//             int delta = rnd.nextInt(1000) + 1;
//             long newSeedTotal = currentSeedUpload + delta;

//             // 调用业务方法（内部自管理事务）
//             Assertions.assertFalse(
//                 tracker.AddUpLoad(uid, (int)newSeedTotal, ih),
//                 "AddUpLoad should return false on successful operation"
//             );
//             em.clear();

//             // 验证 TransRecord 表中的实际总和
//             Long actualTransRecordSum = em.createQuery(
//                 "SELECT COALESCE(SUM(t.upload),0) FROM TransRecord t WHERE t.uploaduserid = :uid", Long.class)
//                 .setParameter("uid", uid)
//                 .getSingleResult();
//             // 验证 UserPT.upload
//             Long userPTUpload = em.createQuery(
//                 "SELECT u.upload FROM UserPT u WHERE u.userid = :uid", Long.class)
//                 .setParameter("uid", uid)
//                 .getSingleResult();

//             Assertions.assertEquals(
//                 actualTransRecordSum, userPTUpload,
//                 "UserPT.upload 应等于 TransRecord 中的总和"
//             );
//             Assertions.assertEquals(
//                 currentUserUpload + delta, userPTUpload.longValue(),
//                 "UserPT.upload 应增加 delta"
//             );

//             // 恢复原始上传量，避免测试污染
//             Assertions.assertFalse(tracker.ReduceUpLoad(uid, delta));
//         }))
//         .collect(Collectors.toList());
// }
    
   
   
    // @TestFactory
    // Collection<DynamicTest> testReduceUpLoad() {
    //     Random rnd = new Random();
    //     String ih = infoHashes.get(0);  // use same infoHash as other tests
    //     return userIds.stream()
    //         .map(uid -> DynamicTest.dynamicTest("ReduceUpLoad for user " + uid, () -> {
    //             long before = originalUploads.get(uid);
    //             int max = (int)Math.min(before, 1000);
    //             int delta = max > 0 ? rnd.nextInt(max) + 1 : 0;
    //             if (delta == 0) return;  // 无可减量时跳过
    //             // ReduceUpLoad 前打印
    //             System.out.println("Running ReduceUpLoad assert for user=" + uid + ", delta=" + delta);
    //             Assertions.assertFalse(tracker.ReduceUpLoad(uid, delta));
    //             System.out.println("ReduceUpLoad assert passed for user=" + uid);
    //             Long after = em.createQuery(
    //                 "select u.upload from UserPT u where u.userid = :uid", Long.class
    //             ).setParameter("uid", uid)
    //              .getSingleResult();
    //             // 减少后值断言前打印
    //             System.out.println("Running post-reduce-value assert for user=" + uid);
    //             Assertions.assertEquals(before - delta, after);
    //             System.out.println("Post-reduce-value assert passed for user=" + uid);
    //             // 回滚 AddUpLoad 前打印
    //             System.out.println("Running rollback AddUpLoad assert for user=" + uid + ", delta=" + delta);
    //             Assertions.assertFalse(tracker.AddUpLoad(uid, (int)before, ih));
    //             System.out.println("Rollback AddUpLoad assert passed for user=" + uid);
    //         }))
    //         .collect(Collectors.toList());
    // }
    
    /*
    @TestFactory
    Collection<DynamicTest> testAddDownload() {
        Random rnd = new Random();
        String ih = infoHashes.get(0);
        return userIds.stream()
            .map(uid -> DynamicTest.dynamicTest("AddDownload for user " + uid, () -> {
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                try {
                    // 获取该用户当前的总下载量
                    Long currentUserDownload = em.createQuery(
                        "SELECT COALESCE(u.download,0) FROM UserPT u WHERE u.userid = :uid", Long.class
                    ).setParameter("uid", uid).getSingleResult();
                    
                    // 获取该用户在该种子上的当前下载量
                    Long currentSeedDownload = em.createQuery(
                        "SELECT COALESCE(SUM(t.download),0) FROM TransRecord t WHERE t.downloaduserid = :uid AND t.seedid = " +
                        "(SELECT s.seedId FROM SeedHash s WHERE s.infoHash = :ih)", Long.class
                    ).setParameter("uid", uid).setParameter("ih", ih).getSingleResult();
                    
                    int delta = rnd.nextInt(1000) + 1;
                    long newSeedTotal = currentSeedDownload + delta;
                    
                    Assertions.assertFalse(tracker.AddDownload(uid, (int)newSeedTotal, ih),
                        "AddDownload should return false on successful operation");
                    em.clear();

                    // commit & restart test TX so we see the data that tracker committed
                    tx.commit();
                    em.clear();
                    tx.begin();

                    // 验证 UserPT.download 是否等于 TransRecord 表中该用户的实际总和
                    Long actualTransRecordSum = em.createQuery(
                        "SELECT COALESCE(SUM(t.download),0) FROM TransRecord t WHERE t.downloaduserid = :uid", Long.class
                    ).setParameter("uid", uid).getSingleResult();
                    
                    Long userPTDownload = em.createQuery(
                        "SELECT u.download FROM UserPT u WHERE u.userid = :uid", Long.class
                    ).setParameter("uid", uid).getSingleResult();
                    
                    Assertions.assertEquals(actualTransRecordSum, userPTDownload,
                        "UserPT.download should equal sum of TransRecord.download for this user");
                    
                    // 验证用户总下载量增加了预期的delta
                    Assertions.assertEquals(currentUserDownload + delta, userPTDownload.longValue(),
                        "User total download should increase by delta");
                } finally {
                    tx.rollback();
                    em.clear();
                }
            }))
            .collect(Collectors.toList());
    }
    */
    @TestFactory
    Collection<DynamicTest> testReduceDownload() {
        Random rnd = new Random();
        return userIds.stream()
            .map(uid -> DynamicTest.dynamicTest("ReduceDownload for user " + uid, () -> {
                Long before = em.createQuery(
                    "SELECT u.download FROM UserPT u WHERE u.userid = :uid", Long.class
                ).setParameter("uid", uid).getSingleResult();
                before = before != null ? before : 0L;
                int max = before.intValue();
                int delta = max > 0 ? rnd.nextInt(max) + 1 : 0;
                if (delta == 0) return;
                
                System.out.println("Running ReduceDownload assert for user=" + uid + ", delta=" + delta);
                Assertions.assertFalse(tracker.ReduceDownload(uid, delta));
                em.clear();
                
                Long after = em.createQuery(
                    "SELECT u.download FROM UserPT u WHERE u.userid = :uid", Long.class
                ).setParameter("uid", uid).getSingleResult();
                System.out.println("Running post-reduce-download-value assert for user=" + uid);
                Assertions.assertEquals(before - delta, after.longValue());
                
                System.out.println("Running rollback AddDownload assert for user=" + uid + ", delta=" + delta);
                // 使用有效的 infoHash
                Assertions.assertFalse(
                    tracker.AddDownload(uid, before.intValue(), infoHashes.get(0))
                );
            }))
            .collect(Collectors.toList());
    }
    @TestFactory
    Collection<DynamicTest> testAddMagic() {
        Random rnd = new Random();
        return userIds.stream()
            .map(uid -> DynamicTest.dynamicTest("AddMagic for user " + uid, () -> {
                int delta = rnd.nextInt(1000) + 1;
                Integer before = em.createQuery(
                    "select u.magic from UserPT u where u.userid = :uid", Integer.class
                ).setParameter("uid", uid).getSingleResult();
                before = before != null ? before : 0;
                System.out.println("Running AddMagic assert for user=" + uid + ", delta=" + delta);
                Assertions.assertFalse(tracker.AddMagic(uid, delta));
                em.clear();
                Integer after = em.createQuery(
                    "select u.magic from UserPT u where u.userid = :uid", Integer.class
                ).setParameter("uid", uid).getSingleResult();
                System.out.println("Running magic-value assert for user=" + uid);
                Assertions.assertEquals((Integer)(before + delta), after);
                System.out.println("Running rollback ReduceMagic assert for user=" + uid + ", delta=" + delta);
                Assertions.assertFalse(tracker.ReduceMagic(uid, delta));
            }))
            .collect(Collectors.toList());
    }
    @TestFactory
    Collection<DynamicTest> testReduceMagic() {
        Random rnd = new Random();
        return userIds.stream()
            .map(uid -> DynamicTest.dynamicTest("ReduceMagic for user " + uid, () -> {
                Integer before = em.createQuery(
                    "select u.magic from UserPT u where u.userid = :uid", Integer.class
                ).setParameter("uid", uid).getSingleResult();
                before = before != null ? before : 0;
                int max = before.intValue();
                int delta = max > 0 ? rnd.nextInt(max) + 1 : 0;
                if (delta == 0) return;
                System.out.println("Running ReduceMagic assert for user=" + uid + ", delta=" + delta);
                Assertions.assertFalse(tracker.ReduceMagic(uid, delta));
                em.clear();
                Integer after = em.createQuery(
                    "select u.magic from UserPT u where u.userid = :uid", Integer.class
                ).setParameter("uid", uid).getSingleResult();
                System.out.println("Running post-reduce-magic-value assert for user=" + uid);
                Assertions.assertEquals((Integer)(before - delta), after);
                System.out.println("Running rollback AddMagic assert for user=" + uid + ", delta=" + delta);
                Assertions.assertFalse(tracker.AddMagic(uid, delta));
            }))
            .collect(Collectors.toList());
    }
    @TestFactory
    Collection<DynamicTest> testAddRecord() {
        Random rnd = new Random();
        // 取所有 seed_id 用于外键测试
        List<String> seedIds = em.createQuery(
            "select s.seedid from Seed s", String.class
        ).getResultList();
        // 若无可用 seedId 则跳过此组测试，避免 rnd.nextInt(0) 抛错
        // if (seedIds.isEmpty()) {
        //     return Collections.emptyList();
        // }
        return IntStream.range(0, 10)
            .mapToObj(i -> DynamicTest.dynamicTest("AddRecord test #" + i, () -> {
                // 随机构造 TransRecord
                String uploaderId = userIds.get(rnd.nextInt(userIds.size()));
                String downloaderId = userIds.get(rnd.nextInt(userIds.size()));
                String seedId = seedIds.get(rnd.nextInt(seedIds.size()));
                String taskId = UUID.randomUUID().toString();
                TransRecord rd = new TransRecord();
                rd.taskid = taskId;
                rd.uploaduserid = uploaderId;
                rd.downloaduserid = downloaderId;
                rd.seedid = seedId;
                rd.upload = (long) rnd.nextInt(10000);
                rd.download = (long) rnd.nextInt(10000);
                rd.maxupload = rd.upload + (long) rnd.nextInt(10000);
                rd.maxdownload = rd.download + (long) rnd.nextInt(10000);
                // 调用待测方法
                int ret = tracker.AddRecord(rd);
                Assertions.assertTrue(ret > 0, "返回值应为新记录主键");
                // 验证已插入
                TransRecord fetched = em.find(TransRecord.class, rd.taskid);

                Assertions.assertNotNull(fetched, "查询应返回非空实体");
                Assertions.assertEquals(rd.upload, fetched.upload);
                Assertions.assertEquals(rd.download, fetched.download);
                Assertions.assertEquals(rd.maxupload, fetched.maxupload);
                Assertions.assertEquals(rd.maxdownload, fetched.maxdownload);
            }))
            .collect(Collectors.toList());
    }
    @TestFactory
    Collection<DynamicTest> testSaveTorrent() {
        List<String> seedIds = em.createQuery(
            "select s.seedid from Seed s", String.class
        ).getResultList();
        return seedIds.stream()
            .map(sid -> DynamicTest.dynamicTest("SaveTorrent for seed " + sid, () -> {
                // 准备一个临时空文件
                File temp = File.createTempFile(sid + "_test", ".torrent");
                // 调用 SaveTorrent
                int ret = tracker.SaveTorrent(sid, temp);
                Assertions.assertEquals(0, ret, "SaveTorrent 应返回 0");
                // 验证文件已按约定存储
                File stored = new File(config.TORRENT_STORAGE_DIR,
                                       sid + "_" + temp.getName());
                Assertions.assertTrue(stored.exists(), "存储文件应存在");
                // 验证数据库中 url 字段已更新
                em.clear();
                Seed seed = em.find(Seed.class, sid);
                // 将 seed.url 转为 File 再取绝对路径进行比较
                Assertions.assertEquals(
                    stored.getAbsolutePath(),
                    new File(seed.url).getAbsolutePath(),
                    "Seed.url 应更新为存储路径"
                );
                // 清理测试文件
                stored.delete();
                temp.delete();
            }))
            .collect(Collectors.toList());
    }
    @TestFactory
    Collection<DynamicTest> testGetTTorent() {
        // 拿到所有 seedid
        List<String> seedIds = em.createQuery(
            "select s.seedid from Seed s", String.class
        ).getResultList();
        return seedIds.stream()
            .map(sid -> DynamicTest.dynamicTest("GetTTorent for seed " + sid, () -> {
                // 准备一个临时空文件并 SaveTorrent
                File temp = File.createTempFile(sid + "_test", ".torrent");
                int saveRet = tracker.SaveTorrent(sid, temp);
                Assertions.assertEquals(0, saveRet, "SaveTorrent 应返回 0");
                // 刷新上下文并取回更新后的 seed 实体
                em.clear();
                Seed seed = em.find(Seed.class, sid);
                Assertions.assertNotNull(seed.url, "Seed.url 应已被更新");
                // 调用 GetTTorent 并断言路径一致
                String uid = userIds.get(0), ip = "127.0.0.1";
                File ret = tracker.GetTTorent(sid, uid);
                File expected = new File(seed.url);
                Assertions.assertNotNull(ret, "应返回文件对象");
                Assertions.assertEquals(
                    expected.getAbsolutePath(),
                    ret.getAbsolutePath(),
                    "返回文件路径应与Seed.url一致"
                );
                // 清理：删掉本地文件，回滚 DB url 字段，删掉 temp
                expected.delete();
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                seed.url = null;
                em.merge(seed);
                tx.commit();
                temp.delete();
            }))
            .collect(Collectors.toList());
    }
    
}
