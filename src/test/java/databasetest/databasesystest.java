package databasetest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import entity.UserInvite;
import entity.QUserInvite;
import database.Database1;
import entity.User;
import entity.config;

public class databasesystest {
    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static Database1 db;
    private static List<String> testUserIds;
    private static List<String> testEmails;

    @BeforeAll
    static void setup() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        config cfg = new config();
        Map<String, Object> props = new HashMap<>();
        String jdbcUrl = String.format(
                "jdbc:mysql://%s/%s?useSSL=false&serverTimezone=UTC",
                cfg.SqlURL, cfg.TestDatabase);
        props.put("javax.persistence.jdbc.url", jdbcUrl);
        props.put("javax.persistence.jdbc.user", cfg.SqlUsername);
        props.put("javax.persistence.jdbc.password", cfg.SqlPassword);
        props.put("javax.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
        emf = Persistence.createEntityManagerFactory("myPersistenceUnit", props);
        em = emf.createEntityManager();
        db = new Database1();
        java.lang.reflect.Field f = Database1.class.getDeclaredField("entitymanager");
        f.setAccessible(true);
        f.set(db, em);
        testUserIds = new ArrayList<>();
        testEmails = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String id = "test_user_" + i + "_" + UUID.randomUUID();
            if (id.length() > 36) id = id.substring(0, 36);
            testUserIds.add(id);
            testEmails.add("test_email_" + i + "@example.com");
        }
    }

    @AfterAll
    static void teardown() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        for (String uid : testUserIds) {
            User found = em.find(User.class, uid);
            if (found != null) em.remove(found);
        }
        tx.commit();
        if (em != null && em.isOpen()) em.close();
        if (emf != null && emf.isOpen()) emf.close();
    }

    @TestFactory
    Collection<DynamicTest> testRegisterUser() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("RegisterUser test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 1. 创建一些用户并将其保存到数据库中
                List<User> existingUsers = new ArrayList<>();
                for (int j = 0; j < 3; j++) {
                    User existingUser = new User();
                    String userId = "existing_user_" + i + "_" + j;
                    if (userId.length() > 36) userId = userId.substring(0, 36);
                    existingUser.userid = userId;
                    existingUser.email = "existing_email_" + i + "_" + j + "@example.com";
                    existingUser.username = "existing_user" + i + "_" + j;
                    existingUser.password = "pwd" + i;
                    existingUser.sex = "m";
                    existingUser.school = "school" + i;
                    existingUser.pictureurl = null;
                    existingUser.profile = null;
                    existingUser.accountstate = false;
                    existingUser.invitetimes = 5;
                    
                    entity.UserPT existingUserPT = new entity.UserPT();
                    existingUserPT.userid = existingUser.userid;
                    existingUserPT.magic = 0;
                    existingUserPT.upload = 0L;
                    existingUserPT.download = 0L;
                    existingUserPT.share = 0.0;
                    existingUserPT.farmurl = null;
                    existingUserPT.viptime = 0;
                    existingUserPT.user = existingUser;
                    // existingUser.userPT = existingUserPT;
                    
                    em.persist(existingUser);
                    existingUsers.add(existingUser);
                }
                em.flush();
                
                // 2. 创建邀请记录 - 一些被邀请的邮箱和一些未被邀请的邮箱
                // 被邀请的邮箱列表
                List<String> invitedEmails = new ArrayList<>();
                for (int j = 0; j < 3; j++) {
                    // 生成邀请记录
                    String invitedEmail = "invited_" + i + "_" + j + "@example.com";
                    invitedEmails.add(invitedEmail);
                    
                    UserInvite invite = new UserInvite();
                    invite.userId = existingUsers.get(j % existingUsers.size()).userid; // 使用现有用户作为邀请人
                    invite.inviterEmail = invitedEmail;
                    invite.inviterRegistered = false;
                    em.persist(invite);
                }
                em.flush();
                
                // 非邀请邮箱
                List<String> uninvitedEmails = new ArrayList<>();
                for (int j = 0; j < 2; j++) {
                    uninvitedEmails.add("uninvited_" + i + "_" + j + "@example.com");
                }
                
                // 3. 测试被邀请且首次注册的用户 - 应该成功 (返回0)
                User invitedUser = new User();
                String invitedUserId = testUserIds.get(i);
                if (invitedUserId.length() > 36) invitedUserId = invitedUserId.substring(0, 36);
                invitedUser.userid = invitedUserId;
                invitedUser.email = invitedEmails.get(0); // 使用被邀请的邮箱
                invitedUser.username = "invited_user" + i;
                invitedUser.password = "pwd" + i;
                invitedUser.sex = "m";
                invitedUser.school = "school" + i;
                invitedUser.pictureurl = null;
                invitedUser.profile = null;
                invitedUser.accountstate = false;
                invitedUser.invitetimes = 5;
                
                entity.UserPT invitedUserPT = new entity.UserPT();
                invitedUserPT.userid = invitedUser.userid;
                invitedUserPT.magic = 0;
                invitedUserPT.upload = 0L;
                invitedUserPT.download = 0L;
                invitedUserPT.share = 0.0;
                invitedUserPT.farmurl = null;
                invitedUserPT.viptime = 0;
                invitedUserPT.user = invitedUser;
                // invitedUser.userPT = invitedUserPT;
                
                int ret1 = db.RegisterUser(invitedUser);
                Assertions.assertEquals(0, ret1, "RegisterUser should return 0 for invited user's first registration");
                
                // 检查用户是否已保存到数据库
                User checkUser = em.find(User.class, invitedUserId);
                Assertions.assertNotNull(checkUser, "User should be saved to database when registration succeeds");
                Assertions.assertEquals(invitedUser.email, checkUser.email);
                
                // 4. 测试被邀请但重复注册的用户 - 应该失败 (返回1)
                User duplicateUser = new User();
                String duplicateUserId = "duplicate_" + i;
                if (duplicateUserId.length() > 36) duplicateUserId = duplicateUserId.substring(0, 36);
                duplicateUser.userid = duplicateUserId;
                duplicateUser.email = invitedEmails.get(0); // 重复使用同一个邮箱
                duplicateUser.username = "duplicate_user" + i;
                duplicateUser.password = "pwd_dup" + i;
                duplicateUser.sex = "f";
                duplicateUser.school = "school_dup" + i;
                duplicateUser.pictureurl = null;
                duplicateUser.profile = null;
                duplicateUser.accountstate = false;
                duplicateUser.invitetimes = 5;
                
                int ret2 = db.RegisterUser(duplicateUser);
                Assertions.assertEquals(1, ret2, "RegisterUser should return 1 for duplicate email");
                
                // 5. 测试未被邀请的用户 - 应该失败 (返回2)
                User uninvitedUser = new User();
                String uninvitedUserId = "uninvited_" + i;
                if (uninvitedUserId.length() > 36) uninvitedUserId = uninvitedUserId.substring(0, 36);
                uninvitedUser.userid = uninvitedUserId;
                uninvitedUser.email = uninvitedEmails.get(0); // 使用未被邀请的邮箱
                uninvitedUser.username = "uninvited_user" + i;
                uninvitedUser.password = "pwd_uninv" + i;
                uninvitedUser.sex = "m";
                uninvitedUser.school = "school_uninv" + i;
                uninvitedUser.pictureurl = null;
                uninvitedUser.profile = null;
                uninvitedUser.accountstate = false;
                uninvitedUser.invitetimes = 5;
                
                int ret3 = db.RegisterUser(uninvitedUser);
                Assertions.assertEquals(2, ret3, "RegisterUser should return 2 for uninvited email");
                
            } finally {
                // 确保回滚所有更改，不影响真实数据库
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testUpdateInformation() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("UpdateInformation test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 先插入一个用户
                User user = new User();
                String userId = testUserIds.get(i);
                if (userId.length() > 36) userId = userId.substring(0, 36);
                user.userid = userId;
                user.email = testEmails.get(i);
                user.username = "user" + i;
                user.password = "pwd" + i;
                user.sex = "m";
                user.school = "school" + i;
                user.pictureurl = null;
                user.profile = null;
                user.accountstate = false;
                user.invitetimes = 5;
                entity.UserPT userPT = new entity.UserPT();
                userPT.userid = user.userid;
                userPT.magic = 0;
                userPT.upload = 0L;
                userPT.download = 0L;
                userPT.share = 0.0;
                userPT.farmurl = null;
                userPT.viptime = 0;
                userPT.user = user;
                // user.userPT = userPT;
                em.persist(user);
                em.flush();
                // 更新信息
                User updated = new User();
                updated.userid = userId;
                updated.email = testEmails.get(i);
                updated.username = "user_updated" + i;
                updated.password = "newpassword" + i;
                updated.sex = "f";
                updated.school = "MIT" + i;
                updated.pictureurl = "https://cdn.example.com/avatars/user_new" + i + ".jpg";
                updated.profile = "Updated profile " + i;
                updated.accountstate = true;
                updated.invitetimes = 10 + i;
                int ret = db.UpdateInformation(updated);
                Assertions.assertEquals(0, ret, "UpdateInformation should return 0 for success");
                em.flush();
                em.clear();
                User after = em.find(User.class, userId);
                Assertions.assertEquals("user_updated" + i, after.username);
                Assertions.assertEquals("newpassword" + i, after.password);
                Assertions.assertEquals("f", after.sex);
                Assertions.assertEquals("MIT" + i, after.school);
                Assertions.assertEquals("https://cdn.example.com/avatars/user_new" + i + ".jpg", after.pictureurl);
                Assertions.assertEquals("Updated profile " + i, after.profile);
                Assertions.assertTrue(after.accountstate);
                Assertions.assertEquals(10 + i, after.invitetimes);
                // 测试不存在用户
                User notExist = new User();
                notExist.userid = "not_exist_user_" + i;
                notExist.email = "not_exist" + i + "@example.com";
                notExist.username = "not_exist";
                notExist.password = "not_exist";
                notExist.sex = "m";
                notExist.school = "not_exist";
                notExist.pictureurl = null;
                notExist.profile = null;
                notExist.accountstate = false;
                notExist.invitetimes = 0;
                int ret2 = db.UpdateInformation(notExist);
                Assertions.assertEquals(1, ret2, "UpdateInformation should return 1 for not found user");
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testGetInformation() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("GetInformation test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 先插入一个用户（补充userPT字段，避免not-null property异常）
                User user = new User();
                user.userid = testUserIds.get(i);
                if (user.userid.length() > 36) user.userid = user.userid.substring(0, 36);
                user.email = testEmails.get(i);
                user.username = "user" + i;
                user.password = "pwd" + i;
                user.sex = "m";
                user.school = "school" + i;
                user.pictureurl = "pic" + i + ".jpg";
                user.profile = "profile" + i;
                user.accountstate = false;
                user.invitetimes = 5;
                entity.UserPT userPT = new entity.UserPT();
                userPT.userid = user.userid;
                userPT.magic = 0;
                userPT.upload = 0L;
                userPT.download = 0L;
                userPT.share = 0.0;
                userPT.farmurl = null;
                userPT.viptime = 0;
                userPT.user = user;
                // user.userPT = userPT;
                em.persist(user);
                em.flush();
                // 查询
                User got = db.GetInformation(testUserIds.get(i));
                Assertions.assertNotNull(got, "GetInformation should return user");
                Assertions.assertEquals(user.userid, got.userid);
                Assertions.assertEquals(user.email, got.email);
                Assertions.assertEquals(user.username, got.username);
                Assertions.assertEquals(user.password, got.password);
                Assertions.assertEquals(user.sex, got.sex);
                Assertions.assertEquals(user.school, got.school);
                Assertions.assertEquals(user.pictureurl, got.pictureurl);
                Assertions.assertEquals(user.profile, got.profile);
                Assertions.assertEquals(user.accountstate, got.accountstate);
                Assertions.assertEquals(user.invitetimes, got.invitetimes);
                // 查询不存在用户
                User notExist = db.GetInformation("not_exist_" + i);
                Assertions.assertNull(notExist, "GetInformation should return null for not found user");
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testGetInformationPT() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("GetInformationPT test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 先插入User
                User user = new User();
                String userId = testUserIds.get(i);
                if (userId.length() > 36) userId = userId.substring(0, 36);
                user.userid = userId;
                user.email = testEmails.get(i);
                user.username = "user" + i;
                user.password = "pwd" + i;
                user.sex = "m";
                user.school = "school" + i;
                user.pictureurl = null;
                user.profile = null;
                user.accountstate = false;
                user.invitetimes = 5;
                // 同时插入UserPT，保证外键依赖
                entity.UserPT userPT = new entity.UserPT();
                userPT.userid = userId;
                userPT.magic = 100 + i;
                userPT.upload = 1000L + i;
                userPT.download = 2000L + i;
                userPT.share = 1.5 + i;
                userPT.farmurl = "/data/seeds/" + userId;
                userPT.viptime = 3 + i;
                userPT.user = user;
                // user.userPT = userPT;
                em.persist(user);
                em.persist(userPT);
                em.flush();
                // 查询
                entity.UserPT got = db.GetInformationPT(userId);
                Assertions.assertNotNull(got, "GetInformationPT should return UserPT");
                Assertions.assertEquals(userId, got.userid);
                Assertions.assertEquals(100 + i, got.magic);
                Assertions.assertEquals(1000L + i, got.upload);
                Assertions.assertEquals(2000L + i, got.download);
                Assertions.assertEquals(1.5 + i, got.share);
                Assertions.assertEquals("/data/seeds/" + userId, got.farmurl);
                Assertions.assertEquals(3 + i, got.viptime);
                // 查询不存在用户
                entity.UserPT notExist = db.GetInformationPT("not_exist_user_" + i);
                Assertions.assertNull(notExist, "GetInformationPT should return null for not found user");
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testUpdateInformationPT() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("UpdateInformationPT test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 先插入User
                User user = new User();
                String userId = testUserIds.get(i);
                user.userid = userId;
                user.email = testEmails.get(i);
                user.username = "user" + i;
                user.password = "pwd" + i;
                user.sex = "m";
                user.school = "school" + i;
                user.pictureurl = null;
                user.profile = null;
                user.accountstate = false;
                user.invitetimes = 5;
                // 自动补齐userPT，避免not-null property异常
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
                em.flush();
                // 修改信息
                entity.UserPT updated = new entity.UserPT();
                updated.userid = userId;
                updated.magic = 200 + i;
                updated.upload = 3000L + i;
                updated.download = 4000L + i;
                updated.share = 2.5 + i;
                updated.farmurl = "/new/path/seed" + i;
                updated.viptime = 8 + i;
                int ret = db.UpdateInformationPT(updated);
                Assertions.assertEquals(0, ret, "UpdateInformationPT should return 0 for success");
                em.flush();
                em.clear();
                entity.UserPT after = em.find(entity.UserPT.class, userId);
                Assertions.assertEquals(200 + i, after.magic);
                Assertions.assertEquals(3000L + i, after.upload);
                Assertions.assertEquals(4000L + i, after.download);
                Assertions.assertEquals(2.5 + i, after.share);
                Assertions.assertEquals("/new/path/seed" + i, after.farmurl);
                Assertions.assertEquals(8 + i, after.viptime);
                // 测试不存在用户
                entity.UserPT notExist = new entity.UserPT();
                notExist.userid = "not_exist_" + i;
                notExist.magic = 0;
                notExist.upload = 0L;
                notExist.download = 0L;
                notExist.share = 0.0;
                notExist.farmurl = null;
                notExist.viptime = 0;
                int ret2 = db.UpdateInformationPT(notExist);
                Assertions.assertEquals(1, ret2, "UpdateInformationPT should return 1 for not found user");
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testRegisterUserPT() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("RegisterUserPT test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 先插入User，保证UserPT外键约束
                User user = new User();
                String userId = testUserIds.get(i);
                if (userId.length() > 36) userId = userId.substring(0, 36);
                user.userid = userId;
                user.email = testEmails.get(i);
                user.username = "user" + i;
                user.password = "pwd" + i;
                user.sex = "m";
                user.school = "school" + i;
                user.pictureurl = null;
                user.profile = null;
                user.accountstate = false;
                user.invitetimes = 5;
                // 自动补齐userPT，避免not-null property异常
                entity.UserPT userPT = new entity.UserPT();
                userPT.userid = userId;
                userPT.magic = 100 + i;
                userPT.upload = 1000L + i;
                userPT.download = 2000L + i;
                userPT.share = 1.5;
                userPT.farmurl = "/path/to/seed" + i;
                userPT.viptime = 3 + i;
                userPT.user = user;
                // user.userPT = userPT;
                // em.persist(user);
                // em.persist(userPT);
                // em.flush();
                // 正常注册
                int ret = db.RegisterUserPT(userPT);
                Assertions.assertEquals(0, ret, "RegisterUserPT should return 0 for success");
                // 再次注册同ID应返回1
                entity.UserPT userPT2 = new entity.UserPT();
                userPT2.userid = userId;
                userPT2.magic = 200 + i;
                userPT2.upload = 3000L + i;
                userPT2.download = 4000L + i;
                userPT2.share = 2.5;
                userPT2.farmurl = "/other/path/seed" + i;
                userPT2.viptime = 8 + i;
                int ret2 = db.RegisterUserPT(userPT2);
                Assertions.assertEquals(1, ret2, "RegisterUserPT should return 1 for duplicate id");
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testGetSeedInformation() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("GetSeedInformation test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 先插入User，保证seed.seeduserid外键依赖
                User user = new User();
                String userId = testUserIds.get(i);
                if (userId.length() > 36) userId = userId.substring(0, 36);
                user.userid = userId;
                user.email = testEmails.get(i);
                user.username = "user" + i;
                user.password = "pwd" + i;
                user.sex = "m";
                user.school = "school" + i;
                user.pictureurl = null;
                user.profile = null;
                user.accountstate = false;
                user.invitetimes = 5;
                // 自动补齐userPT，保证UserPT不为空
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
                // 再插入Seed
                entity.Seed seed = new entity.Seed();
                seed.seedid = "test_seed_" + i + "_" + UUID.randomUUID();
                seed.seeduserid = userId;
                seed.faketime = i;
                seed.lastfakecheck = new java.util.Date();
                seed.outurl = "http://example.com/seed" + i;
                seed.title = "title" + i;
                seed.subtitle = "subtitle" + i;
                seed.seedsize = "100MB";
                seed.seedtag = "tag" + i;
                seed.downloadtimes = 10 + i;
                seed.url = "http://download.com/seed" + i;
                em.persist(seed);
                em.flush();
                // 查询
                entity.Seed got = db.GetSeedInformation(seed.seedid);
                Assertions.assertNotNull(got, "GetSeedInformation should return seed");
                Assertions.assertEquals(seed.seedid, got.seedid);
                Assertions.assertEquals(seed.seeduserid, got.seeduserid);
                Assertions.assertEquals(seed.faketime, got.faketime);
                Assertions.assertEquals(seed.outurl, got.outurl);
                Assertions.assertEquals(seed.title, got.title);
                Assertions.assertEquals(seed.subtitle, got.subtitle);
                Assertions.assertEquals(seed.seedsize, got.seedsize);
                Assertions.assertEquals(seed.seedtag, got.seedtag);
                Assertions.assertEquals(seed.downloadtimes, got.downloadtimes);
                Assertions.assertEquals(seed.url, got.url);
                // 查询不存在种子
                entity.Seed notExist = db.GetSeedInformation("not_exist_seed_" + i);
                Assertions.assertNull(notExist, "GetSeedInformation should return null for not found seed");
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testRegisterSeed() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("RegisterSeed test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 先插入User，保证seed.seeduserid外键依赖，且UserPT不为空
                User user = new User();
                String userId = testUserIds.get(i);
                if (userId.length() > 36) userId = userId.substring(0, 36);
                user.userid = userId;
                user.email = testEmails.get(i);
                user.username = "user" + i;
                user.password = "pwd" + i;
                user.sex = "m";
                user.school = "school" + i;
                user.pictureurl = null;
                user.profile = null;
                user.accountstate = false;
                user.invitetimes = 5;
                // 关键：补充userPT字段，避免not-null property异常
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
                // 再插入Seed
                entity.Seed seed = new entity.Seed();
                String sid = ("test_seed_" + i + "_" + UUID.randomUUID());
                if (sid.length() > 64) sid = sid.substring(0, 64);
                seed.seedid = sid;
                seed.seeduserid = userId;
                seed.faketime = i;
                seed.lastfakecheck = new java.util.Date();
                seed.outurl = ("http://example.com/seed" + i);
                if (seed.outurl != null && seed.outurl.length() > 255) seed.outurl = seed.outurl.substring(0, 255);
                seed.title = ("title" + i);
                if (seed.title.length() > 255) seed.title = seed.title.substring(0, 255);
                seed.subtitle = ("subtitle" + i);
                if (seed.subtitle != null && seed.subtitle.length() > 255) seed.subtitle = seed.subtitle.substring(0, 255);
                seed.seedsize = "100MB";
                if (seed.seedsize.length() > 50) seed.seedsize = seed.seedsize.substring(0, 50);
                seed.seedtag = ("tag" + i);
                if (seed.seedtag != null && seed.seedtag.length() > 255) seed.seedtag = seed.seedtag.substring(0, 255);
                seed.downloadtimes = 10 + i;
                seed.url = ("http://download.com/seed" + i);
                em.persist(seed);
                em.flush();
                // 校验数据库中已存在
                entity.Seed found = em.find(entity.Seed.class, seed.seedid);
                Assertions.assertNotNull(found, "数据库应存在该种子");
                Assertions.assertEquals(seed.seedid, found.seedid);
                Assertions.assertEquals(seed.seeduserid, found.seeduserid);
                Assertions.assertEquals(seed.faketime, found.faketime);
                Assertions.assertEquals(seed.outurl, found.outurl);
                Assertions.assertEquals(seed.title, found.title);
                Assertions.assertEquals(seed.subtitle, found.subtitle);
                Assertions.assertEquals(seed.seedsize, found.seedsize);
                Assertions.assertEquals(seed.seedtag, found.seedtag);
                Assertions.assertEquals(seed.downloadtimes, found.downloadtimes);
                Assertions.assertEquals(seed.url, found.url);
                // 再次注册同ID应返回1
                entity.Seed seed2 = new entity.Seed();
                seed2.seedid = sid;
                seed2.seeduserid = userId;
                seed2.faketime = 99;
                seed2.lastfakecheck = new java.util.Date();
                seed2.outurl = ("http://example.com/seed_dup" + i);
                if (seed2.outurl != null && seed2.outurl.length() > 255) seed2.outurl = seed2.outurl.substring(0, 255);
                seed2.title = ("title_dup" + i);
                if (seed2.title.length() > 255) seed2.title = seed2.title.substring(0, 255);
                seed2.subtitle = ("subtitle_dup" + i);
                if (seed2.subtitle != null && seed2.subtitle.length() > 255) seed2.subtitle = seed2.subtitle.substring(0, 255);
                seed2.seedsize = "200MB";
                if (seed2.seedsize.length() > 50) seed2.seedsize = seed2.seedsize.substring(0, 50);
                seed2.seedtag = ("tag_dup" + i);
                if (seed2.seedtag != null && seed2.seedtag.length() > 255) seed2.seedtag = seed2.seedtag.substring(0, 255);
                seed2.downloadtimes = 99;
                seed2.url = ("http://download.com/seed_dup" + i);
                int ret2 = db.RegisterSeed(seed2);
                Assertions.assertEquals(1, ret2, "RegisterSeed should return 1 for duplicate id");
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testUpdateSeed() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("UpdateSeed test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 先插入User，保证seed.seeduserid外键依赖，且UserPT不为空
                User user = new User();
                String userId = testUserIds.get(i);
                if (userId.length() > 36) userId = userId.substring(0, 36);
                user.userid = userId;
                user.email = testEmails.get(i);
                user.username = "user" + i;
                user.password = "pwd" + i;
                user.sex = "m";
                user.school = "school" + i;
                user.pictureurl = null;
                user.profile = null;
                user.accountstate = false;
                user.invitetimes = 5;
                // 关键：补充userPT字段，避免not-null property异常
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
                // 再插入Seed
                entity.Seed seed = new entity.Seed();
                String sid = ("test_seed_" + i + "_" + UUID.randomUUID());
                if (sid.length() > 64) sid = sid.substring(0, 64);
                seed.seedid = sid;
                seed.seeduserid = userId;
                seed.faketime = i;
                seed.lastfakecheck = new java.util.Date();
                seed.outurl = ("http://example.com/seed" + i);
                if (seed.outurl != null && seed.outurl.length() > 255) seed.outurl = seed.outurl.substring(0, 255);
                seed.title = ("title" + i);
                if (seed.title.length() > 255) seed.title = seed.title.substring(0, 255);
                seed.subtitle = ("subtitle" + i);
                if (seed.subtitle != null && seed.subtitle.length() > 255) seed.subtitle = seed.subtitle.substring(0, 255);
                seed.seedsize = "100MB";
                if (seed.seedsize.length() > 50) seed.seedsize = seed.seedsize.substring(0, 50);
                seed.seedtag = ("tag" + i);
                if (seed.seedtag != null && seed.seedtag.length() > 255) seed.seedtag = seed.seedtag.substring(0, 255);
                seed.downloadtimes = 10 + i;
                seed.url = ("http://download.com/seed" + i);
                em.persist(seed);
                em.flush();
                // 修改信息
                entity.Seed updated = new entity.Seed();
                updated.seedid = sid;
                updated.seeduserid = userId + "_updated";
                if (updated.seeduserid.length() > 36) updated.seeduserid = updated.seeduserid.substring(0, 36);
                updated.faketime = 99;
                updated.lastfakecheck = new java.util.Date();
                updated.outurl = ("http://example.com/seed_updated" + i);
                if (updated.outurl != null && updated.outurl.length() > 255) updated.outurl = updated.outurl.substring(0, 255);
                updated.title = ("title_updated" + i);
                if (updated.title.length() > 255) updated.title = updated.title.substring(0, 255);
                updated.subtitle = ("subtitle_updated" + i);
                if (updated.subtitle != null && updated.subtitle.length() > 255) updated.subtitle = updated.subtitle.substring(0, 255);
                updated.seedsize = "200MB";
                if (updated.seedsize.length() > 50) updated.seedsize = updated.seedsize.substring(0, 50);
                updated.seedtag = ("tag_updated" + i);
                if (updated.seedtag != null && updated.seedtag.length() > 255) updated.seedtag = updated.seedtag.substring(0, 255);
                updated.downloadtimes = 99;
                updated.url = ("http://download.com/seed_updated" + i);
                int ret = db.UpdateSeed(updated);
                Assertions.assertEquals(0, ret, "UpdateSeed should return 0 for success");
                em.flush();
                em.clear();
                entity.Seed after = em.find(entity.Seed.class, sid);
                Assertions.assertEquals(updated.seeduserid, after.seeduserid);
                Assertions.assertEquals(99, after.faketime);
                Assertions.assertEquals(updated.outurl, after.outurl);
                Assertions.assertEquals(updated.title, after.title);
                Assertions.assertEquals(updated.subtitle, after.subtitle);
                Assertions.assertEquals(updated.seedsize, after.seedsize);
                Assertions.assertEquals(updated.seedtag, after.seedtag);
                Assertions.assertEquals(99, after.downloadtimes);
                Assertions.assertEquals(updated.url, after.url);
                // 测试不存在种子
                entity.Seed notExist = new entity.Seed();
                notExist.seedid = "not_exist_seed_" + i;
                if (notExist.seedid.length() > 64) notExist.seedid = notExist.seedid.substring(0, 64);
                notExist.seeduserid = "owner_x";
                if (notExist.seeduserid.length() > 36) notExist.seeduserid = notExist.seeduserid.substring(0, 36);
                notExist.faketime = 0;
                notExist.lastfakecheck = new java.util.Date();
                notExist.outurl = null;
                notExist.title = "not_exist";
                if (notExist.title.length() > 255) notExist.title = notExist.title.substring(0, 255);
                notExist.subtitle = null;
                notExist.seedsize = "0MB";
                if (notExist.seedsize.length() > 50) notExist.seedsize = notExist.seedsize.substring(0, 50);
                notExist.seedtag = null;
                notExist.downloadtimes = 0;
                notExist.url = null;
                int ret2 = db.UpdateSeed(notExist);
                Assertions.assertEquals(1, ret2, "UpdateSeed should return 1 for not found seed");
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testSearchSeed() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("SearchSeed test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 先插入User，保证seed.seeduserid外键依赖
                User user = new User();
                String userId = testUserIds.get(i);
                if (userId.length() > 36) userId = userId.substring(0, 36);
                user.userid = userId;
                user.email = testEmails.get(i);
                user.username = "user" + i;
                user.password = "pwd" + i;
                user.sex = "m";
                user.school = "school" + i;
                user.pictureurl = null;
                user.profile = null;
                user.accountstate = false;
                user.invitetimes = 5;
    
                // 插入UserPT，保证UserPT不为空
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
                em.flush();
                // 插入多条Seed
                List<entity.Seed> seeds = new ArrayList<>();
                for (int j = 0; j < 5; j++) {
                    entity.Seed seed = new entity.Seed();
                    seed.seedid = "search_seed_" + i + "_" + j + "_" + UUID.randomUUID();
                    seed.seeduserid = userId;
                    seed.faketime = j;
                    seed.lastfakecheck = new java.util.Date();
                    seed.outurl = "http://example.com/seed" + j;
                    seed.title = "TestTitle" + i + (j % 2 == 0 ? "_Special" : "") + "_" + j;
                    seed.subtitle = "subtitle" + j;
                    seed.seedsize = "100MB";
                    seed.seedtag = "tag" + j;
                    seed.downloadtimes = 10 + j;
                    seed.url = "http://download.com/seed" + j;
                    em.persist(seed);
                    seeds.add(seed);
                }
                em.flush();
                // 测试关键词能搜到相关种子
                String keyword = "Special";
                entity.Seed[] result = db.SearchSeed(keyword);
                boolean found = false;
                for (entity.Seed s : result) {
                    if (s.title.contains(keyword)) found = true;
                }
                Assertions.assertTrue(found, "SearchSeed should find seeds with keyword in title");
                // 测试空字符串返回所有种子
                entity.Seed[] all = db.SearchSeed("");
                Assertions.assertEquals(seeds.size() + 5, all.length, "SearchSeed with empty string should return all");
                // 测试无匹配关键词
                entity.Seed[] none = db.SearchSeed("NoSuchKeyword" + i);
                Assertions.assertEquals(seeds.size() + 5, none.length, "SearchSeed with no match should return all, sorted");
                // 测试null关键词
                entity.Seed[] nullResult = db.SearchSeed(null);
                Assertions.assertEquals(seeds.size() + 5, nullResult.length, "SearchSeed with null should return all");
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testAddNotice() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("AddNotice test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 构造 Notice
                entity.Notice notice = new entity.Notice();
                notice.noticeid = "test_notice_" + i + "_" + UUID.randomUUID();
                notice.noticecontent = "内容" + i;
                notice.state = (i % 2 == 0);
                notice.posttag = "tag" + i;
                // 正常插入
                int ret = db.AddNotice(notice);
                Assertions.assertEquals(0, ret, "AddNotice 应返回0");
                // 再次插入同ID应返回1
                entity.Notice notice2 = new entity.Notice();
                notice2.noticeid = notice.noticeid;
                notice2.noticecontent = "内容重复" + i;
                notice2.state = false;
                notice2.posttag = "tag_dup" + i;
                int ret2 = db.AddNotice(notice2);
                Assertions.assertEquals(1, ret2, "AddNotice 应返回1（重复ID）");
                // 校验数据库中已存在
                entity.Notice found = em.find(entity.Notice.class, notice.noticeid);
                Assertions.assertNotNull(found, "数据库应存在该公告");
                Assertions.assertEquals(notice.noticecontent, found.noticecontent);
                Assertions.assertEquals(notice.state, found.state);
                Assertions.assertEquals(notice.posttag, found.posttag);
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testUpdateNotice() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("UpdateNotice test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 先插入一个Notice
                entity.Notice notice = new entity.Notice();
                notice.noticeid = "test_notice_update_" + i + "_" + UUID.randomUUID();
                notice.noticecontent = "原内容" + i;
                notice.state = true;
                notice.posttag = "tag" + i;
                em.persist(notice);
                em.flush();
                // 修改内容
                entity.Notice updated = new entity.Notice();
                updated.noticeid = notice.noticeid;
                updated.noticecontent = "新内容" + i;
                updated.state = false;
                updated.posttag = "tag_updated" + i;
                boolean ret = db.UpdateNotice(updated);
                Assertions.assertTrue(ret, "UpdateNotice 应返回 true");
                em.flush();
                em.clear();
                entity.Notice after = em.find(entity.Notice.class, notice.noticeid);
                Assertions.assertEquals("新内容" + i, after.noticecontent);
                Assertions.assertFalse(after.state);
                Assertions.assertEquals("tag_updated" + i, after.posttag);
                // 测试不存在公告
                entity.Notice notExist = new entity.Notice();
                notExist.noticeid = "not_exist_notice_" + i;
                notExist.noticecontent = "xxx";
                notExist.state = false;
                notExist.posttag = "none";
                boolean ret2 = db.UpdateNotice(notExist);
                Assertions.assertFalse(ret2, "UpdateNotice 应返回 false for not found");
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testDeleteNotice() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("DeleteNotice test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 先插入一个Notice
                entity.Notice notice = new entity.Notice();
                notice.noticeid = "test_notice_delete_" + i + "_" + UUID.randomUUID();
                notice.noticecontent = "内容" + i;
                notice.state = true;
                notice.posttag = "tag" + i;
                em.persist(notice);
                em.flush();
                // 删除
                boolean ret = db.DeleteNotice(notice.noticeid);
                Assertions.assertTrue(ret, "DeleteNotice 应返回 true");
                em.flush();
                em.clear();
                entity.Notice after = em.find(entity.Notice.class, notice.noticeid);
                Assertions.assertNull(after, "DeleteNotice 后应查不到公告");
                // 删除不存在的公告
                boolean ret2 = db.DeleteNotice("not_exist_notice_" + i);
                Assertions.assertFalse(ret2, "DeleteNotice 应返回 false for not found");
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testGetUserAvailableInviteTimes() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("GetUserAvailableInviteTimes test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 先插入一个User
                entity.User user = new entity.User();
                String userId = testUserIds.get(i);
                if (userId.length() > 36) userId = userId.substring(0, 36);
                user.userid = userId;
                user.email = "invite" + i + "@example.com";
                user.username = "user" + i;
                user.password = "pwd" + i;
                user.sex = "m";
                user.school = "school" + i;
                user.pictureurl = "pic" + i + ".jpg";
                user.profile = "profile" + i;
                user.accountstate = true;
                user.invitetimes = 5 + i;
                
                // 插入UserPT，保证UserPT不为空
                entity.UserPT userPT = new entity.UserPT();
                userPT.userid = user.userid;
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
                em.flush();
                // 查询
                int left = db.GetUserAvailableInviteTimes(user.userid);
                Assertions.assertEquals(5 + i, left, "GetUserAvailableInviteTimes 应返回正确剩余次数");
                // 查询不存在用户
                int notExist = db.GetUserAvailableInviteTimes("not_exist_user_" + i);
                Assertions.assertEquals(-1, notExist, "GetUserAvailableInviteTimes 不存在用户应返回-1");
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testInviteUser() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("InviteUser test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 先插入一个User
                entity.User user = new entity.User();
                String userId = testUserIds.get(i);
                if (userId.length() > 36) userId = userId.substring(0, 36);
                user.userid = userId;
                user.email = "invite" + i + "@example.com";
                user.username = "user" + i;
                user.password = "pwd" + i;
                user.sex = "m";
                user.school = "school" + i;
                user.pictureurl = "pic" + i + ".jpg";
                user.profile = "profile" + i;
                user.accountstate = true;
                user.invitetimes = 3 + i;
                
                // 插入UserPT，保证UserPT不为空
                entity.UserPT userPT = new entity.UserPT();
                userPT.userid = user.userid;
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
                em.flush();
                // 正常邀请
                int ret = db.InviteUser(user.userid, user.email);
                Assertions.assertEquals(0, ret, "InviteUser 应返回0");
                em.flush();
                em.clear();
                entity.User after = em.find(entity.User.class, user.userid);
                Assertions.assertEquals(2 + i, after.invitetimes, "邀请后次数应减少1");
                // 剩余次数不足
                after.invitetimes = 0;
                em.merge(after);
                em.flush();
                int ret2 = db.InviteUser(user.userid, user.email);
                Assertions.assertEquals(1, ret2, "InviteUser 剩余次数不足应返回1");
                // 邮箱不匹配
                int ret3 = db.InviteUser(user.userid, "wrong" + i + "@example.com");
                Assertions.assertEquals(3, ret3, "InviteUser 邮箱不匹配应返回3");
                // 用户不存在
                int ret4 = db.InviteUser("not_exist_user_" + i, "invite" + i + "@example.com");
                Assertions.assertEquals(3, ret4, "InviteUser 用户不存在应返回3");
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testAddCollect() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("AddCollect test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 构造用户和种子
                String userId = testUserIds.get(i);
                if (userId.length() > 36) userId = userId.substring(0, 36);

                entity.User user = new entity.User();
                user.userid = userId;
                user.email = "invite" + i + "@example.com";
                user.username = "user" + i;
                user.password = "pwd" + i;
                user.sex = "m";
                user.school = "school" + i;
                user.pictureurl = "pic" + i + ".jpg";
                user.profile = "profile" + i;
                user.accountstate = true;
                user.invitetimes = 5 + i;
                // 插入UserPT，保证UserPT不为空
                entity.UserPT userPT = new entity.UserPT();
                userPT.userid = user.userid;
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
                em.flush();
                entity.Seed seed = new entity.Seed();
                String seedid = "test_seed_collect_" + i + "_" + UUID.randomUUID();
                seed.seedid = seedid;
                seed.seeduserid = userId;
                seed.faketime = i;
                seed.lastfakecheck = new java.util.Date();
                seed.outurl = "http://example.com/seed" + i;
                seed.title = "title" + i;
                seed.subtitle = "subtitle" + i;
                seed.seedsize = "100MB";
                seed.seedtag = "tag" + i;
                seed.downloadtimes = 10 + i;
                seed.url = "http://download.com/seed" + i;
                em.persist(seed);
                em.flush();
                // 正常收藏
                boolean ret = db.AddCollect(userId, seedid);
                Assertions.assertTrue(ret, "AddCollect 应返回 true");
                em.flush();
                em.clear();
                entity.UserStar found = em.find(entity.UserStar.class, new entity.UserStarId(userId, seedid));
                Assertions.assertNotNull(found, "收藏应已存在");
                // 重复收藏
                boolean ret2 = db.AddCollect(userId, seedid);
                Assertions.assertFalse(ret2, "AddCollect 重复应返回 false");
                // 用户不存在
                boolean ret3 = db.AddCollect("not_exist_user_" + i, seedid);
                Assertions.assertFalse(ret3, "AddCollect 用户不存在应返回 false");
                // 帖子不存在
                boolean ret4 = db.AddCollect(userId, "not_exist_post_" + i);
                Assertions.assertFalse(ret4, "AddCollect 帖子不存在应返回 false");
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testDeleteCollect() {
        return IntStream.range(0, 10).mapToObj(i -> DynamicTest.dynamicTest("DeleteCollect test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 构造用户和种子
                String userId = testUserIds.get(i);
                if (userId.length() > 36) userId = userId.substring(0, 36);

                entity.User user = new entity.User();
                user.userid = userId;
                user.email = "invite" + i + "@example.com";
                user.username = "user" + i;
                user.password = "pwd" + i;
                user.sex = "m";
                user.school = "school" + i;
                user.pictureurl = "pic" + i + ".jpg";
                user.profile = "profile" + i;
                user.accountstate = true;
                user.invitetimes = 5 + i;
                // 插入UserPT，保证UserPT不为空
                entity.UserPT userPT = new entity.UserPT();
                userPT.userid = user.userid;
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
                em.flush();
                entity.Seed seed = new entity.Seed();
                String seedid = "test_seed_collect_" + i + "_" + UUID.randomUUID();
                seed.seedid = seedid;
                seed.seeduserid = userId;
                seed.faketime = i;
                seed.lastfakecheck = new java.util.Date();
                seed.outurl = "http://example.com/seed" + i;
                seed.title = "title" + i;
                seed.subtitle = "subtitle" + i;
                seed.seedsize = "100MB";
                seed.seedtag = "tag" + i;
                seed.downloadtimes = 10 + i;
                seed.url = "http://download.com/seed" + i;
                em.persist(seed);
                em.flush();
                // 先收藏
                entity.UserStar star = new entity.UserStar();
                star.userid = userId;
                star.seedid = seedid;
                em.persist(star);
                em.flush();
                // 正常删除
                boolean ret = db.DeleteCollect(userId, seedid);
                Assertions.assertTrue(ret, "DeleteCollect 应返回 true");
                em.flush();
                em.clear();
                entity.UserStar found = em.find(entity.UserStar.class, new entity.UserStarId(userId, seedid));
                Assertions.assertNull(found, "删除后收藏应不存在");
                // 删除不存在的收藏
                boolean ret2 = db.DeleteCollect(userId, seedid);
                Assertions.assertTrue(ret2, "再次删除应返回 true（JPA remove null容忍）");
                // 用户不存在
                boolean ret3 = db.DeleteCollect("not_exist_user_" + i, seedid);
                Assertions.assertTrue(ret3, "用户不存在时应返回 true（JPA remove null容忍）");
                // 帖子不存在
                boolean ret4 = db.DeleteCollect(userId, "not_exist_seed_" + i);
                Assertions.assertTrue(ret4, "帖子不存在时应返回 true（JPA remove null容忍）");
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }

    @TestFactory
    Collection<DynamicTest> testLoginUser() {
        // 查询所有现有用户
        List<User> allUsers = em.createQuery("SELECT u FROM User u", User.class).getResultList();
        
        // 为每个用户创建测试
        return allUsers.stream().map(existingUser -> 
            DynamicTest.dynamicTest("LoginUser test for user: " + existingUser.username, () -> {
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                try {
                    // 测试1：使用用户名登录（成功）
                    User loginWithUsername = new User();
                    loginWithUsername.username = existingUser.username;
                    loginWithUsername.password = existingUser.password;
                    String loginResult1 = db.LoginUser(loginWithUsername);
                    Assertions.assertEquals(existingUser.userid, loginResult1, 
                        "Should successfully login with username and correct password for user: " + existingUser.username);
                    
                    // 测试2：使用邮箱登录（成功）
                    User loginWithEmail = new User();
                    loginWithEmail.email = existingUser.email;
                    loginWithEmail.password = existingUser.password;
                    String loginResult2 = db.LoginUser(loginWithEmail);
                    Assertions.assertEquals(existingUser.userid, loginResult2, 
                        "Should successfully login with email and correct password for user: " + existingUser.email);
                    
                    // 测试3：密码错误（失败）
                    User wrongPassword = new User();
                    wrongPassword.username = existingUser.username;
                    wrongPassword.password = "wrong_password_" + UUID.randomUUID();
                    String loginResult3 = db.LoginUser(wrongPassword);
                    Assertions.assertNull(loginResult3, 
                        "Should fail with wrong password for user: " + existingUser.username);
                    
                    // 测试4：同时提供用户名和邮箱（失败）
                    User bothFields = new User();
                    bothFields.username = existingUser.username;
                    bothFields.email = existingUser.email;
                    bothFields.password = existingUser.password;
                    String loginResult5 = db.LoginUser(bothFields);
                    Assertions.assertNull(loginResult5, 
                        "Should fail when both username and email are provided for user: " + existingUser.username);
                    
                } finally {
                    tx.rollback();
                }
            })
        ).collect(Collectors.toList());
    }
    
    @TestFactory
    Collection<DynamicTest> testLoginUserAdditionalCases() {
        return IntStream.range(0, 3).mapToObj(i -> DynamicTest.dynamicTest("LoginUser additional test #" + i, () -> {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 测试1：用户名不存在（失败）
                User nonExistentUser = new User();
                nonExistentUser.username = "non_existent_user_" + UUID.randomUUID();
                nonExistentUser.password = "any_password";
                String loginResult1 = db.LoginUser(nonExistentUser);
                Assertions.assertNull(loginResult1, "Should fail with non-existent username");
                
                // 测试2：邮箱不存在（失败）
                User nonExistentEmail = new User();
                nonExistentEmail.email = "non_existent_" + UUID.randomUUID() + "@example.com";
                nonExistentEmail.password = "any_password";
                String loginResult2 = db.LoginUser(nonExistentEmail);
                Assertions.assertNull(loginResult2, "Should fail with non-existent email");
                
                // 测试3：缺少密码（失败）
                User noPassword = new User();
                noPassword.username = "some_username";
                noPassword.password = null;
                String loginResult3 = db.LoginUser(noPassword);
                Assertions.assertNull(loginResult3, "Should fail when password is null");
                
                // 测试4：null userinfo（失败）
                String loginResult4 = db.LoginUser(null);
                Assertions.assertNull(loginResult4, "Should fail when userinfo is null");
                
            } finally {
                tx.rollback();
            }
        })).collect(Collectors.toList());
    }
}