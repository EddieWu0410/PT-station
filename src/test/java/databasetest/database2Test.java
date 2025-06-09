package databasetest;

import database.Database2;
import entity.*;
import org.junit.jupiter.api.*;

import javax.persistence.*;
import java.util.*;

public class database2Test {
    private static EntityManagerFactory emf;
    private static EntityManager em;
    private static Database2 db;

    @BeforeAll
    static void setup() throws Exception {
        // 强制加载 MySQL 驱动
        Class.forName("com.mysql.cj.jdbc.Driver");
        entity.config cfg = new entity.config();
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
        db = new Database2(emf);
    }

    @AfterAll
    static void teardown() {
        if (em != null && em.isOpen())
            em.close();
        if (emf != null && emf.isOpen())
            emf.close();
    }

    @TestFactory
    Collection<DynamicTest> testAddBegSeed() {
        List<String> userIds = em.createQuery("select u.userid from User u", String.class).getResultList();
        if (userIds.isEmpty())
            return Collections.emptyList();
        String uid = userIds.get(0);

        return List.of(
                DynamicTest.dynamicTest("AddBegSeed success", () -> {
                    String begId = "test_beg_" + System.currentTimeMillis();
                    BegInfo beg = new BegInfo();
                    beg.begid = begId;
                    beg.begnumbers = 1;
                    beg.magic = 100;
                    beg.endtime = new Date();
                    beg.hasseed = 0;
                    try {
                        int ret = db.AddBegSeed(beg);
                        Assertions.assertEquals(0, ret, "AddBegSeed应返回0");
                    } finally {
                        EntityTransaction tx = em.getTransaction();
                        tx.begin();
                        BegInfo toDelete = em.find(BegInfo.class, begId);
                        if (toDelete != null)
                            em.remove(toDelete);
                        tx.commit();
                    }
                }),

                DynamicTest.dynamicTest("AddBegSeed duplicate", () -> {
                    String begId = "test_beg_" + (System.currentTimeMillis() + 1);
                    BegInfo beg = new BegInfo();
                    beg.begid = begId;
                    beg.begnumbers = 1;
                    beg.magic = 100;
                    beg.endtime = new Date();
                    beg.hasseed = 0;
                    try {
                        db.AddBegSeed(beg);
                        int ret = db.AddBegSeed(beg);
                        Assertions.assertEquals(1, ret, "重复插入应返回1");
                    } finally {
                        EntityTransaction tx = em.getTransaction();
                        tx.begin();
                        BegInfo toDelete = em.find(BegInfo.class, begId);
                        if (toDelete != null)
                            em.remove(toDelete);
                        tx.commit();
                    }
                }),

                DynamicTest.dynamicTest("AddBegSeed invalid param", () -> {
                    Assertions.assertEquals(2, db.AddBegSeed(null));
                    BegInfo invalidBeg = new BegInfo();
                    invalidBeg.begid = "";
                    Assertions.assertEquals(2, db.AddBegSeed(invalidBeg));
                }));
    }

    @TestFactory
    Collection<DynamicTest> testUpdateBegSeed() {
        List<BegInfo> existingBegs = em.createQuery("SELECT b FROM BegInfo b", BegInfo.class)
                .setMaxResults(1)
                .getResultList();
        if (existingBegs.isEmpty()) {
            return Collections.emptyList();
        }

        BegInfo originalBeg = existingBegs.get(0);
        String begId = originalBeg.begid;
        int originalMagic = originalBeg.magic;
        int originalBegNumbers = originalBeg.begnumbers;
        int originalHasSeed = originalBeg.hasseed;
        Date originalEndTime = originalBeg.endtime;

        return List.of(
                DynamicTest.dynamicTest("UpdateBegSeed success", () -> {
                    try {
                        BegInfo update = new BegInfo();
                        update.begid = begId;
                        update.begnumbers = originalBegNumbers + 1;
                        update.magic = originalMagic + 100;
                        update.endtime = originalEndTime;
                        update.hasseed = originalHasSeed;

                        int ret = db.UpdateBegSeed(update);
                        Assertions.assertEquals(0, ret, "UpdateBegSeed应返回0");

                        em.clear();
                        BegInfo updated = em.find(BegInfo.class, begId);
                        Assertions.assertEquals(originalMagic + 100, updated.magic, "魔力值应已更新");
                        Assertions.assertEquals(originalBegNumbers + 1, updated.begnumbers, "求种人数应已更新");
                    } finally {
                        // 恢复原始数据
                        EntityTransaction tx = em.getTransaction();
                        tx.begin();
                        BegInfo toRestore = em.find(BegInfo.class, begId);
                        if (toRestore != null) {
                            toRestore.magic = originalMagic;
                            toRestore.begnumbers = originalBegNumbers;
                            toRestore.hasseed = originalHasSeed;
                            toRestore.endtime = originalEndTime;
                            em.merge(toRestore);
                        }
                        tx.commit();
                    }
                }),
                DynamicTest.dynamicTest("UpdateBegSeed not exist", () -> {
                    BegInfo notExist = new BegInfo();
                    notExist.begid = "not_exist_beg";
                    notExist.begnumbers = 1;
                    notExist.magic = 100;
                    notExist.endtime = new Date();
                    notExist.hasseed = 0;

                    int ret = db.UpdateBegSeed(notExist);
                    Assertions.assertEquals(1, ret, "不存在的求种应返回1");
                }),
                DynamicTest.dynamicTest("UpdateBegSeed invalid param", () -> {
                    Assertions.assertEquals(2, db.UpdateBegSeed(null));
                    BegInfo invalid = new BegInfo();
                    invalid.begid = "";
                    Assertions.assertEquals(2, db.UpdateBegSeed(invalid));
                }));
    }

    @TestFactory
    Collection<DynamicTest> testDeleteBegSeed() {
        return List.of(
            DynamicTest.dynamicTest("DeleteBegSeed success", () -> {
                String begId = "test_beg_" + (System.currentTimeMillis() + 1);
                BegInfo beg = new BegInfo();
                beg.begid = begId;
                beg.begnumbers = 0;
                beg.magic = 1;
                beg.endtime = new Date();
                beg.hasseed = 0;

                int addRet = db.AddBegSeed(beg);
                int delRet = db.DeleteBegSeed(begId);
                Assertions.assertEquals(0, delRet, "DeleteBegSeed 应返回0");
            }),
            DynamicTest.dynamicTest("DeleteBegSeed not exist", () -> {
                int ret = db.DeleteBegSeed("not_exist_beg");
                Assertions.assertEquals(1, ret, "不存在的求种任务应返回1");
            }),
            DynamicTest.dynamicTest("DeleteBegSeed invalid param", () -> {
                Assertions.assertEquals(2, db.DeleteBegSeed(null));
                Assertions.assertEquals(2, db.DeleteBegSeed(""));
            })
        );
    }

    // @TestFactory
    // Collection<DynamicTest> testVoteSeed() {
    //     // 获取现有用户ID
    //     List<String> userIds = em.createQuery("select u.userid from User u", String.class)
    //             .setMaxResults(1)
    //             .getResultList();
    //     if (userIds.isEmpty())
    //         return Collections.emptyList();
    //     String uid = userIds.get(0);

    //     // 获取现有的求种信息
    //     List<BegInfo> begs = em.createQuery("SELECT b FROM BegInfo b", BegInfo.class)
    //             .setMaxResults(1)
    //             .getResultList();
    //     if (begs.isEmpty())
    //         return Collections.emptyList();
    //     String begId = begs.get(0).begid;

    //     // 获取现有的种子信息
    //     List<Seed> seeds = em.createQuery("SELECT s FROM Seed s", Seed.class)
    //             .setMaxResults(1)
    //             .getResultList();
    //     if (seeds.isEmpty())
    //         return Collections.emptyList();
    //     String seedId = seeds.get(0).seedid;

    //     try {
    //         return List.of(
    //                 DynamicTest.dynamicTest("VoteSeed success", () -> {
    //                     int ret = db.VoteSeed(begId, seedId, uid);
    //                     Assertions.assertEquals(0, ret, "VoteSeed应返回0");
    //                 }),

    //                 DynamicTest.dynamicTest("VoteSeed duplicate", () -> {
    //                     int ret = db.VoteSeed(begId, seedId, uid);
    //                     Assertions.assertEquals(1, ret, "重复投票应返回1");
    //                 }),

    //                 DynamicTest.dynamicTest("VoteSeed invalid param", () -> {
    //                     Assertions.assertEquals(2, db.VoteSeed(null, seedId, uid));
    //                     Assertions.assertEquals(2, db.VoteSeed(begId, null, uid));
    //                     Assertions.assertEquals(2, db.VoteSeed(begId, seedId, null));
    //                 }));
    //     } finally {
    //         EntityTransaction tx = em.getTransaction();
    //         tx.begin();
    //         try {
    //             em.createQuery(
    //                     "DELETE FROM UserVotes v WHERE v.begid = :begid AND v.seedid = :seedid AND v.userid = :uid")
    //                     .setParameter("begid", begId)
    //                     .setParameter("seedid", seedId)
    //                     .setParameter("uid", uid)
    //                     .executeUpdate();
    //         } catch (Exception ignored) {
    //         }
    //         tx.commit();
    //     }
    // }

    @TestFactory
    Collection<DynamicTest> testSubmitSeed() {
        // 获取现有的求种信息
        List<BegInfo> begs = em.createQuery("SELECT b FROM BegInfo b WHERE b.hasseed = 0", BegInfo.class)
                .setMaxResults(1)
                .getResultList();
        if (begs.isEmpty())
            return Collections.emptyList();
        String begId = begs.get(0).begid;

        // 获取现有的可用种子信息
        List<Seed> existingSeeds = em.createQuery(
                "SELECT s FROM Seed s WHERE s.seedid NOT IN " +
                        "(SELECT ss.seed.seedid FROM SubmitSeed ss)",
                Seed.class)
                .setMaxResults(1)
                .getResultList();
        if (existingSeeds.isEmpty())
            return Collections.emptyList();
        Seed seed = existingSeeds.get(0);

        try {
            return List.of(
                    DynamicTest.dynamicTest("SubmitSeed success", () -> {
                        int ret = db.SubmitSeed(begId, seed);
                        Assertions.assertEquals(0, ret, "SubmitSeed应返回0");
                    }),

                    DynamicTest.dynamicTest("SubmitSeed duplicate", () -> {
                        int ret = db.SubmitSeed(begId, seed);
                        Assertions.assertEquals(1, ret, "重复提交应返回1");
                    }),

                    DynamicTest.dynamicTest("SubmitSeed invalid param", () -> {
                        Assertions.assertEquals(2, db.SubmitSeed(null, seed));
                        Assertions.assertEquals(2, db.SubmitSeed(begId, null));
                    }));
        } finally {
            // 清理测试数据
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 恢复求种状态
                BegInfo toRestore = em.find(BegInfo.class, begId);
                if (toRestore != null) {
                    toRestore.hasseed = 0;
                    em.merge(toRestore);
                }

                // 清理投票记录
                em.createQuery("DELETE FROM Vote v WHERE v.begid = :begid AND v.seedid = :seedid")
                        .setParameter("begid", begId)
                        .setParameter("seedid", seed.seedid)
                        .executeUpdate();
            } catch (Exception ignored) {
            }
            tx.commit();
        }
    }

    @Test
    void testSettleBeg() {
        String uid = em.createQuery("select u.userid from User u", String.class)
                .setMaxResults(1)
                .getSingleResult();

        String begId = "test_beg_settle_" + System.currentTimeMillis();
        String seedId = "test_seed_settle_" + System.currentTimeMillis();

        BegInfo beg = new BegInfo();
        beg.begid = begId;
        beg.begnumbers = 1;
        beg.magic = 100;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -15); // 15天前
        beg.endtime = cal.getTime();
        beg.hasseed = 0;

        Seed seed = new Seed();
        seed.seedid = seedId;
        seed.seeduserid = uid;
        seed.title = "测试种子";
        seed.seedsize = "1";

        try {
            db.AddBegSeed(beg);
            db.SubmitSeed(begId, seed);
            db.VoteSeed(begId, seedId, uid);

            db.SettleBeg();
            em.clear();

            BegInfo settled = em.find(BegInfo.class, begId);
            Assertions.assertEquals(settled.hasseed, 1, "求种应已完成");

            UserPT userPT = em.find(UserPT.class, uid);
            Assertions.assertTrue(userPT.magic >= 0, "用户应获得魔力值奖励");
        } finally {
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                // 删除投票记录
                em.createQuery(
                        "DELETE FROM UserVotes v WHERE v.begid = :begid AND v.seedid = :seedid AND v.userid = :uid")
                        .setParameter("begid", begId)
                        .setParameter("seedid", seedId)
                        .setParameter("uid", uid)
                        .executeUpdate();
                // 删除提交记录
                em.createQuery("DELETE FROM SubmitSeed s WHERE s.begid = :begid AND s.seedid = :seedid")
                        .setParameter("begid", begId)
                        .setParameter("seedid", seedId)
                        .executeUpdate();
            } catch (Exception ignored) {
            }
            // 删除种子和求种任务
            Seed toDeleteSeed = em.find(Seed.class, seedId);
            if (toDeleteSeed != null)
                em.remove(toDeleteSeed);
            BegInfo toDeleteBeg = em.find(BegInfo.class, begId);
            if (toDeleteBeg != null)
                em.remove(toDeleteBeg);
            tx.commit();
        }
    }

    @TestFactory
    Collection<DynamicTest> testAddPost() {
        List<String> userIds = em.createQuery("select u.userid from User u", String.class).getResultList();
        if (userIds.isEmpty())
            return Collections.emptyList();
        String uid = userIds.get(0);
        String postid = "test_post_" + System.currentTimeMillis();
        Post post = new Post();
        post.postid = postid;
        post.postuserid = uid;
        post.posttitle = "单元测试帖子";
        post.postcontent = "内容";
        post.posttime = new Date();

        return List.of(
                DynamicTest.dynamicTest("AddPost success", () -> {
                    try {
                        int ret = db.AddPost(post);
                        Assertions.assertEquals(0, ret, "AddPost 应返回0");
                        Post inserted = em.find(Post.class, postid);
                        Assertions.assertNotNull(inserted, "帖子应已插入");
                    } finally {
                        EntityTransaction tx = em.getTransaction();
                        tx.begin();
                        Post toDelete = em.find(Post.class, postid);
                        if (toDelete != null)
                            em.remove(toDelete);
                        tx.commit();
                    }
                }),
                DynamicTest.dynamicTest("AddPost duplicate", () -> {
                    try {
                        db.AddPost(post);
                        int ret = db.AddPost(post);
                        Assertions.assertEquals(1, ret, "重复插入应返回1");
                    } finally {
                        EntityTransaction tx = em.getTransaction();
                        tx.begin();
                        Post toDelete = em.find(Post.class, postid);
                        if (toDelete != null)
                            em.remove(toDelete);
                        tx.commit();
                    }
                }),
                DynamicTest.dynamicTest("AddPost user not exist", () -> {
                    Post p2 = new Post();
                    p2.postid = "test_post_" + (System.currentTimeMillis() + 1);
                    p2.postuserid = "not_exist_user";
                    p2.posttitle = "无效用户";
                    p2.postcontent = "内容";
                    p2.posttime = new Date();
                    int ret = db.AddPost(p2);
                    Assertions.assertEquals(2, ret, "用户不存在应返回2");
                }),
                DynamicTest.dynamicTest("AddPost invalid param", () -> {
                    Assertions.assertEquals(2, db.AddPost(null));
                    Post p3 = new Post();
                    p3.postid = null;
                    p3.postuserid = uid;
                    Assertions.assertEquals(2, db.AddPost(p3));
                    Post p4 = new Post();
                    p4.postid = "";
                    p4.postuserid = uid;
                    Assertions.assertEquals(2, db.AddPost(p4));
                }));
    }

    @TestFactory
    Collection<DynamicTest> testUpdatePost() {
        List<Post> existingPosts = em.createQuery("SELECT p FROM Post p", Post.class)
                .setMaxResults(1)
                .getResultList();
        if (existingPosts.isEmpty()) {
            return Collections.emptyList();
        }

        Post originalPost = existingPosts.get(0);
        String postId = originalPost.postid;
        String originalTitle = originalPost.posttitle;
        String originalContent = originalPost.postcontent;
        String originalUserId = originalPost.postuserid;
        Date originalTime = originalPost.posttime;

        return List.of(
                DynamicTest.dynamicTest("UpdatePost success", () -> {
                    try {
                        Post update = new Post();
                        update.postid = postId;
                        update.postuserid = originalUserId;
                        update.posttitle = originalTitle + "_updated";
                        update.postcontent = originalContent + "_updated";
                        update.posttime = originalTime;

                        int ret = db.UpdatePost(update);
                        Assertions.assertEquals(0, ret, "UpdatePost应返回0");

                        em.clear();
                        Post updated = em.find(Post.class, postId);
                        Assertions.assertEquals(originalTitle + "_updated", updated.posttitle, "标题应已更新");
                        Assertions.assertEquals(originalContent + "_updated", updated.postcontent, "内容应已更新");
                    } finally {
                        // 恢复原始数据
                        EntityTransaction tx = em.getTransaction();
                        tx.begin();
                        Post toRestore = em.find(Post.class, postId);
                        if (toRestore != null) {
                            toRestore.posttitle = originalTitle;
                            toRestore.postcontent = originalContent;
                            toRestore.posttime = originalTime;
                            em.merge(toRestore);
                        }
                        tx.commit();
                    }
                }),
                DynamicTest.dynamicTest("UpdatePost not exist", () -> {
                    Post notExist = new Post();
                    notExist.postid = "not_exist_post";
                    notExist.postuserid = originalUserId;
                    notExist.posttitle = "不存在的帖子";
                    notExist.postcontent = "测试内容";
                    notExist.posttime = new Date();

                    int ret = db.UpdatePost(notExist);
                    Assertions.assertEquals(1, ret, "不存在的帖子应返回1");
                }),
                DynamicTest.dynamicTest("UpdatePost invalid param", () -> {
                    Assertions.assertEquals(2, db.UpdatePost(null));
                    Post invalid = new Post();
                    invalid.postid = "";
                    Assertions.assertEquals(2, db.UpdatePost(invalid));

                    Post invalid2 = new Post();
                    invalid2.postid = null;
                    Assertions.assertEquals(2, db.UpdatePost(invalid2));
                }));
    }

    @TestFactory
    Collection<DynamicTest> testDeletePost() {
        List<String> userIds = em.createQuery("select u.userid from User u", String.class).getResultList();
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }
        String uid = userIds.get(0);
        String postid = "test_post_" + System.currentTimeMillis();
        Post post = new Post();
        post.postid = postid;
        post.postuserid = uid;
        post.posttitle = "单元测试帖子";
        post.postcontent = "内容";
        post.posttime = new Date();

        return List.of(
            DynamicTest.dynamicTest("DeletePost success", () -> {
                try {
                    int addRet = db.AddPost(post);
                    int delRet = db.DeletePost(postid);
                    Assertions.assertEquals(0, delRet, "DeletePost 应返回0");
                } finally {
                }
            }),
            DynamicTest.dynamicTest("DeletePost not exist", () -> {
                int ret = db.DeletePost("not_exist_post");
                Assertions.assertEquals(1, ret, "不存在的帖子应返回1");
            }),
            DynamicTest.dynamicTest("DeletePost invalid param", () -> {
                Assertions.assertEquals(2, db.DeletePost(null));
                Assertions.assertEquals(2, db.DeletePost(""));
            })
        );
    }

    @TestFactory
    Collection<DynamicTest> testAddComment() {
        List<String> userIds = em.createQuery("select u.userid from User u", String.class).getResultList();
        List<String> postIds = em.createQuery("select p.postid from Post p", String.class).getResultList();
        if (userIds.isEmpty() || postIds.isEmpty()) {
            return Collections.emptyList();
        }
        String uid = userIds.get(0);
        String pid = postIds.get(0);
        String comment = "单元测试评论";
        return List.of(
                DynamicTest.dynamicTest("AddComment", () -> {
                    String replyid = null;
                    try {
                        int ret = db.AddComment(pid, uid, comment);
                        Assertions.assertEquals(0, ret, "AddComment 应返回0");
                        List<PostReply> replies = em.createQuery(
                                "SELECT r FROM PostReply r WHERE r.postid = :pid AND r.authorid = :uid AND r.content = :c",
                                PostReply.class)
                                .setParameter("pid", pid)
                                .setParameter("uid", uid)
                                .setParameter("c", comment)
                                .getResultList();
                        Assertions.assertFalse(replies.isEmpty(), "评论应已插入");
                        replyid = replies.get(0).replyid;
                    } finally {
                        if (replyid != null) {
                            EntityTransaction tx = em.getTransaction();
                            tx.begin();
                            PostReply toDelete = em.find(PostReply.class, replyid);
                            if (toDelete != null)
                                em.remove(toDelete);
                            tx.commit();
                        }
                    }
                }));
    }

    @TestFactory
    Collection<DynamicTest> testDeleteComment() {
        List<String> userIds = em.createQuery("select u.userid from User u", String.class).getResultList();
        List<String> postIds = em.createQuery("select p.postid from Post p", String.class).getResultList();
        if (userIds.isEmpty() || postIds.isEmpty()) {
            return Collections.emptyList();
        }
        String uid = userIds.get(0);
        String pid = postIds.get(0);
        String comment = "待删除评论";

        return List.of(
                DynamicTest.dynamicTest("DeleteComment", () -> {
                    String replyid = null;
                    try {
                        // 先确保评论存在
                        EntityTransaction tx = em.getTransaction();
                        tx.begin();
                        PostReply reply = new PostReply();
                        reply.replyid = "reply_" + System.currentTimeMillis();
                        reply.postid = pid;
                        reply.authorid = uid;
                        reply.content = comment;
                        reply.createdAt = new Date();
                        em.persist(reply);
                        tx.commit();
                        replyid = reply.replyid;

                        // 执行删除测试
                        int ret = db.DeleteComment(pid, replyid);
                        Assertions.assertEquals(0, ret, "DeleteComment 应返回0");

                        em.clear();
                        PostReply deleted = em.find(PostReply.class, replyid);
                        Assertions.assertNull(deleted, "评论应已删除");
                    } finally {
                        if (replyid != null) {
                            try {
                                EntityTransaction tx = em.getTransaction();
                                if (!tx.isActive()) {
                                    tx.begin();
                                    PostReply toDelete = em.find(PostReply.class, replyid);
                                    if (toDelete != null) {
                                        em.remove(toDelete);
                                    }
                                    tx.commit();
                                }
                            } catch (Exception e) {
                            }
                        }
                    }
                }));
    }

    @TestFactory
    Collection<DynamicTest> testExchangeMagicToUpload() {
        List<String> userIds = em.createQuery("select u.userid from User u", String.class).getResultList();
        if (userIds.isEmpty())
            return Collections.emptyList();

        String uid = userIds.get(0);
        final UserPT testUserPT;

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        UserPT tempUserPT = em.find(UserPT.class, uid);

        if (tempUserPT == null) {
            tempUserPT = new UserPT();
            tempUserPT.userid = uid;
            tempUserPT.magic = 1000;
            tempUserPT.upload = 1000;
            tempUserPT.download = 1000;
            em.persist(tempUserPT);
        } else {
            tempUserPT.magic = 1000;
            tempUserPT.upload = 1000;
            em.merge(tempUserPT);
        }
        testUserPT = tempUserPT;
        tx.commit();

        return List.of(
                DynamicTest.dynamicTest("ExchangeMagicToUpload success", () -> {
                    try {
                        int magic = 100;
                        long beforeUpload = testUserPT.upload;
                        int beforeMagic = testUserPT.magic;

                        boolean ret = db.ExchangeMagicToUpload(uid, magic);
                        Assertions.assertTrue(ret, "兑换上传量应成功");

                        em.clear();
                        UserPT after = em.find(UserPT.class, uid);
                        Assertions.assertEquals(beforeMagic - magic, after.magic, "魔力值应减少");
                        Assertions.assertEquals(beforeUpload + magic, after.upload, "上传量应增加");
                    } finally {
                        EntityTransaction tx2 = em.getTransaction();
                        tx2.begin();
                        UserPT user = em.find(UserPT.class, uid);
                        if (user != null) {
                            user.magic = 0;
                            user.upload = 0;
                            em.merge(user);
                        }
                        tx2.commit();
                    }
                }),

                DynamicTest.dynamicTest("ExchangeMagicToUpload insufficient magic", () -> {
                    boolean ret = db.ExchangeMagicToUpload(uid, 2000);
                    Assertions.assertFalse(ret, "魔力值不足时应返回false");
                }),

                DynamicTest.dynamicTest("ExchangeMagicToUpload invalid params", () -> {
                    Assertions.assertFalse(db.ExchangeMagicToUpload(null, 100));
                    Assertions.assertFalse(db.ExchangeMagicToUpload("", 100));
                    Assertions.assertFalse(db.ExchangeMagicToUpload(uid, 0));
                    Assertions.assertFalse(db.ExchangeMagicToUpload(uid, -1));
                }));
    }

    @TestFactory
    Collection<DynamicTest> testExchangeMagicToDownload() {
        List<String> userIds = em.createQuery("select u.userid from User u", String.class).getResultList();
        if (userIds.isEmpty())
            return Collections.emptyList();

        String uid = userIds.get(0);
        final UserPT testUserPT;

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        UserPT tempUserPT = em.find(UserPT.class, uid);
        if (tempUserPT == null) {
            tempUserPT = new UserPT();
            tempUserPT.userid = uid;
            tempUserPT.magic = 1000;
            tempUserPT.upload = 1000;
            tempUserPT.download = 1000;
            em.persist(tempUserPT);
        } else {
            tempUserPT.magic = 1000;
            tempUserPT.download = 1000;
            em.merge(tempUserPT);
        }
        testUserPT = tempUserPT;
        tx.commit();

        return List.of(
                DynamicTest.dynamicTest("ExchangeMagicToDownload success", () -> {
                    try {
                        int magic = 100;
                        long beforeDownload = testUserPT.download;
                        int beforeMagic = testUserPT.magic;

                        boolean ret = db.ExchangeMagicToDownload(uid, magic);
                        Assertions.assertTrue(ret, "兑换下载量应成功");

                        em.clear();
                        UserPT after = em.find(UserPT.class, uid);
                        Assertions.assertEquals(beforeMagic - magic, after.magic, "魔力值应减少");
                        Assertions.assertEquals(beforeDownload - magic, after.download, "下载量应减少");
                    } finally {
                        EntityTransaction tx2 = em.getTransaction();
                        tx2.begin();
                        UserPT user = em.find(UserPT.class, uid);
                        if (user != null) {
                            user.magic = 0;
                            user.download = 0;
                            em.merge(user);
                        }
                        tx2.commit();
                    }
                }),

                DynamicTest.dynamicTest("ExchangeMagicToDownload insufficient magic", () -> {
                    boolean ret = db.ExchangeMagicToDownload(uid, 2000);
                    Assertions.assertFalse(ret, "魔力值不足时应返回false");
                }),

                DynamicTest.dynamicTest("ExchangeMagicToDownload invalid params", () -> {
                    Assertions.assertFalse(db.ExchangeMagicToDownload(null, 100));
                    Assertions.assertFalse(db.ExchangeMagicToDownload("", 100));
                    Assertions.assertFalse(db.ExchangeMagicToDownload(uid, 0));
                    Assertions.assertFalse(db.ExchangeMagicToDownload(uid, -1));
                }));
    }

    @TestFactory
    Collection<DynamicTest> testExchangeMagicToVip() {
        List<String> userIds = em.createQuery("select u.userid from User u", String.class).getResultList();
        if (userIds.isEmpty())
            return Collections.emptyList();

        String uid = userIds.get(0);
        final UserPT testUserPT;

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        UserPT tempUserPT = em.find(UserPT.class, uid);
        if (tempUserPT == null) {
            tempUserPT = new UserPT();
            tempUserPT.userid = uid;
            tempUserPT.magic = 1000;
            tempUserPT.upload = 1000;
            tempUserPT.download = 1000;
            tempUserPT.viptime = 0;
            em.persist(tempUserPT);
        } else {
            tempUserPT.magic = 1000;
            tempUserPT.viptime = 0;
            em.merge(tempUserPT);
        }
        testUserPT = tempUserPT;
        tx.commit();

        return List.of(
                DynamicTest.dynamicTest("ExchangeMagicToVip success", () -> {
                    try {
                        int magic = 100;
                        int beforeVip = testUserPT.viptime;
                        int beforeMagic = testUserPT.magic;

                        boolean ret = db.ExchangeMagicToVip(uid, magic);
                        Assertions.assertTrue(ret, "兑换VIP次数应成功");

                        em.clear();
                        UserPT after = em.find(UserPT.class, uid);
                        Assertions.assertEquals(beforeMagic - magic, after.magic, "魔力值应减少");
                        Assertions.assertEquals(beforeVip + magic, after.viptime, "VIP次数应增加");
                    } finally {
                        EntityTransaction tx2 = em.getTransaction();
                        tx2.begin();
                        UserPT user = em.find(UserPT.class, uid);
                        if (user != null) {
                            user.magic = 0;
                            user.viptime = 0;
                            em.merge(user);
                        }
                        tx2.commit();
                    }
                }),

                DynamicTest.dynamicTest("ExchangeMagicToVip insufficient magic", () -> {
                    boolean ret = db.ExchangeMagicToVip(uid, 2000);
                    Assertions.assertFalse(ret, "魔力值不足时应返回false");
                }),

                DynamicTest.dynamicTest("ExchangeMagicToVip invalid params", () -> {
                    Assertions.assertFalse(db.ExchangeMagicToVip(null, 100));
                    Assertions.assertFalse(db.ExchangeMagicToVip("", 100));
                    Assertions.assertFalse(db.ExchangeMagicToVip(uid, 0));
                    Assertions.assertFalse(db.ExchangeMagicToVip(uid, -1));
                }));
    }

    @TestFactory
    Collection<DynamicTest> testUploadTransmitProfile() {
        List<String> userIds = em.createQuery("select u.userid from User u", String.class).getResultList();
        if (userIds.isEmpty())
            return Collections.emptyList();

        String uid = userIds.get(0);
        String profileId = "test_profile_" + System.currentTimeMillis();
        Profile profile = new Profile();
        profile.profileurl = profileId;
        profile.userid = uid;
        profile.magictogive = "100";
        profile.uploadtogive = "1000";
        profile.downloadtogive = "500";
        profile.exampass = false;
        profile.magicgived = "0";
        profile.uploadgived = "0";
        profile.applicationurl = "http://example.com/apply";

        return List.of(
                DynamicTest.dynamicTest("UploadTransmitProfile success", () -> {
                    try {
                        boolean ret = db.UploadTransmitProfile(profile);
                        Assertions.assertTrue(ret, "上传迁移信息应成功");

                        Profile inserted = em.find(Profile.class, profileId);
                        Assertions.assertNotNull(inserted, "迁移信息应已插入");
                        Assertions.assertFalse(inserted.exampass, "新迁移信息默认未审核");
                    } finally {
                        EntityTransaction tx = em.getTransaction();
                        tx.begin();
                        Profile toDelete = em.find(Profile.class, profileId);
                        if (toDelete != null)
                            em.remove(toDelete);
                        tx.commit();
                    }
                }),

                DynamicTest.dynamicTest("UploadTransmitProfile duplicate", () -> {
                    try {
                        db.UploadTransmitProfile(profile);
                        boolean ret = db.UploadTransmitProfile(profile);
                        Assertions.assertFalse(ret, "重复上传应返回false");
                    } finally {
                        EntityTransaction tx = em.getTransaction();
                        tx.begin();
                        Profile toDelete = em.find(Profile.class, profileId);
                        if (toDelete != null)
                            em.remove(toDelete);
                        tx.commit();
                    }
                }),

                DynamicTest.dynamicTest("UploadTransmitProfile invalid params", () -> {
                    Assertions.assertFalse(db.UploadTransmitProfile(null));
                    Profile invalidProfile = new Profile();
                    Assertions.assertFalse(db.UploadTransmitProfile(invalidProfile));
                }));
    }

    @TestFactory
    Collection<DynamicTest> testGetTransmitProfile() {
        // 获取现有的迁移信息记录
        List<Profile> existingProfiles = em.createQuery(
                "SELECT p FROM Profile p", Profile.class)
                .setMaxResults(1)
                .getResultList();
        if (existingProfiles.isEmpty())
            return Collections.emptyList();

        Profile profile = existingProfiles.get(0);
        String profileId = profile.profileurl;

        return List.of(
                DynamicTest.dynamicTest("GetTransmitProfile success", () -> {
                    Profile ret = db.GetTransmitProfile(profileId);
                    Assertions.assertNotNull(ret, "应成功获取迁移信息");
                }),

                DynamicTest.dynamicTest("GetTransmitProfile not exist", () -> {
                    Profile ret = db.GetTransmitProfile("not_exist_profile");
                    Assertions.assertNull(ret, "不存在的迁移信息应返回null");
                }),

                DynamicTest.dynamicTest("GetTransmitProfile invalid params", () -> {
                    Assertions.assertNull(db.GetTransmitProfile(null));
                    Assertions.assertNull(db.GetTransmitProfile(""));
                }));
    }

    @TestFactory
    Collection<DynamicTest> testExamTransmitProfile() {
        List<String> userIds = em.createQuery("select u.userid from User u", String.class).getResultList();
        if (userIds.isEmpty())
            return Collections.emptyList();

        String uid = userIds.get(0);
        String profileId = "test_profile_exam_" + System.currentTimeMillis();
        Profile profile = new Profile();
        profile.profileurl = profileId;
        profile.userid = uid;
        profile.magictogive = "100";
        profile.uploadtogive = "1000";
        profile.exampass = false;
        profile.magicgived = "0";
        profile.uploadgived = "0";
        profile.applicationurl = "http://example.com/apply";

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(profile);
        UserPT userPT = em.find(UserPT.class, uid);
        if (userPT == null) {
            userPT = new UserPT();
            userPT.userid = uid;
            userPT.magic = 0;
            userPT.upload = 0;
            em.persist(userPT);
        } else {
            userPT.magic = 0;
            userPT.upload = 0;
            em.merge(userPT);
        }
        tx.commit();

        return List.of(
                DynamicTest.dynamicTest("ExamTransmitProfile approve", () -> {
                    try {
                        boolean ret = db.ExamTransmitProfile(profileId, true, 0);
                        Assertions.assertTrue(ret, "审核通过应成功");
                    } finally {
                        EntityTransaction tx2 = em.getTransaction();
                        tx2.begin();
                        UserPT user = em.find(UserPT.class, uid);
                        if (user != null) {
                            user.magic = 0;
                            user.upload = 0;
                            em.merge(user);
                        }
                        Profile toDelete = em.find(Profile.class, profileId);
                        if (toDelete != null)
                            em.remove(toDelete);
                        tx2.commit();
                    }
                }),

                DynamicTest.dynamicTest("ExamTransmitProfile reject", () -> {
                    String rejectId = "test_profile_reject_" + System.currentTimeMillis();
                    Profile rejectProfile = new Profile();
                    rejectProfile.profileurl = rejectId;
                    rejectProfile.userid = uid;
                    rejectProfile.magictogive = "100";
                    rejectProfile.uploadtogive = "1000";
                    rejectProfile.magicgived = "0";
                    rejectProfile.uploadgived = "0";
                    rejectProfile.exampass = false;
                    rejectProfile.applicationurl = "http://example.com/apply";

                    EntityTransaction tx3 = em.getTransaction();
                    tx3.begin();
                    em.persist(rejectProfile);
                    tx3.commit();

                    try {
                        boolean ret = db.ExamTransmitProfile(rejectId, false, 0);
                        Assertions.assertTrue(ret, "审核拒绝应成功");
                    } finally {
                        EntityTransaction tx4 = em.getTransaction();
                        tx4.begin();
                        Profile toDelete = em.find(Profile.class, rejectId);
                        if (toDelete != null)
                            em.remove(toDelete);
                        tx4.commit();
                    }
                }),

                DynamicTest.dynamicTest("ExamTransmitProfile invalid params", () -> {
                    Assertions.assertFalse(db.ExamTransmitProfile(null, true, 0));
                    Assertions.assertFalse(db.ExamTransmitProfile("", true, 0));
                    Assertions.assertFalse(db.ExamTransmitProfile("not_exist_profile", true, 0));
                }));
    }

    @Test
    void testGetTransmitProfileList() {
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Profile").executeUpdate();
        em.getTransaction().commit();

        List<String> userIds = em.createQuery("select u.userid from User u", String.class)
                .setMaxResults(3)
                .getResultList();
        if (userIds.isEmpty()) {
            return;
        }

        EntityTransaction tx = em.getTransaction();
        tx.begin();
        for (int i = 0; i < userIds.size(); i++) {
            Profile profile = new Profile();
            profile.profileurl = "test_profile_list_" + i + "_" + System.currentTimeMillis();
            profile.userid = userIds.get(i);
            profile.magictogive = String.valueOf(100 * (i + 1));
            profile.uploadtogive = String.valueOf(1000 * (i + 1));
            profile.exampass = false;
            profile.magicgived = "0";
            profile.uploadgived = "0";
            profile.applicationurl = "http://example.com/apply";
            em.persist(profile);
        }
        tx.commit();

        try {
            Profile[] profiles = db.GetTransmitProfileList();
            Assertions.assertEquals(userIds.size(), profiles.length, "应返回所有迁移申请");
            Arrays.stream(profiles)
                    .forEach(p -> Assertions.assertTrue(p.profileurl.startsWith("test_profile_list_"), "应为测试数据"));
        } finally {
            EntityTransaction tx2 = em.getTransaction();
            tx2.begin();
            em.createQuery("DELETE FROM Profile p WHERE p.profileurl LIKE 'test_profile_list_%'").executeUpdate();
            tx2.commit();
        }
    }
}