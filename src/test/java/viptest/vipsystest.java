package viptest;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
import entity.VipSeed;
import vip.Vip;

public class vipsystest {
    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static Vip vip;
    private static final int TEST_CASES = 10;

    @BeforeAll
    public static void setup() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        java.util.Map<String,Object> props = new HashMap<>();
        String jdbcUrl = String.format(
            "jdbc:mysql://%s/%s?useSSL=false&serverTimezone=UTC",
            entity.config.SqlURL, entity.config.TestDatabase);
        props.put("javax.persistence.jdbc.url", jdbcUrl);
        props.put("javax.persistence.jdbc.user", entity.config.SqlUsername);
        props.put("javax.persistence.jdbc.password", entity.config.SqlPassword);
        props.put("javax.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
        emf = Persistence.createEntityManagerFactory("myPersistenceUnit", props);
        em = emf.createEntityManager();
        vip = new Vip();
        java.lang.reflect.Field f = Vip.class.getDeclaredField("entitymanager");
        f.setAccessible(true);
        f.set(vip, em);
    }

    @AfterAll
    public static void teardown() {
        if (em != null && em.isOpen()) em.close();
        if (emf != null && emf.isOpen()) emf.close();
    }

    @TestFactory
    Collection<DynamicTest> testAddFarmerNumber() {
        Random rnd = new Random();
        return IntStream.range(0, TEST_CASES)
            .mapToObj(i -> DynamicTest.dynamicTest("AddFarmerNumber test #" + i, () -> {
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                String userId = "testuser-" + UUID.randomUUID();
                if (userId.length() > 36) userId = userId.substring(0, 36);
                String seedid = "testseed-" + UUID.randomUUID();
                try {
                    // 插入User
                    entity.User user = new entity.User();
                    user.userid = userId;
                    user.email = userId + "@example.com";
                    user.username = userId;
                    user.password = "pwd";
                    user.sex = "m";
                    user.school = "school";
                    user.pictureurl = null;
                    user.profile = null;
                    user.accountstate = false;
                    user.invitetimes = 5;
                    
                    // 插入UserPT
                    entity.UserPT userPT = new entity.UserPT();
                    userPT.userid = userId;
                    userPT.magic = 0;
                    userPT.upload = 0L;
                    userPT.download = 0L;
                    userPT.share = 0.0;
                    userPT.farmurl = null;
                    userPT.viptime = 0;
                    userPT.user = user;
                    // user.userPT = userPT;
                    em.persist(user);
                    em.persist(userPT);
                    // 插入Seed
                    entity.Seed seed = new entity.Seed();
                    seed.seedid = seedid;
                    seed.seeduserid = userId;
                    seed.faketime = 0;
                    seed.lastfakecheck = new java.util.Date();
                    seed.outurl = "http://example.com/seed";
                    seed.title = "title";
                    seed.subtitle = "subtitle";
                    seed.seedsize = "100MB";
                    seed.seedtag = "tag";
                    seed.downloadtimes = 0;
                    seed.url = "http://download.com/seed";
                    em.persist(seed);
                    // 插入VipSeed
                    VipSeed vs = new VipSeed();
                    vs.seedid = seedid;
                    vs.farmernumber = rnd.nextInt(10) + 1;
                    vs.seedercount = 0;
                    vs.rewardmagic = 0;
                    vs.stopcaching = 0;
                    vs.bonus = 0;
                    vs.cachestate = false;
                    vs.seed = seed;
                    em.persist(vs);
                    em.flush();
                    int delta = rnd.nextInt(100) + 1;
                    Integer before = em.createQuery(
                        "select v.farmernumber from VipSeed v where v.seedid = :sid", Integer.class)
                        .setParameter("sid", seedid).getSingleResult();
                    int ret = vip.AddFarmerNumber(delta, seedid);
                    Assertions.assertEquals(0, ret, "AddFarmerNumber 应返回0");
                    em.flush();
                    em.clear();
                    Integer after = em.createQuery(
                        "select v.farmernumber from VipSeed v where v.seedid = :sid", Integer.class)
                        .setParameter("sid", seedid).getSingleResult();
                    Assertions.assertEquals(Integer.valueOf(before + delta), after, "farmernumber 应增加");
                } finally {
                    tx.rollback();
                }
            })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testReduceFarmerNumber() {
        Random rnd = new Random();
        return IntStream.range(0, TEST_CASES)
            .mapToObj(i -> DynamicTest.dynamicTest("ReduceFarmerNumber test #" + i, () -> {
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                String userId = "testuser-" + UUID.randomUUID();
                if (userId.length() > 36) userId = userId.substring(0, 36);
                String seedid = "testseed-" + UUID.randomUUID();
                try {
                    entity.User user = new entity.User();
                    user.userid = userId;
                    user.email = userId + "@example.com";
                    user.username = userId;
                    user.password = "pwd";
                    user.sex = "m";
                    user.school = "school";
                    user.pictureurl = null;
                    user.profile = null;
                    user.accountstate = false;
                    user.invitetimes = 5;
                    
                    entity.UserPT userPT = new entity.UserPT();
                    userPT.userid = userId;
                    userPT.magic = 0;
                    userPT.upload = 0L;
                    userPT.download = 0L;
                    userPT.share = 0.0;
                    userPT.farmurl = null;
                    userPT.viptime = 0;
                    userPT.user = user;
                    // user.userPT = userPT;
                    em.persist(user);
                    em.persist(userPT);
                    entity.Seed seed = new entity.Seed();
                    seed.seedid = seedid;
                    seed.seeduserid = userId;
                    seed.faketime = 0;
                    seed.lastfakecheck = new java.util.Date();
                    seed.outurl = "http://example.com/seed";
                    seed.title = "title";
                    seed.subtitle = "subtitle";
                    seed.seedsize = "100MB";
                    seed.seedtag = "tag";
                    seed.downloadtimes = 0;
                    seed.url = "http://download.com/seed";
                    em.persist(seed);
                    VipSeed vs = new VipSeed();
                    vs.seedid = seedid;
                    vs.farmernumber = rnd.nextInt(10) + 5;
                    vs.seedercount = 0;
                    vs.rewardmagic = 0;
                    vs.stopcaching = 0;
                    vs.bonus = 0;
                    vs.cachestate = false;
                    vs.seed = seed;
                    em.persist(vs);
                    em.flush();
                    Integer before = em.createQuery(
                        "select v.farmernumber from VipSeed v where v.seedid = :sid", Integer.class)
                        .setParameter("sid", seedid).getSingleResult();
                    int max = Math.max(before, 0);
                    int delta = max > 0 ? rnd.nextInt(max) + 1 : 0;
                    if (delta == 0) return;
                    int ret = vip.ReduceFarmerNumber(delta, seedid);
                    Assertions.assertEquals(0, ret, "ReduceFarmerNumber 应返回0");
                    em.flush();
                    em.clear();
                    Integer after = em.createQuery(
                        "select v.farmernumber from VipSeed v where v.seedid = :sid", Integer.class)
                        .setParameter("sid", seedid).getSingleResult();
                    Assertions.assertEquals(Integer.valueOf(before - delta), after, "farmernumber 应减少");
                } finally {
                    tx.rollback();
                }
            })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testRemoveSeed() {
        return IntStream.range(0, TEST_CASES)
            .mapToObj(i -> DynamicTest.dynamicTest("RemoveSeed test #" + i, () -> {
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                String userId = "testuser-" + UUID.randomUUID();
                if (userId.length() > 36) userId = userId.substring(0, 36);
                String seedid = "testseed-" + UUID.randomUUID();
                try {
                    entity.User user = new entity.User();
                    user.userid = userId;
                    user.email = userId + "@example.com";
                    user.username = userId;
                    user.password = "pwd";
                    user.sex = "m";
                    user.school = "school";
                    user.pictureurl = null;
                    user.profile = null;
                    user.accountstate = false;
                    user.invitetimes = 5;
                    
                    entity.UserPT userPT = new entity.UserPT();
                    userPT.userid = userId;
                    userPT.magic = 0;
                    userPT.upload = 0L;
                    userPT.download = 0L;
                    userPT.share = 0.0;
                    userPT.farmurl = null;
                    userPT.viptime = 0;
                    userPT.user = user;
                    // user.userPT = userPT;
                    em.persist(user);
                    em.persist(userPT);
                    entity.Seed seed = new entity.Seed();
                    seed.seedid = seedid;
                    seed.seeduserid = userId;
                    seed.faketime = 0;
                    seed.lastfakecheck = new java.util.Date();
                    seed.outurl = "http://example.com/seed";
                    seed.title = "title";
                    seed.subtitle = "subtitle";
                    seed.seedsize = "100MB";
                    seed.seedtag = "tag";
                    seed.downloadtimes = 0;
                    seed.url = "http://download.com/seed";
                    em.persist(seed);
                    VipSeed vs = new VipSeed();
                    vs.seedid = seedid;
                    vs.farmernumber = 5;
                    vs.seedercount = 0;
                    vs.rewardmagic = 0;
                    vs.stopcaching = 0;
                    vs.bonus = 0;
                    vs.cachestate = false;
                    vs.seed = seed;
                    em.persist(vs);
                    em.flush();
                    VipSeed before = em.find(VipSeed.class, seedid);
                    Assertions.assertNotNull(before, "VipSeed 应存在");
                    int ret = vip.RemoveSeed(seedid);
                    Assertions.assertEquals(0, ret, "RemoveSeed 应返回0");
                    em.flush();
                    em.clear();
                    VipSeed after = em.find(VipSeed.class, seedid);
                    Assertions.assertNull(after, "VipSeed 应被移除");
                } finally {
                    tx.rollback();
                }
            })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testCheckSeed() {
        return IntStream.range(0, TEST_CASES)
            .mapToObj(i -> DynamicTest.dynamicTest("CheckSeed test #" + i, () -> {
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                String userId = "testuser-" + UUID.randomUUID();
                if (userId.length() > 36) userId = userId.substring(0, 36);
                String seedid = "testseed-" + UUID.randomUUID();
                try {
                    entity.User user = new entity.User();
                    user.userid = userId;
                    user.email = userId + "@example.com";
                    user.username = userId;
                    user.password = "pwd";
                    user.sex = "m";
                    user.school = "school";
                    user.pictureurl = null;
                    user.profile = null;
                    user.accountstate = false;
                    user.invitetimes = 5;
                    
                    entity.UserPT userPT = new entity.UserPT();
                    userPT.userid = userId;
                    userPT.magic = 0;
                    userPT.upload = 0L;
                    userPT.download = 0L;
                    userPT.share = 0.0;
                    userPT.farmurl = null;
                    userPT.viptime = 0;
                    userPT.user = user;
                    // user.userPT = userPT;
                    em.persist(user);
                    em.persist(userPT);
                    entity.Seed seed = new entity.Seed();
                    seed.seedid = seedid;
                    seed.seeduserid = userId;
                    seed.faketime = 0;
                    seed.lastfakecheck = new java.util.Date();
                    seed.outurl = "http://example.com/seed";
                    seed.title = "title";
                    seed.subtitle = "subtitle";
                    seed.seedsize = "100MB";
                    seed.seedtag = "tag";
                    seed.downloadtimes = 0;
                    seed.url = "http://download.com/seed";
                    em.persist(seed);
                    VipSeed vs = new VipSeed();
                    vs.seedid = seedid;
                    vs.farmernumber = 1000;
                    vs.seedercount = 0;
                    vs.rewardmagic = 0;
                    vs.stopcaching = 0;
                    vs.bonus = 0;
                    vs.cachestate = false;
                    vs.seed = seed;
                    em.persist(vs);
                    em.flush();
                    VipSeed vipSeed = em.find(VipSeed.class, seedid);
                    Assertions.assertNotNull(vipSeed, "VipSeed 应存在");
                    vip.CheckSeed();
                    em.flush();
                    em.clear();
                    VipSeed after = em.find(VipSeed.class, seedid);
                    Assertions.assertNull(after, "CheckSeed后超限VipSeed应被移除");
                } finally {
                    tx.rollback();
                }
            })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testGetSeedToPlant() {
        return IntStream.range(0, TEST_CASES)
            .mapToObj(i -> DynamicTest.dynamicTest("GetSeedToPlant test #" + i, () -> {
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                String userId = "testuser-" + UUID.randomUUID();
                if (userId.length() > 36) userId = userId.substring(0, 36);
                String seedid = "testseed-" + UUID.randomUUID();
                try {
                    entity.User user = new entity.User();
                    user.userid = userId;
                    user.email = userId + "@example.com";
                    user.username = userId;
                    user.password = "pwd";
                    user.sex = "m";
                    user.school = "school";
                    user.pictureurl = null;
                    user.profile = null;
                    user.accountstate = false;
                    user.invitetimes = 5;
                    
                    entity.UserPT userPT = new entity.UserPT();
                    userPT.userid = userId;
                    userPT.magic = 0;
                    userPT.upload = 0L;
                    userPT.download = 0L;
                    userPT.share = 0.0;
                    userPT.farmurl = null;
                    userPT.viptime = 0;
                    userPT.user = user;
                    // user.userPT = userPT;
                    em.persist(user);
                    em.persist(userPT);
                    entity.Seed seed = new entity.Seed();
                    seed.seedid = seedid;
                    seed.seeduserid = userId;
                    seed.faketime = 0;
                    seed.lastfakecheck = new java.util.Date();
                    seed.outurl = "http://example.com/seed";
                    seed.title = "title";
                    seed.subtitle = "subtitle";
                    seed.seedsize = "100MB";
                    seed.seedtag = "tag";
                    seed.downloadtimes = 0;
                    seed.url = "http://download.com/seed";
                    em.persist(seed);
                    VipSeed vs = new VipSeed();
                    vs.seedid = seedid;
                    vs.farmernumber = 5;
                    vs.seedercount = 0;
                    vs.rewardmagic = 0;
                    vs.stopcaching = 0;
                    vs.bonus = 0;
                    vs.cachestate = false;
                    vs.seed = seed;
                    em.persist(vs);
                    em.flush();
                    VipSeed vipSeed = em.find(VipSeed.class, seedid);
                    Assertions.assertNotNull(vipSeed, "VipSeed 应存在");
                    Seed[] seeds = vip.GetSeedToPlant();
                    boolean found = Arrays.stream(seeds).anyMatch(s -> s.seedid.equals(seedid));
                    Assertions.assertTrue(found, "GetSeedToPlant 返回的种子应包含当前seedid");
                } finally {
                    tx.rollback();
                }
            })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testGetTTorent() {
        return IntStream.range(0, TEST_CASES)
            .mapToObj(i -> DynamicTest.dynamicTest("GetTTorent test #" + i, () -> {
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                String userId = "testuser-" + UUID.randomUUID();
                if (userId.length() > 36) userId = userId.substring(0, 36);
                String seedid = "testseed-" + UUID.randomUUID();
                String ip = "127.0.0." + i;
                java.io.File tempFile = null;
                try {
                    // 正常情况：用户、种子、文件都存在
                    entity.User user = new entity.User();
                    user.userid = userId;
                    user.email = userId + "@example.com";
                    user.username = userId;
                    user.password = "pwd";
                    user.sex = "m";
                    user.school = "school";
                    user.pictureurl = null;
                    user.profile = null;
                    user.accountstate = false;
                    user.invitetimes = 5;
                    entity.UserPT userPT = new entity.UserPT();
                    userPT.userid = userId;
                    userPT.magic = 0;
                    userPT.upload = 0L;
                    userPT.download = 0L;
                    userPT.share = 0.0;
                    userPT.farmurl = null;
                    userPT.viptime = 0;
                    userPT.user = user;
                    // user.userPT = userPT;
                    em.persist(user);
                    em.persist(userPT);
                    entity.Seed seed = new entity.Seed();
                    seed.seedid = seedid;
                    seed.seeduserid = userId;
                    seed.faketime = 0;
                    seed.lastfakecheck = new java.util.Date();
                    seed.outurl = "http://example.com/seed";
                    seed.title = "title";
                    seed.subtitle = "subtitle";
                    seed.seedsize = "100MB";
                    seed.seedtag = "tag";
                    seed.downloadtimes = 0;
                    em.persist(seed);
                    VipSeed vs = new VipSeed();
                    vs.seedid = seedid;
                    vs.farmernumber = 1;
                    vs.seedercount = 0;
                    vs.rewardmagic = 0;
                    vs.stopcaching = 0;
                    vs.bonus = 0;
                    vs.cachestate = false;
                    vs.seed = seed;
                    em.persist(vs);
                    em.flush();
                    tempFile = java.io.File.createTempFile(seedid + "_test", ".torrent");
                    seed.url = tempFile.getAbsolutePath();
                    em.merge(seed);
                    em.flush();
                    org.apache.commons.lang3.tuple.Pair<java.io.File, Integer> result = vip.GetTTorent(seedid, userId, ip);
                    Assertions.assertNotNull(result, "GetTTorent 返回结果不应为null");
                    Assertions.assertEquals(0, result.getRight().intValue(), "GetTTorent 应返回0");
                    Assertions.assertNotNull(result.getLeft(), "GetTTorent 应返回文件对象");
                    Assertions.assertEquals(tempFile.getAbsolutePath(), result.getLeft().getAbsolutePath(), "文件路径应一致");

                    // 情况2：种子不存在
                    org.apache.commons.lang3.tuple.Pair<java.io.File, Integer> result2 = vip.GetTTorent("notexist-seed", userId, ip);
                    Assertions.assertNotNull(result2, "种子不存在时返回不应为null");
                    Assertions.assertNull(result2.getLeft(), "种子不存在时文件应为null");
                    Assertions.assertNotEquals(0, result2.getRight().intValue(), "种子不存在应返回非0");

                    // 情况3：用户不存在
                    org.apache.commons.lang3.tuple.Pair<java.io.File, Integer> result3 = vip.GetTTorent(seedid, "notexist-user", ip);
                    Assertions.assertNotNull(result3, "用户不存在时返回不应为null");
                    Assertions.assertNull(result3.getLeft(), "用户不存在时文件应为null");
                    Assertions.assertNotEquals(0, result3.getRight().intValue(), "用户不存在应返回非0");

                    // 情况4：文件路径不存在
                    seed.url = null;
                    em.merge(seed);
                    em.flush();
                    org.apache.commons.lang3.tuple.Pair<java.io.File, Integer> result4 = vip.GetTTorent(seedid, userId, ip);
                    Assertions.assertNotNull(result4, "文件不存在时返回不应为null");
                    Assertions.assertNull(result4.getLeft(), "文件不存在时文件应为null");
                    Assertions.assertNotEquals(0, result4.getRight().intValue(), "文件不存在应返回非0");

                    // 情况5：参数为空
                    org.apache.commons.lang3.tuple.Pair<java.io.File, Integer> result5 = vip.GetTTorent(null, userId, ip);
                    Assertions.assertNotNull(result5, "参数为空时返回不应为null");
                    Assertions.assertNull(result5.getLeft(), "参数为空时文件应为null");
                    Assertions.assertNotEquals(0, result5.getRight().intValue(), "参数为空应返回非0");
                } finally {
                    tx.rollback();
                    if (tempFile != null && tempFile.exists()) tempFile.delete();
                }
            })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testKeepSeed() {
        return IntStream.range(0, TEST_CASES)
            .mapToObj(i -> DynamicTest.dynamicTest("KeepSeed test #" + i, () -> {
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                String userId = "user-" + UUID.randomUUID();
                if (userId.length() > 36) userId = userId.substring(0, 36);
                String seedid = "testseed-" + UUID.randomUUID();
                try {
                    // 插入User
                    entity.User user = new entity.User();
                    user.userid = userId;
                    user.email = userId + "@example.com";
                    user.username = userId;
                    user.password = "pwd";
                    user.sex = "m";
                    user.school = "school";
                    user.pictureurl = null;
                    user.profile = null;
                    user.accountstate = false;
                    user.invitetimes = 5;
                    // 插入UserPT
                    entity.UserPT userPT = new entity.UserPT();
                    userPT.userid = userId;
                    userPT.magic = 0;
                    userPT.upload = 0L;
                    userPT.download = 0L;
                    userPT.share = 0.0;
                    userPT.farmurl = null;
                    userPT.viptime = 0;
                    userPT.user = user;
                    // user.userPT = userPT;
                    em.persist(user);
                    em.persist(userPT);
                    // 插入Seed
                    entity.Seed seed = new entity.Seed();
                    seed.seedid = seedid;
                    seed.seeduserid = userId;
                    seed.faketime = 0;
                    seed.lastfakecheck = new java.util.Date();
                    seed.outurl = "http://example.com/seed";
                    seed.title = "title";
                    seed.subtitle = "subtitle";
                    seed.seedsize = "100MB";
                    seed.seedtag = "tag";
                    seed.downloadtimes = 0;
                    seed.url = "http://download.com/seed";
                    em.persist(seed);
                    em.flush();
                    int ret = vip.KeepSeed(seedid);
                    Assertions.assertEquals(0, ret, "KeepSeed 正常插入应返回0");
                    VipSeed vs = em.find(VipSeed.class, seedid);
                    Assertions.assertNotNull(vs, "VipSeed 应被插入");

                    // 异常情况：种子不存在
                    String notExistSeedId = "notexist-seed-" + UUID.randomUUID();
                    int ret2 = vip.KeepSeed(notExistSeedId);
                    Assertions.assertEquals(1, ret2, "种子不存在应返回1");
                } finally {
                    tx.rollback();
                }
            })).collect(Collectors.toList());
    }
}
