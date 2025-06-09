package cheattest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import cheat.Cheat;
import entity.Appeal;
import entity.Seed;
import entity.User;
import entity.UserPT;
import entity.config;

public class cheatsystest {
    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static Cheat cheat;
    private static List<String> insertedAppealIds = new ArrayList<>();
    private static List<String> insertedSeedIds = new ArrayList<>(); // 用于记录插入的假种子 ID

    @BeforeAll
    static void setup() throws Exception {
        // 加载 MySQL 驱动
        Class.forName("com.mysql.cj.jdbc.Driver");
        Map<String, Object> props = new HashMap<>();
        config cfg = new config();
        String jdbcUrl = String.format(
            "jdbc:mysql://%s/%s?useSSL=false&serverTimezone=UTC",
            cfg.SqlURL, cfg.TestDatabase);
        props.put("javax.persistence.jdbc.url", jdbcUrl);
        props.put("javax.persistence.jdbc.user", cfg.SqlUsername);
        props.put("javax.persistence.jdbc.password", cfg.SqlPassword);
        props.put("javax.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
        emf = Persistence.createEntityManagerFactory("myPersistenceUnit", props);
        em = emf.createEntityManager();
        cheat = new Cheat();
        // 通过反射注入 entityManager
        java.lang.reflect.Field f = Cheat.class.getDeclaredField("entityManager");
        f.setAccessible(true);
        f.set(cheat, em);
    }

    @AfterAll
    static void teardown() {
        // 清理测试过程中插入的 Appeal 数据
        if (em != null && em.isOpen()) {
            em.getTransaction().begin();
            for (String appealId : insertedAppealIds) {
                Appeal appeal = em.find(Appeal.class, appealId);
                if (appeal != null) {
                    em.remove(appeal);
                }
            }
            em.getTransaction().commit();
        }

        // 清理测试过程中插入的 Seed 数据
        if (em != null && em.isOpen()) {
            em.getTransaction().begin();
            for (String seedId : insertedSeedIds) {
                Seed seed = em.find(Seed.class, seedId);
                if (seed != null) {
                    em.remove(seed);
                }
            }
            em.getTransaction().commit();
        }

        // 清理测试过程中插入的 User 数据
        if (em != null && em.isOpen()) {
            em.getTransaction().begin();
            List<String> insertedUserIds = em.createQuery("SELECT u.userid FROM User u WHERE u.accountstate = true", String.class)
                                             .getResultList();
            for (String userId : insertedUserIds) {
                User user = em.find(User.class, userId);
                if (user != null) {
                    em.remove(user);
                }
            }
            em.getTransaction().commit();
        }

        // 关闭 EntityManager 和 EntityManagerFactory
        if (em != null && em.isOpen()) em.close();
        if (emf != null && emf.isOpen()) emf.close();
    }

    @TestFactory
    Collection<DynamicTest> testAddAppeal() {
        // 查询数据库中已存在的一个用户
        String userId = em.createQuery("SELECT u.userid FROM User u", String.class)
                          .setMaxResults(1)
                          .getSingleResult();

        return IntStream.range(0, 10)
            .mapToObj(i -> DynamicTest.dynamicTest("AddAppeal test #" + i, () -> {
                // 插入 Appeal
                String appealId = UUID.randomUUID().toString();
                Appeal appeal = new Appeal();
                appeal.appealid = appealId;
                appeal.appealuserid = userId;
                appeal.content = "Test appeal content " + i;
                appeal.fileURL = "http://example.com/file" + i;
                appeal.status = 0;

                em.getTransaction().begin();
                boolean result = cheat.AddAppeal(appeal);
                em.getTransaction().commit();

                Assertions.assertTrue(result, "AddAppeal 应返回 true");

                // 验证 Appeal 是否插入
                Appeal fetched = em.find(Appeal.class, appealId);
                Assertions.assertNotNull(fetched, "数据库应能查到新插入的 Appeal");
                Assertions.assertEquals(userId, fetched.appealuserid);
                Assertions.assertEquals("Test appeal content " + i, fetched.content);

                // 清理
                em.getTransaction().begin();
                em.remove(fetched);
                em.getTransaction().commit();
            }))
            .collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testGetAppeal() {
        // 查询数据库中已存在的10个Appeal
        List<Appeal> appeals = em.createQuery("SELECT a FROM Appeal a", Appeal.class)
                             .setMaxResults(10)
                             .getResultList();

        List<DynamicTest> tests = new ArrayList<>();

        // 前面用已有的Appeal测试能查到
        for (int i = 0; i < Math.min(7, appeals.size()); i++) {
            Appeal appeal = appeals.get(i);
            tests.add(DynamicTest.dynamicTest("GetAppeal found test #" + i, () -> {
                Appeal fetched = cheat.GetAppeal(appeal.appealid);
                Assertions.assertNotNull(fetched, "GetAppeal 应返回非空");
                Assertions.assertEquals(appeal.appealid, fetched.appealid);
                Assertions.assertEquals(appeal.appealuserid, fetched.appealuserid);
                Assertions.assertEquals(appeal.content, fetched.content);
            }));
        }

        // 后3个用随机UUID测试查不到
        for (int i = 7; i < 10; i++) {
            String notExistId = UUID.randomUUID().toString();
            tests.add(DynamicTest.dynamicTest("GetAppeal not found test #" + i, () -> {
                Appeal fetched = cheat.GetAppeal(notExistId);
                Assertions.assertNull(fetched, "GetAppeal 查询不存在的id应返回null");
            }));
        }

        return tests;
    }

    @TestFactory
    Collection<DynamicTest> testGetAppealList() {
        // 查询数据库中已存在的一个用户
        String userId = em.createQuery("SELECT u.userid FROM User u", String.class)
                      .setMaxResults(1)
                      .getSingleResult();
        // 用于记录测试过程中插入的 Appeal ID

        return IntStream.range(0, 10)
            .mapToObj(i -> DynamicTest.dynamicTest("GetAppealList test #" + i, () -> {
                // 插入一个新的 Appeal
                String appealId = UUID.randomUUID().toString();
                Appeal appeal = new Appeal();
                appeal.appealid = appealId;
                appeal.appealuserid = userId;
                appeal.content = "GetAppealList test content " + i;
                appeal.fileURL = "http://example.com/file" + i;
                appeal.status = 0;

                em.getTransaction().begin();
                em.persist(appeal);
                em.getTransaction().commit();

                // 记录插入的 Appeal ID
                insertedAppealIds.add(appealId);

                // 调用 GetAppealList 并验证
                Appeal[] appeals = cheat.GetAppealList();
                Assertions.assertNotNull(appeals, "GetAppealList 应返回非空");

                // 检查整个列表是否包含所有插入的数据
                for (String id : insertedAppealIds) {
                    boolean found = Arrays.stream(appeals)
                                           .anyMatch(a -> a.appealid.equals(id));
                    Assertions.assertTrue(found, "GetAppealList 应包含插入的 Appeal ID: " + id);
                }
            }))
            .collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testHandleAppeal() {
        // 查询数据库中已存在的一个用户
        String userId = em.createQuery("SELECT u.userid FROM User u", String.class)
                          .setMaxResults(1)
                          .getSingleResult();

        return IntStream.range(0, 10)
            .mapToObj(i -> DynamicTest.dynamicTest("HandleAppeal test #" + i, () -> {
                // 插入一个新的 Appeal
                String appealId = UUID.randomUUID().toString();
                Appeal appeal = new Appeal();
                appeal.appealid = appealId;
                appeal.appealuserid = userId;
                appeal.content = "HandleAppeal test content " + i;
                appeal.fileURL = "http://example.com/file" + i;
                appeal.status = 0;

                em.getTransaction().begin();
                em.persist(appeal);
                em.getTransaction().commit();

                // 如果 newStatus 为 1，先将用户的 account_status 设置为 true
                if (i % 2 == 0) { // 偶数索引对应 newStatus = 1
                    em.getTransaction().begin();
                    User user = em.find(User.class, userId);
                    Assertions.assertNotNull(user, "数据库应能查到相关用户");
                    user.accountstate = true; // 设置为 true
                    em.getTransaction().commit();
                }

                // 测试处理申诉
                int newStatus = (i % 2 == 0) ? 1 : 2; // 偶数索引通过申诉，奇数索引拒绝申诉
                boolean result = cheat.HandleAppeal(appealId, newStatus);
                Assertions.assertTrue(result, "HandleAppeal 应返回 true");

                // 验证 Appeal 状态是否更新
                Appeal updatedAppeal = em.find(Appeal.class, appealId);
                Assertions.assertNotNull(updatedAppeal, "数据库应能查到更新后的 Appeal");
                Assertions.assertEquals(newStatus, updatedAppeal.status, "Appeal 的状态应被更新为: " + newStatus);

                // 如果申诉通过，验证用户的 account_status 是否被设置为 false
                if (newStatus == 1) {
                    User user = em.find(User.class, userId);
                    Assertions.assertNotNull(user, "数据库应能查到相关用户");
                    Assertions.assertFalse(user.accountstate, "通过申诉后用户的 account_status 应为 false");
                }

                // 清理测试数据
                em.getTransaction().begin();
                em.remove(updatedAppeal);
                em.getTransaction().commit();
            }))
            .collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testGetFakeSeed() {
        return IntStream.range(0, 10)
            .mapToObj(i -> DynamicTest.dynamicTest("GetFakeSeed test #" + i, () -> {
                // 插入一个新的假种子
                String seedId = UUID.randomUUID().toString();
                String userId = em.createQuery("SELECT u.userid FROM User u", String.class)
                                  .setMaxResults(1)
                                  .getSingleResult();

                Seed seed = new Seed();
                seed.seedid = seedId;
                seed.seeduserid = userId;
                seed.faketime = 100 + i; // 设置为大于 config.getFakeTime() 的值
                seed.lastfakecheck = new Date();
                seed.outurl = "http://example.com/fake" + i;
                seed.title = "Fake Seed " + i;
                seed.subtitle = "Subtitle " + i;
                seed.seedsize = "100MB";
                seed.seedtag = "test,fake";
                seed.downloadtimes = 0;
                seed.url = "http://example.com/seed" + i;

                em.getTransaction().begin();
                em.persist(seed);
                em.getTransaction().commit();

                // 记录插入的 Seed ID
                insertedSeedIds.add(seedId);

                // 调用 GetFakeSeed 并验证
                Pair<String, String>[] fakeSeeds = cheat.GetFakeSeed();
                Assertions.assertNotNull(fakeSeeds, "GetFakeSeed 应返回非空");

                // 验证返回的假种子列表是否包含所有插入的假种子
                for (String id : insertedSeedIds) {
                    boolean found = Arrays.stream(fakeSeeds)
                                           .anyMatch(pair -> pair.getLeft().equals(id));
                    Assertions.assertTrue(found, "GetFakeSeed 应包含插入的假种子 ID: " + id);
                }
            }))
            .collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testGetPunishedUserList() {
        List<String> insertedUserIds = new ArrayList<>(); // 用于记录插入的用户 ID

        return IntStream.range(0, 10)
            .mapToObj(i -> DynamicTest.dynamicTest("GetPunishedUserList test #" + i, () -> {
                // 插入一个新的用户，accountstate 设置为 true
                String userId = UUID.randomUUID().toString();
                User user = new User();
                user.userid = userId;
                user.email = "test" + i + "@example.com";
                user.username = "TestUser" + i;
                user.password = "password" + i;
                user.sex = "M";
                user.detectedCount = 0;
                user.lastDetectedTime = new Date();
                user.school = "TestSchool";
                user.pictureurl = "http://example.com/avatar" + i;
                user.profile = "Test profile " + i;
                user.accountstate = true; // 设置为 true
                user.invitetimes = 5;

                // 创建并设置 UserPT 实例
                UserPT userPT = new UserPT();
                userPT.user = user; // 关联 User 实例
                // user.userPT = userPT;

                em.getTransaction().begin();
                em.persist(user);
                em.persist(userPT); // 持久化 UserPT 实例
                em.getTransaction().commit();

                // 记录插入的用户 ID
                insertedUserIds.add(userId);

                // 调用 GetPunishedUserList 并验证
                String[] punishedUsers = cheat.GetPunishedUserList();
                Assertions.assertNotNull(punishedUsers, "GetPunishedUserList 应返回非空");

                // 验证返回的用户列表是否包含所有插入的用户
                for (String id : insertedUserIds) {
                    boolean found = Arrays.stream(punishedUsers).anyMatch(returnedId -> returnedId.equals(id));
                    Assertions.assertTrue(found, "GetPunishedUserList 应包含插入的用户 ID: " + id);
                }
            }))
            .collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testPunishUser() {
        List<String> insertedUserIds = new ArrayList<>(); // 用于记录插入的用户 ID

        return IntStream.range(0, 10)
            .mapToObj(i -> DynamicTest.dynamicTest("PunishUser test #" + i, () -> {
                // 配置参数
                int cheatTime = config.getCheatTime();
                int fakeTime = config.getFakeTime();

                // 插入用户 1（作弊用户）
                String userId1 = UUID.randomUUID().toString();
                User user1 = new User();
                user1.userid = userId1;
                user1.email = "cheater" + i + "_" + UUID.randomUUID().toString() + "@example.com"; // 确保唯一性
                user1.username = "Cheater" + i;
                user1.password = "password" + i;
                user1.sex = "M";
                user1.detectedCount = cheatTime + 1; // detectedCount 超过 cheatTime
                user1.fakeDetectedCount = 0;
                user1.lastDetectedTime = new Date();
                user1.fakeLastDetectedTime = new Date();
                user1.accountstate = false; // 初始状态为未封禁
                user1.invitetimes = 5;

                // 创建并设置 UserPT 实例
                UserPT userPT1 = new UserPT();
                userPT1.user = user1; // 关联 User 实例
                // user1.userPT = userPT1;

                // 插入用户 2（非作弊用户）
                String userId2 = UUID.randomUUID().toString();
                User user2 = new User();
                user2.userid = userId2;
                user2.email = "normal" + i + "_" + UUID.randomUUID().toString() + "@example.com"; // 确保唯一性
                user2.username = "NormalUser" + i;
                user2.password = "password" + i;
                user2.sex = "F";
                user2.detectedCount = 0;
                user2.fakeDetectedCount = fakeTime - 1; // fakeDetectedCount 未超过 fakeTime
                user2.lastDetectedTime = new Date();
                user2.fakeLastDetectedTime = new Date();
                user2.accountstate = false; // 初始状态为未封禁
                user2.invitetimes = 5;

                // 创建并设置 UserPT 实例
                UserPT userPT2 = new UserPT();
                userPT2.user = user2; // 关联 User 实例
                // user2.userPT = userPT2;

                em.getTransaction().begin();
                em.persist(user1);
                em.persist(userPT1); // 持久化 UserPT 实例
                em.persist(user2);
                em.persist(userPT2); // 持久化 UserPT 实例
                em.getTransaction().commit();

                // 记录插入的用户 ID
                insertedUserIds.add(userId1);
                insertedUserIds.add(userId2);

                // 调用 PunishUser 方法
                cheat.PunishUser();

                // 验证用户 1 是否被封禁
                User punishedUser1 = em.find(User.class, userId1);
                Assertions.assertNotNull(punishedUser1, "数据库应能查到用户 1");
                Assertions.assertTrue(punishedUser1.accountstate, "作弊用户应被封禁");

                // 验证用户 2 是否未被封禁
                User punishedUser2 = em.find(User.class, userId2);
                Assertions.assertNotNull(punishedUser2, "数据库应能查到用户 2");
                Assertions.assertFalse(punishedUser2.accountstate, "非作弊用户不应被封禁");
            }))
            .collect(Collectors.toList());
    }

    // @TestFactory
    // Collection<DynamicTest> testDetectTrans() {
    //     List<String> insertedUserIds = new ArrayList<>(); // 用于记录插入的用户 ID
    //     List<String> insertedSeedIds = new ArrayList<>(); // 用于记录插入的种子 ID
    //     List<String> insertedTransIds = new ArrayList<>(); // 用于记录插入的传输记录 ID

    //     return IntStream.range(0, 10)
    //         .mapToObj(i -> DynamicTest.dynamicTest("DetectTrans test #" + i, () -> {
    //             // 插入上传用户
    //             String uploaderId = UUID.randomUUID().toString();
    //             User uploader = new User();
    //             uploader.userid = uploaderId;
    //             uploader.email = "uploader" + i + "_" + UUID.randomUUID().toString() + "@example.com"; // 确保唯一性
    //             uploader.username = "Uploader" + i;
    //             uploader.password = "password" + i;
    //             uploader.sex = "M";
    //             uploader.detectedCount = 0; // 初始 detectedCount 为 0
    //             uploader.fakeDetectedCount = 0;
    //             uploader.lastDetectedTime = new Date();
    //             uploader.fakeLastDetectedTime = new Date();
    //             uploader.accountstate = false;
    //             uploader.invitetimes = 5;

    //             // 创建并设置 UserPT 实例
    //             UserPT uploaderPT = new UserPT();
    //             uploaderPT.user = uploader; // 关联 User 实例
    //             uploader.userPT = uploaderPT;

    //             // 插入下载用户
    //             String downloaderId = UUID.randomUUID().toString();
    //             User downloader = new User();
    //             downloader.userid = downloaderId;
    //             downloader.email = "downloader" + i + "_" + UUID.randomUUID().toString() + "@example.com"; // 确保唯一性
    //             downloader.username = "Downloader" + i;
    //             downloader.password = "password" + i;
    //             downloader.sex = "F";
    //             downloader.detectedCount = 0;
    //             downloader.fakeDetectedCount = 0;
    //             downloader.lastDetectedTime = new Date();
    //             downloader.fakeLastDetectedTime = new Date();
    //             downloader.accountstate = false;
    //             downloader.invitetimes = 5;

    //             // 创建并设置 UserPT 实例
    //             UserPT downloaderPT = new UserPT();
    //             downloaderPT.user = downloader; // 关联 User 实例
    //             downloader.userPT = downloaderPT;

    //             em.getTransaction().begin();
    //             em.persist(uploader);
    //             em.persist(uploaderPT); // 持久化 UserPT 实例
    //             em.persist(downloader);
    //             em.persist(downloaderPT); // 持久化 UserPT 实例
    //             em.getTransaction().commit();
    //             insertedUserIds.add(uploaderId);
    //             insertedUserIds.add(downloaderId);

    //             // 插入种子
    //             String seedId = UUID.randomUUID().toString();
    //             Seed seed = new Seed();
    //             seed.seedid = seedId;
    //             seed.seeduserid = uploaderId;
    //             seed.faketime = 0;
    //             seed.lastfakecheck = new Date();
    //             seed.outurl = "http://example.com/seed" + i;
    //             seed.title = "Seed " + i;
    //             seed.subtitle = "Subtitle " + i;
    //             seed.seedsize = "100MB";
    //             seed.seedtag = "test";
    //             seed.downloadtimes = 0;
    //             seed.url = "http://example.com/seed" + i;

    //             em.getTransaction().begin();
    //             em.persist(seed);
    //             em.getTransaction().commit();
    //             insertedSeedIds.add(seedId);

    //             // 插入正常和异常的传输记录
    //             List<TransRecord> transRecords = new ArrayList<>();
    //             for (int j = 0; j < 15; j++) {
    //                 TransRecord transRecord = new TransRecord();
    //                 transRecord.taskid = UUID.randomUUID().toString();
    //                 transRecord.uploaduserid = uploaderId;
    //                 transRecord.downloaduserid = downloaderId; // 确保 downloader_id 存在于 User 表中
    //                 transRecord.seedid = seedId; // 确保 seed_id 存在于 Seed 表中
    //                 transRecord.upload = (j < 13) ? 100 : 1000; // 前 3 条为正常数据，后 2 条为异常数据
    //                 transRecord.download = (j < 13) ? 90 : 10; // 异常数据的上传量远大于下载量
    //                 transRecord.maxupload = 200;
    //                 transRecord.maxdownload = 200;

    //                 em.getTransaction().begin();
    //                 em.persist(transRecord);
    //                 em.getTransaction().commit();
    //                 insertedTransIds.add(transRecord.taskid);
    //                 transRecords.add(transRecord);
    //             }

    //             // 调用 DetectTrans 方法
    //             cheat.DetectTrans();

    //             // 验证下载用户的 detectedCount 是否增加
    //             User detectedDownloader = em.find(User.class, downloaderId);
    //             Assertions.assertNotNull(detectedDownloader, "数据库应能查到上传用户");
    //             Assertions.assertEquals(2, detectedDownloader.detectedCount, "上传用户的 detectedCount 应增加 2（对应 2 条异常数据）");
    //         }))
    //         .collect(Collectors.toList());
    // }
}
