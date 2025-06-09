package database;

import java.util.Calendar;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.core.Tuple;
import entity.QBegInfo;
import entity.QUserVotes;
import entity.QSubmitSeed;
import entity.QProfile;
import entity.QSeed;
import entity.Seed;
import entity.SeedPromotion;
import entity.SeedWithPromotionDTO;
import entity.User;
import entity.UserPT;
import entity.config;
import entity.Notice;
import entity.BegInfo;
import entity.BegInfoDetail;
import entity.BegSeedDetail;
import entity.Post;
import entity.PostReply;
import entity.Profile;
import entity.UserStar;
import entity.SeedWithVotes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database2 implements DataManagerInterface {

    private EntityManagerFactory emf;
    private static final Logger logger = LoggerFactory.getLogger(Database2.class);

    // 构造函数，初始化EntityManagerFactory
    public Database2() {
        config cfg = new config();
        Map<String, Object> props = new HashMap<>();
        props.put("javax.persistence.jdbc.url",
                "jdbc:mysql://" + cfg.SqlURL + "/" + cfg.Database);
        props.put("javax.persistence.jdbc.user", cfg.SqlUsername);
        props.put("javax.persistence.jdbc.password", cfg.SqlPassword);
        this.emf = Persistence.createEntityManagerFactory("myPersistenceUnit", props);
    }

    public Database2(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public String LoginUser(User userinfo){
        return null;
    }

    @Override
    public int RegisterUser(User userinfo) {
        return 0;
    }

    @Override
    public int UpdateInformation(User userinfo) {
        return 0;
    }

    @Override
    public User GetInformation(String userid) {
        return null;
    }

    @Override
    public UserPT GetInformationPT(String userid) {
        return null;
    }

    @Override
    public int UpdateInformationPT(UserPT userinfo) {
        return 0;
    }

    @Override
    public int RegisterUserPT(UserPT userinfo) {
        return 0;
    }

    @Override
    public Seed GetSeedInformation(String seedid) {
        return null;
    }

    @Override
    public int RegisterSeed(Seed seedinfo) {
        return 0;
    }

    @Override
    public int UpdateSeed(Seed seedinfo) {
        return 0;
    }

    @Override
    public Seed[] SearchSeed(String userQ) {
        return new Seed[0];
    }

    @Override
    public int AddNotice(Notice notice) {
        return 0;
    }

    @Override
    public boolean UpdateNotice(Notice notice) {
        return false;
    }

    @Override
    public boolean DeleteNotice(String noticeid) {
        return false;
    }

    @Override
    public int GetUserAvailableInviteTimes(String userid) {
        return 0;
    }

    @Override
    public int InviteUser(String inviterid, String inviteemail) {
        return 0;
    }

    @Override
    public SeedWithPromotionDTO[] GetSeedListByTag(String tag) {
        return new SeedWithPromotionDTO[0];
    }

    @Override
    public Seed[] GetSeedListByUser(String userid) {
        return new Seed[0];
    }

    @Override
    public int DeleteSeed(String seedid) {
        return 0;
    }

    @Override
    public boolean AddCollect(String userid, String postid) {
        return false;
    }

    @Override
    public boolean DeleteCollect(String userid, String postid) {
        return false;
    }

    @Override
    public int AddBegSeed(BegInfo info) {
        if (info == null || info.begid == null || info.begid.isEmpty()) {
            logger.warn("Invalid parameter: info is null or begid is empty");
            return 2;
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 检查是否重复
            BegInfo existingBeg = em.find(BegInfo.class, info.begid);
            if (existingBeg != null) {
                logger.warn("BegSeed with ID {} already exists", info.begid);
                return 1;
            }

            // 设置默认值
            if (info.endtime == null) {
                // 设置默认14天截止期
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, 14);
                info.endtime = calendar.getTime();
            }
            info.hasseed = 0;

            // 保存新的求种信息
            em.persist(info);
            tx.commit();

            logger.info("Successfully added new BegSeed with ID: {}", info.begid);
            return 0;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error adding BegSeed: {}", e.getMessage());
            return 2;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public int UpdateBegSeed(BegInfo info) {
        if (info == null || info.begid == null || info.begid.isEmpty()) {
            logger.warn("Invalid parameter: info is null or begid is empty");
            return 2;
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 检查是否存在
            BegInfo existingBeg = em.find(BegInfo.class, info.begid);
            if (existingBeg == null) {
                logger.warn("BegSeed with ID {} does not exist", info.begid);
                return 1;
            }

            // 保持原有值不变的字段
            info.hasseed = existingBeg.hasseed;
            if (info.endtime == null) {
                info.endtime = existingBeg.endtime;
            }

            // 更新求种信息
            em.merge(info);
            tx.commit();

            logger.info("Successfully updated BegSeed with ID: {}", info.begid);
            return 0;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error updating BegSeed: {}", e.getMessage());
            return 2;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public int DeleteBegSeed(String begid) {
        if (begid == null || begid.isEmpty()) {
            logger.warn("Invalid parameter: begid is null or empty");
            return 2;
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 查找要删除的求种信息
            BegInfo begInfo = em.find(BegInfo.class, begid);
            if (begInfo == null) {
                logger.warn("BegSeed with ID {} does not exist", begid);
                tx.rollback();
                return 1;
            }

            // 删除求种信息
            em.remove(begInfo);
            tx.commit();

            logger.info("Successfully deleted BegSeed with ID: {}", begid);
            return 0;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error deleting BegSeed: {}", e.getMessage());
            return 2;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public int VoteSeed(String begId, String seedId, String userId) {
        if (begId == null || seedId == null || userId == null ||
                begId.isEmpty() || seedId.isEmpty() || userId.isEmpty()) {
            logger.warn("Invalid parameters: begId, seedId or userId is null or empty");
            return 2;
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 检查求种信息是否存在
            BegInfo begInfo = em.find(BegInfo.class, begId);
            if (begInfo == null) {
                logger.warn("BegSeed with ID {} does not exist", begId);
                return 2;
            }

            // 检查用户是否已投票
            Long voteCount = new JPAQuery<>(em)
                    .select(QUserVotes.userVotes.count())
                    .from(QUserVotes.userVotes)
                    .where(QUserVotes.userVotes.id.eq(new entity.UserVotesId(userId, begId, seedId)))
                    .fetchOne();

            if (voteCount != null && voteCount > 0) {
                logger.warn("User {} has already voted for seed {} in beg {}", userId, seedId, begId);
                return 1;
            }

            // 创建新的投票记录
            em.createNativeQuery("INSERT INTO UserVotes (user_id, beg_id, seed_id, created_at) " +
                    "VALUES (?, ?, ?, CURRENT_TIMESTAMP)")
                    .setParameter(1, userId)
                    .setParameter(2, begId)
                    .setParameter(3, seedId)
                    .executeUpdate();

            // 更新SubmitSeed表中的投票数
            em.createQuery("UPDATE SubmitSeed s SET s.votes = s.votes + 1 " +
                    "WHERE s.id.begId = :begId AND s.id.seedId = :seedId")
                    .setParameter("begId", begId)
                    .setParameter("seedId", seedId)
                    .executeUpdate();

            tx.commit();
            logger.info("Successfully added vote from user {} for seed {} in beg {}", userId, seedId, begId);
            return 0;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error voting for seed: {}", e.getMessage());
            return 2;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public int SubmitSeed(String begid, Seed seed) {
        if (begid == null || seed == null || begid.isEmpty() || seed.seedid == null) {
            logger.warn("Invalid parameters: begid or seed is null or empty");
            return 2;
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 检查求种信息是否存在
            BegInfo begInfo = em.find(BegInfo.class, begid);
            if (begInfo == null) {
                logger.warn("BegSeed with ID {} does not exist", begid);
                return 2;
            }

            // 检查种子是否已提交过
            QSubmitSeed ss = QSubmitSeed.submitSeed;
            Long submitCount = new JPAQuery<>(em)
                    .select(ss.count())
                    .from(ss)
                    .where(ss.begInfo.begid.eq(begid))
                    .where(ss.seed.seedid.eq(seed.seedid))
                    .fetchOne();

            if (submitCount > 0) {
                logger.warn("Seed {} has already been submitted for beg {}", seed.seedid,
                        begid);
                return 1;
            }

            // 保存种子信息（如果不存在）
            if (em.find(Seed.class, seed.seedid) == null) {
                em.persist(seed);
            }

            // 创建提交记录
            em.createNativeQuery("INSERT INTO SubmitSeed (beg_id, seed_id, votes) VALUES (?, ?, 0)")
                    .setParameter(1, begid)
                    .setParameter(2, seed.seedid)
                    .executeUpdate();

            tx.commit();
            logger.info("Successfully submitted seed {} for beg {}", seed.seedid, begid);
            return 0;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error submitting seed: {}", e.getMessage());
            return 2;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public void SettleBeg() {
        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. 获取所有已过期且未完成的求种信息
            QBegInfo b = QBegInfo.begInfo;
            List<BegInfo> expiredBegs = new JPAQuery<>(em)
                    .select(b)
                    .from(b)
                    .where(b.endtime.loe(new Date())
                            .and(b.hasseed.eq(0)))
                    .fetch();

            for (BegInfo beg : expiredBegs) {
                // 2. 查找投票最多的提交任务
                QSubmitSeed ss = QSubmitSeed.submitSeed;
                Tuple topSubmission = new JPAQuery<>(em)
                        .select(ss.seed.seedid, ss.votes)
                        .from(ss)
                        .where(ss.begInfo.begid.eq(beg.begid))
                        .orderBy(ss.votes.desc())
                        .limit(1)
                        .fetchOne();

                if (topSubmission != null && topSubmission.get(ss.votes) > 0) {
                    String seedId = topSubmission.get(ss.seed.seedid);

                    // 3. 获取上传者ID
                    QSeed s = QSeed.seed;
                    String ownerId = new JPAQuery<>(em)
                            .select(s.seeduserid)
                            .from(s)
                            .where(s.seedid.eq(seedId))
                            .fetchOne();

                    // 4. 获取上传者的PT信息并更新魔力值
                    UserPT ownerPT = em.find(UserPT.class, ownerId);
                    if (ownerPT != null) {
                        // 5. 发放奖励
                        ownerPT.magic += beg.magic;
                        em.merge(ownerPT);

                        // 6. 更新求种状态
                        beg.hasseed = 1;
                        em.merge(beg);

                        logger.info("Reward {} magic points awarded to user {} for beg {}",
                                beg.magic, ownerId, beg.begid);
                    }
                }
            }

            tx.commit();
            logger.info("Successfully settled {} expired beg requests",
                    expiredBegs.size());

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error settling beg requests: {}", e.getMessage(), e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public int AddPost(Post post) {
        if (post == null || post.postid == null || post.postid.isEmpty()) {
            logger.warn("Invalid parameter: post is null or postid is empty");
            return 2;
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 检查是否重复
            Post existingPost = em.find(Post.class, post.postid);
            if (existingPost != null) {
                logger.warn("Post with ID {} already exists", post.postid);
                return 1;
            }

            // 检查用户是否存在
            User user = em.find(User.class, post.postuserid);
            if (user == null) {
                logger.warn("User with ID {} does not exist", post.postuserid);
                return 2;
            }

            // 设置初始值
            if (post.posttime == null) {
                post.posttime = new Date();
            }
            post.replytime = 0;
            post.readtime = 0;

            // 保存帖子
            em.persist(post);
            tx.commit();

            logger.info("Successfully added new post with ID: {}", post.postid);
            return 0;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error adding post: {}", e.getMessage());
            return 2;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public int UpdatePost(Post post) {
        if (post == null || post.postid == null || post.postid.isEmpty()) {
            logger.warn("Invalid parameter: post is null or postid is empty");
            return 2;
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 检查帖子是否存在
            Post existingPost = em.find(Post.class, post.postid);
            if (existingPost == null) {
                logger.warn("Post with ID {} does not exist", post.postid);
                return 1;
            }

            // 保持原有不可修改的字段
            post.postuserid = existingPost.postuserid;
            post.posttime = existingPost.posttime;
            post.replytime = existingPost.replytime;
            post.readtime = existingPost.readtime;

            // 更新帖子
            em.merge(post);
            tx.commit();

            logger.info("Successfully updated post with ID: {}", post.postid);
            return 0;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error updating post: {}", e.getMessage());
            return 2;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public int DeletePost(String postid) {
        if (postid == null || postid.isEmpty()) {
            logger.warn("Invalid parameter: postid is null or empty");
            return 2;
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 查找要删除的帖子
            Post post = em.find(Post.class, postid);
            if (post == null) {
                logger.warn("Post with ID {} does not exist", postid);
                tx.rollback();
                return 1;
            }

            // 删除帖子(由于设置了级联删除，相关的回复会自动删除)
            em.remove(post);
            tx.commit();

            logger.info("Successfully deleted post with ID: {}", postid);
            return 0;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error deleting post: {}", e.getMessage());
            return 2;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public int AddComment(String postid, String userid, String comment) {
        if (postid == null || postid.isEmpty() ||
                userid == null || userid.isEmpty() ||
                comment == null || comment.isEmpty()) {
            logger.warn("Invalid parameters: postid, userid or comment is null or empty");
            return 2;
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 检查帖子是否存在
            Post post = em.find(Post.class, postid);
            if (post == null) {
                logger.warn("Post with ID {} does not exist", postid);
                return 1;
            }

            // 检查评论用户是否存在
            User user = em.find(User.class, userid);
            if (user == null) {
                logger.warn("User with ID {} does not exist", userid);
                return 1;
            }

            // 创建新的回复
            PostReply reply = new PostReply();
            reply.replyid = UUID.randomUUID().toString();
            reply.postid = postid;
            reply.content = comment;
            reply.authorid = userid;
            reply.createdAt = new Date();

            // 保存回复
            em.persist(reply);

            // 更新帖子的回复数
            post.replytime += 1;
            em.merge(post);

            tx.commit();

            logger.info("Successfully added comment by user {} to post {}, reply ID: {}",
                    userid, postid, reply.replyid);
            return 0;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error adding comment: {}", e.getMessage());
            return 2;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public int DeleteComment(String postid, String commentid) {
        if (postid == null || postid.isEmpty() || commentid == null || commentid.isEmpty()) {
            logger.warn("Invalid parameters: postid or commentid is null or empty");
            return 2;
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 检查帖子是否存在
            Post post = em.find(Post.class, postid);
            if (post == null) {
                logger.warn("Post with ID {} does not exist", postid);
                return 1;
            }

            // 检查评论是否存在且属于该帖子
            PostReply reply = em.find(PostReply.class, commentid);
            if (reply == null || !reply.postid.equals(postid)) {
                logger.warn("Comment {} does not exist or does not belong to post {}", commentid, postid);
                return 1;
            }

            // 删除评论
            em.remove(reply);

            // 更新帖子的回复数
            post.replytime = Math.max(0, post.replytime - 1);
            em.merge(post);

            tx.commit();

            logger.info("Successfully deleted comment {} from post {}", commentid, postid);
            return 0;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error deleting comment: {}", e.getMessage());
            return 2;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public boolean ExchangeMagicToUpload(String userid, int magic) {
        if (userid == null || userid.isEmpty() || magic <= 0) {
            logger.warn("参数无效: userid为空或magic <= 0");
            return false;
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            UserPT userPT = em.find(UserPT.class, userid);
            if (userPT == null) {
                logger.warn("未找到用户 {} 的PT信息", userid);
                return false;
            }

            if (userPT.magic < magic) {
                logger.warn("用户 {} 的魔力值不足", userid);
                return false;
            }

            // 1:1兑换，直接加上魔力值（假设单位是MB）
            userPT.magic -= magic;
            userPT.upload += magic;

            // 更新分享率
            if (userPT.download > 0) {
                userPT.share = (double) userPT.upload / userPT.download;
            }

            em.merge(userPT);
            tx.commit();

            logger.info("用户 {} 成功将 {} 点魔力值兑换为 {}MB 上传量", userid, magic, magic);
            return true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("魔力值兑换上传量时发生错误: {}", e.getMessage());
            return false;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public boolean ExchangeMagicToDownload(String userid, int magic) {
        if (userid == null || userid.isEmpty() || magic <= 0) {
            logger.warn("参数无效: userid为空或magic <= 0");
            return false;
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            UserPT userPT = em.find(UserPT.class, userid);
            if (userPT == null) {
                logger.warn("未找到用户 {} 的PT信息", userid);
                return false;
            }

            if (userPT.magic < magic) {
                logger.warn("用户 {} 的魔力值不足", userid);
                return false;
            }

            // 1:1兑换，直接减去魔力值对应的下载量（假设单位是MB）
            userPT.magic -= magic;
            userPT.download = Math.max(0, userPT.download - magic);

            // 更新分享率
            if (userPT.download > 0) {
                userPT.share = (double) userPT.upload / userPT.download;
            }

            em.merge(userPT);
            tx.commit();

            logger.info("用户 {} 成功将 {} 点魔力值兑换为 {}MB 下载量减免", userid, magic, magic);
            return true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("魔力值兑换下载量时发生错误: {}", e.getMessage());
            return false;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public boolean ExchangeMagicToVip(String userid, int magic) {
        if (userid == null || userid.isEmpty() || magic <= 0) {
            logger.warn("参数无效: userid为空或magic <= 0");
            return false;
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            UserPT userPT = em.find(UserPT.class, userid);
            if (userPT == null) {
                logger.warn("未找到用户 {} 的PT信息", userid);
                return false;
            }

            if (userPT.magic < magic) {
                logger.warn("用户 {} 的魔力值不足", userid);
                return false;
            }

            // 1:1兑换VIP下载次数
            userPT.magic -= magic;
            userPT.viptime += magic;

            em.merge(userPT);
            tx.commit();

            logger.info("用户 {} 成功将 {} 点魔力值兑换为 {} 次VIP下载次数", userid, magic, magic);
            return true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("魔力值兑换VIP下载次数时发生错误: {}", e.getMessage());
            return false;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public boolean UploadTransmitProfile(Profile profile) {
        if (profile == null || profile.profileurl == null || profile.profileurl.isEmpty() ||
                profile.userid == null || profile.userid.isEmpty()) {
            logger.warn("参数无效: profile为空或必要字段为空");
            return false;
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 检查用户是否存在
            User user = em.find(User.class, profile.userid);
            if (user == null) {
                logger.warn("用户 {} 不存在", profile.userid);
                return false;
            }

            // 检查是否已存在相同的迁移申请
            Profile existingProfile = em.find(Profile.class, profile.profileurl);
            if (existingProfile != null) {
                logger.warn("迁移申请 {} 已存在", profile.profileurl);
                return false;
            }

            // 设置初始值
            profile.exampass = false;
            profile.magicgived = "0";
            profile.uploadgived = "0";

            // 保存迁移申请
            em.persist(profile);
            tx.commit();

            logger.info("成功上传迁移申请 {}", profile.profileurl);
            return true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("上传迁移申请时发生错误: {}", e.getMessage());
            return false;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public Profile GetTransmitProfile(String profileid) {
        if (profileid == null || profileid.isEmpty()) {
            logger.warn("参数无效: profileid为空");
            return null;
        }

        EntityManager em = null;

        try {
            em = emf.createEntityManager();
            Profile profile = em.find(Profile.class, profileid);
            if (profile == null) {
                logger.warn("未找到迁移申请 {}", profileid);
                return null;
            }

            logger.info("成功获取迁移申请 {}", profileid);
            return profile;

        } catch (Exception e) {
            logger.error("获取迁移申请时发生错误: {}", e.getMessage());
            return null;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public boolean ExamTransmitProfile(String profileid, boolean result, Integer grantedUpload) {
        if (profileid == null || profileid.isEmpty()) {
            logger.warn("参数无效: profileid为空");
            return false;
        }

        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            em = emf.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 查找迁移申请
            Profile profile = em.find(Profile.class, profileid);
            if (profile == null) {
                logger.warn("未找到迁移申请 {}", profileid);
                return false;
            }

            // 更新审核状态
            profile.exampass = result;

            if (result) {
                // 如果审核通过，更新用户的PT信息
                UserPT userPT = em.find(UserPT.class, profile.userid);
                if (userPT != null) {
                    // 发放魔力值
                    int magicToGive = Integer.parseInt(profile.magictogive);
                    userPT.magic += magicToGive;
                    profile.magicgived = String.valueOf(magicToGive);

                    // 发放上传量
                    long uploadToGive = Long.parseLong(profile.uploadtogive);
                    userPT.upload += uploadToGive;
                    profile.uploadgived = String.valueOf(uploadToGive);

                    em.merge(userPT);
                }
            }

            em.merge(profile);
            tx.commit();

            logger.info("成功审核迁移申请 {}, 结果: {}", profileid, result);
            return true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("审核迁移申请时发生错误: {}", e.getMessage());
            return false;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public Profile[] GetTransmitProfileList() {
        EntityManager em = null;

        try {
            em = emf.createEntityManager();

            // 获取所有迁移申请
            QProfile p = QProfile.profile;
            List<Profile> profiles = new JPAQuery<>(em)
                    .select(p)
                    .from(p)
                    .fetch();

            logger.info("成功获取所有迁移申请，共 {} 条", profiles.size());
            return profiles.toArray(new Profile[0]);

        } catch (Exception e) {
            logger.error("获取迁移申请列表时发生错误: {}", e.getMessage());
            return new Profile[0];
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public Post[] GetPostList(){
        return null;
    }

    @Override
    public Post GetPost(String postid) {
        return null;
    }

    @Override
    public PostReply[] GetPostReplyList(String postid) {
        return null;
    }

    @Override
    public Post[] SearchPost(String userQ) {
        return null;
    }

    @Override
    public int CheckAdmin(String userid) {
        return 0;
    }

    @Override
    public int InviteNewUser(String inviterid, String invitedemail) {
        return 0;
    }

    @Override
    public UserStar[] GetUserStarList(String userid) {
        return new UserStar[0];
    }

    @Override
    public int UploadMigration(String userid, File file, String uploadtogive){
        return 0;
    }

    @Override
    public BegSeedDetail[] GetBegList() {
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            JPAQuery<BegInfo> query = new JPAQuery<>(em);
            QBegInfo b = QBegInfo.begInfo;
            List<BegInfo> begList = query.select(b).from(b).fetch();
            
            List<BegSeedDetail> begSeedDetailList = new ArrayList<>();
            for (BegInfo begInfo : begList) {
                // 查询对应的BegInfo表的Info字段
                BegInfoDetail begInfoDetail = em.find(BegInfoDetail.class, begInfo.begid);
                // 构造BegSeedDetail对象
                BegSeedDetail begSeedDetail = new BegSeedDetail(begInfo, begInfoDetail);
                begSeedDetailList.add(begSeedDetail);
            }
            
            return begSeedDetailList.toArray(new BegSeedDetail[0]);
        } catch (Exception e) {
            logger.error("Error getting BegList: {}", e.getMessage(), e);
            return new BegSeedDetail[0];
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public BegInfo GetBegDetail(String begid) {
        return null;
    }

    @Override
    public BegSeedDetail GetBegSeedDetail(String begid) {
        if (begid == null || begid.isEmpty()) {
            return null;
        }

        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            
            // 查询 BegSeed 表数据
            JPAQuery<BegInfo> begQuery = new JPAQuery<>(em);
            QBegInfo b = QBegInfo.begInfo;
            BegInfo begInfo = begQuery.select(b).from(b).where(b.begid.eq(begid)).fetchOne();
            
            if (begInfo == null) {
                return null;
            }

            // 查询 BegInfo 表的 Info 字段
            BegInfoDetail begInfoDetail = em.find(BegInfoDetail.class, begid);
            
            // 构造返回对象
            return new BegSeedDetail(begInfo, begInfoDetail);
        } catch (Exception e) {
            logger.error("Error getting BegSeedDetail: {}", e.getMessage(), e);
            return null;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    @Override
    public SeedWithVotes[] GetBegSeedListWithVotes(String begid) {
        return new SeedWithVotes[0];
    }

    @Override
    public int SubmitBegSeed(String begid, String seedid, String userid) {
        return 0;
    }

    @Override
    public int createBagSeed(BegInfo begInfo, String userid, String info) {
        return 0;
    }

    @Override
    public Seed[] getAllSeeds() {
        return null;
    }

    @Override
    public SeedPromotion[] getAllSeedPromotions() {
        return null;
    }

    @Override
    public int createSeedPromotion(String seedid, Date startTime, Date endTime, Integer discount) {
        return 0;
    }
}