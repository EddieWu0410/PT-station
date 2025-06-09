package database;

import java.util.Calendar;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.Persistence;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.core.Tuple;

import entity.BegInfo;
import entity.BegInfoDetail;
import entity.BegSeedDetail;
import entity.Notice;
import entity.Post;
import entity.PostReply;
import entity.Profile;
import entity.SeedWithVotes;
import entity.QAdmin;
import entity.QBegInfo;
import entity.QNotice;
import entity.QProfile;
import entity.QSeed;
import entity.QSubmitSeed;
import entity.QUser;
import entity.QUserPT;
import entity.QUserStar;
import entity.QUserInvite;
import entity.QUserVotes;
import entity.QPost;
import entity.Seed;
import entity.SeedPromotion;
import entity.SeedWithPromotionDTO;
import entity.User;
import entity.UserPT;
import entity.UserStar;
import entity.config;
import entity.PostReply;
import entity.QPostReply;
import entity.UserInvite;
import entity.QAdmin;
import entity.UserStar;
import entity.QUserStar;
import entity.SubmitSeed;
import entity.SubmitSeedId;
import entity.QSeedPromotion;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database1 implements DataManagerInterface {
    private static final Logger logger = LoggerFactory.getLogger(Database1.class);
    private EntityManagerFactory emf;
    
    public Database1() {
        config cfg = new config();
        Map<String,Object> props = new HashMap<>();
        props.put("javax.persistence.jdbc.url",
                  "jdbc:mysql://" + cfg.SqlURL + "/" + cfg.Database);
        props.put("javax.persistence.jdbc.user", cfg.SqlUsername);
        props.put("javax.persistence.jdbc.password", cfg.SqlPassword);
        // 只创建一个 EntityManagerFactory，为每个操作创建新的 EntityManager
        this.emf = Persistence.createEntityManagerFactory("myPersistenceUnit", props);
    }
    
    // 为每个操作创建新的 EntityManager 以避免线程安全问题
    private EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public String LoginUser(User userinfo){
        try {
            // 检查传入的参数是否合法
            if (userinfo == null || userinfo.password == null) {
                return null;
            }
            
            boolean hasEmail = userinfo.email != null && !userinfo.email.isEmpty();
            
            // 如果两个都为空或两个都不为空，返回null
            if (!hasEmail) {
                return null;
            }
            
            EntityManager entitymanager = createEntityManager();
            JPAQuery<User> query = new JPAQuery<>(entitymanager);
            QUser u = QUser.user;
            User foundUser = null;
            
            
            // 通过邮箱和密码查找
            foundUser = query.select(u)
                .from(u)
                .where(u.email.eq(userinfo.email)
                    .and(u.password.eq(userinfo.password)))
                .fetchOne();
            
            // 如果找到匹配的用户则返回用户ID，否则返回null
            return foundUser != null ? foundUser.userid : null;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 返回状态：0 success，1 邮箱重复，2其他原因
    @Override
    public int RegisterUser(User userinfo){
        EntityManager entitymanager = createEntityManager();
        try{
            // 首先检查该邮箱是否在UserInvite表中被邀请过
            JPAQuery<UserInvite> inviteQuery = new JPAQuery<>(entitymanager);
            QUserInvite ui = QUserInvite.userInvite;
            List<UserInvite> UserInvites = inviteQuery.select(ui).from(ui).where(ui.inviterEmail.eq(userinfo.email)).fetch();

            // 如果邮箱不在被邀请列表中，拒绝注册
            if(UserInvites.isEmpty()){
                return 2; // 未被邀请
            }

            // 检查邮箱是否已在User表中存在
            JPAQuery<String> query = new JPAQuery<>(entitymanager);
            QUser u = QUser.user;
            List<String> allEmails = query.select(u.email).from(u).fetch();

            if(allEmails.contains(userinfo.email)){
                return 1; // 邮箱重复
            }

            UserPT userPT = new UserPT();
            userPT.userid = userinfo.userid;
            userPT.magic = 0; // 设置默认值
            userPT.upload = 0; // 设置默认值
            userPT.download = 0; // 设置默认值
            userPT.share = 0.0; // 设置默认值
            userPT.user = userinfo; // 设置关联关系
            userPT.farmurl = ""; // 设置默认值
            userPT.viptime = 0; // 设置默认值

            entitymanager.getTransaction().begin();
            entitymanager.persist(userinfo);
            entitymanager.persist(userPT);
            // 删除所有匹配的邀请记录
            for (UserInvite invite : UserInvites) {
                entitymanager.remove(invite);
            }
            entitymanager.getTransaction().commit();
            return 0; // 注册成功

        }catch(Exception e){
            e.printStackTrace();
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }   
            return 4;
        }
         
    }

    // 返回状态：0 success，1 不存在，2其他原因
    @Override
    public int UpdateInformation(User userinfo){
        EntityManager entitymanager = createEntityManager();
        try {
            if (userinfo.userid == null) {
                return 2; // userid为null直接返回错误
            }

            entitymanager.getTransaction().begin();
            
            JPAQuery<User> query = new JPAQuery<>(entitymanager);
            QUser u = QUser.user;
            User updateUser = query.select(u).from(u).where(u.userid.eq(userinfo.userid)).fetchOne();

            if(updateUser == null){
                entitymanager.getTransaction().rollback();
                return 1;
            }
            // 只更新需要的字段，避免破坏持久化状态和关联关系
            if (userinfo.email != null) updateUser.email = userinfo.email;
            if (userinfo.username != null) updateUser.username = userinfo.username;
            if (userinfo.password != null) updateUser.password = userinfo.password;
            if (userinfo.sex != null) updateUser.sex = userinfo.sex;
            if (userinfo.school != null) updateUser.school = userinfo.school;
            if (userinfo.pictureurl != null) updateUser.pictureurl = userinfo.pictureurl;
            if (userinfo.profile != null) updateUser.profile = userinfo.profile;
            updateUser.accountstate = userinfo.accountstate;
            updateUser.invitetimes = userinfo.invitetimes;
            // 如有其他字段也一并赋值
            entitymanager.merge(updateUser);
            entitymanager.getTransaction().commit();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            return 2;
        }
        
    }

    // 返回用户的全部基本信息
    @Override
    public User GetInformation(String userid){
        EntityManager entitymanager = createEntityManager();
        User user = entitymanager.find(User.class, userid);
        return user;
    }

    //返回用户的全部pt站信息
    @Override
    public UserPT GetInformationPT(String userid){
        EntityManager entitymanager = createEntityManager();
        try {
            UserPT userPT = entitymanager.find(UserPT.class, userid);
            if (userPT == null) {
                return null; // 用户PT信息不存在
            }
            return userPT;
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 出现异常，返回null
        }
    }

    //返回状态：0 success，1 邮箱重复，2其他原因
    @Override
    public int UpdateInformationPT(UserPT userinfo){
        try{
            EntityManager entitymanager = createEntityManager();
            JPAQuery<UserPT> query = new JPAQuery<>(entitymanager);
            QUserPT u = QUserPT.userPT;
            UserPT userPT = query.select(u).from(u).where(u.userid.eq(userinfo.userid)).fetchOne();
        
            if(userPT == null){
                return 1;
            }
            userPT = userinfo;
            entitymanager.merge(userPT);
            return 0;

        }catch(Exception e){
            e.printStackTrace();
            return 2;
        }
    }

    //返回状态：0 success，1 id重复，2其他原因
    @Override
    public int RegisterUserPT(UserPT userinfo){
        EntityManager entitymanager = createEntityManager();
        try {
            entitymanager.getTransaction().begin();
            
            JPAQuery<UserPT> query = new JPAQuery<>(entitymanager);
            QUserPT u = QUserPT.userPT;
            UserPT checkUserPT = query.select(u).from(u).where(u.userid.eq(userinfo.userid)).fetchOne();
            if (checkUserPT != null) {
                entitymanager.getTransaction().rollback();
                return 1;
            }
            
            entitymanager.persist(userinfo);
            entitymanager.getTransaction().commit();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            return 2;
        }
    }

    //返回种子的全部信息
    @Override
    public Seed GetSeedInformation(String seedid){
        EntityManager em = createEntityManager();
        try {
            JPAQuery<Seed> query = new JPAQuery<>(em);
            QSeed s = QSeed.seed;
            QUser u = QUser.user;
            // 使用 fetch join 预先加载关联的 user 对象，避免懒加载导致的 ResultSet closed 错误
            Seed seed = query.select(s).from(s)
                    .leftJoin(s.user, u).fetchJoin()
                    .where(s.seedid.eq(seedid)).fetchOne();
            return seed;
        } finally {
            em.close();
        }
    }

    @Override
    public SeedWithPromotionDTO[] GetSeedListByTag(String tag){
        EntityManager em = createEntityManager();
        try {
            JPAQuery<Seed> query = new JPAQuery<>(em);
            QSeed s = QSeed.seed;
            QUser u = QUser.user;
            QSeedPromotion sp = QSeedPromotion.seedPromotion;
            
            // 使用 fetch join 预先加载关联的 user 对象，避免懒加载导致的 ResultSet closed 错误
            List<Seed> seeds = query.select(s).from(s)
                    .leftJoin(s.user, u).fetchJoin()
                    .where(s.seedtag.eq(tag)).fetch();
            
            // 创建结果列表
            List<SeedWithPromotionDTO> result = new ArrayList<>();
            Date currentDate = new Date();
            
            for (Seed seed : seeds) {
                // 查询当前有效的促销信息
                JPAQuery<SeedPromotion> promotionQuery = new JPAQuery<>(em);
                SeedPromotion promotion = promotionQuery.select(sp).from(sp)
                        .where(sp.seed.seedid.eq(seed.seedid)
                                .and(sp.startTime.loe(currentDate))
                                .and(sp.endTime.goe(currentDate)))
                        .fetchOne();
                
                Integer discount = promotion != null ? promotion.discount : null;
                result.add(new SeedWithPromotionDTO(seed, discount));
            }
            
            return result.toArray(new SeedWithPromotionDTO[0]);
        } finally {
            em.close();
        }
    }

    @Override
    public Seed[] GetSeedListByUser(String userid){
        EntityManager em = createEntityManager();
        try {
            JPAQuery<Seed> query = new JPAQuery<>(em);
            QSeed s = QSeed.seed;
            QUser u = QUser.user;
            // 使用 fetch join 预先加载关联的 user 对象，避免懒加载导致的 ResultSet closed 错误
            List<Seed> seeds = query.select(s).from(s)
                    .leftJoin(s.user, u).fetchJoin()
                    .where(s.seeduserid.eq(userid)).fetch();
            return seeds.toArray(new Seed[0]);
        } finally {
            em.close();
        }
    }

    @Override
    public int DeleteSeed(String seedid){
        EntityManager entitymanager = createEntityManager();
        try {
            entitymanager.getTransaction().begin();
            Seed seed = entitymanager.find(Seed.class, seedid);
            if (seed == null) {
                entitymanager.getTransaction().rollback();
                return 1; // 种子不存在
            }
            entitymanager.remove(seed);
            entitymanager.getTransaction().commit();
            return 0; // 成功删除
        } catch (Exception e) {
            e.printStackTrace();
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            return 2; // 其他错误
        }
    }

    //添加一个新的种子，0成功，其他失败信息待定;
    @Override
    public int RegisterSeed(Seed seedinfo){
        EntityManager entitymanager = createEntityManager();
        try {
            entitymanager.getTransaction().begin();
            JPAQuery<Seed> query = new JPAQuery<>(entitymanager);
            QSeed s = QSeed.seed;
            Seed seed = query.select(s).from(s).where(s.seedid.eq(seedinfo.seedid)).fetchOne();
            User user = entitymanager.find(User.class, seedinfo.seeduserid);
            if (user == null) {
                entitymanager.getTransaction().rollback();
                return 2; // 用户不存在
            }
            seedinfo.user = user; // 设置种子的用户关联
            if (seed != null) {
                entitymanager.getTransaction().rollback();
                return 1;
            }
            entitymanager.persist(seedinfo);
            entitymanager.getTransaction().commit();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            return 2;
        }
    }

    //接收新的种子然后更新其全部属性
    @Override
    public int UpdateSeed(Seed seedinfo){
        try {
            EntityManager entitymanager = createEntityManager();
            JPAQuery<Seed> query = new JPAQuery<>(entitymanager);
            QSeed s = QSeed.seed;
            Seed seed = query.select(s).from(s).where(s.seedid.eq(seedinfo.seedid)).fetchOne();
            if (seed == null) {
                return 1;
            }
            seed = seedinfo;
            entitymanager.merge(seed);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 2;
        }
    }

    //传入搜索的关键词或句子，返回搜索到的种子信息（按照公共字符数量排序）
    @Override
    public Seed[] SearchSeed(String userQ){
        EntityManager em = createEntityManager();
        try {
            JPAQuery<Seed> query = new JPAQuery<>(em);
            QSeed s = QSeed.seed;
            QUser u = QUser.user;
            // 使用 fetch join 预先加载关联的 user 对象，避免懒加载导致的 ResultSet closed 错误
            List<Seed> seeds = query.select(s).from(s)
                    .leftJoin(s.user, u).fetchJoin()
                    .fetch();

            if (seeds == null || userQ == null || userQ.trim().isEmpty()) {
                return seeds.toArray(new Seed[0]);
            }

            String processedQuery = userQ.toLowerCase().trim();
            Map<Seed, Integer> seedCountMap = new HashMap<>();
            for(Seed seed : seeds){
                String title = seed.title.toLowerCase().trim();
                int count = countCommonCharacter(processedQuery, title);
                seedCountMap.put(seed, count);
            }
            seeds.sort((s1, s2) -> {
                int count1 = seedCountMap.getOrDefault(s1, 0);
                int count2 = seedCountMap.getOrDefault(s2, 0);
                return Integer.compare(count2, count1);
            });

            return seeds.toArray(new Seed[0]);
        } finally {
            em.close();
        }
    }

    //计算字符串公共字符数量
    private int countCommonCharacter(String str1, String str2){
        Map<Character, Integer> map1 = new HashMap<>();
        Map<Character, Integer> map2 = new HashMap<>();

        for(char c : str1.toCharArray()){
            if (!Character.isWhitespace(c)) {
                map1.put(c, map1.getOrDefault(c, 0) + 1);
            }
        }

        for(char c : str2.toCharArray()){
            if (!Character.isWhitespace(c)) {
                map2.put(c, map2.getOrDefault(c, 0) + 1);
            }
        }

        int res = 0;
        for(char c : map1.keySet()){
            if (map2.containsKey(c)) {
                res += Math.min(map1.get(c), map2.get(c));
            }
            
        }
        return res;
    }

    //返回状态：0 success，1 重复，2其他原因
    @Override
    public int AddNotice(Notice notice){
        try {
            EntityManager entitymanager = createEntityManager();
            JPAQuery<Notice> query = new JPAQuery<>(entitymanager);
            QNotice n = QNotice.notice;
            Notice checkNotice = query.select(n).from(n).where(n.noticeid.eq(notice.noticeid)).fetchOne();
            if (checkNotice != null) {
                return 1;
            }
            
            entitymanager.persist(notice);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 2;
        }
        
    }

    //返回状态：0 success，1 不存在，2其他原因
    @Override
    public boolean UpdateNotice(Notice notice){
        try {
            EntityManager entitymanager = createEntityManager();
            Notice oldNotice = entitymanager.find(Notice.class, notice.noticeid);
            if (oldNotice == null) {
                return false;
            }
            oldNotice = notice;
            entitymanager.merge(oldNotice);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //删除公告，返回状态：0 success，1 不存在，2其他原因
    @Override
    public boolean DeleteNotice(String noticeid){
        try {
            EntityManager entitymanager = createEntityManager();
            Notice notice = entitymanager.find(Notice.class, noticeid);
            if (notice == null) {
                return false;
            }
            entitymanager.remove(notice);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
    }

    //获取用户的剩余邀请次数
    public int GetUserAvailableInviteTimes(String userid){
        try {
            EntityManager entitymanager = createEntityManager();
            JPAQuery<Integer> query = new JPAQuery<>(entitymanager);
            QUser u = QUser.user;
            int invite_left = query.select(u.invitetimes).from(u).where(u.userid.eq(userid)).fetchOne();
            
            return invite_left;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        
    }

    //邀请用户，返回状态：0 success，1 剩余次数不足，2,3其他原因
    @Override
    public int InviteUser(String inviterid,String inviteemail){
        EntityManager entitymanager = createEntityManager();
        try {
            User user = entitymanager.find(User.class, inviterid);
            if (user == null || !user.email.equals(inviteemail)) {
                return 3;
            }
            if (user.invitetimes <= 0) {
                return 1;
            }
            user.invitetimes -= 1;
            entitymanager.getTransaction().begin();
            entitymanager.merge(user);
            entitymanager.getTransaction().commit();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            return 2;
        }
    }

    //添加一个收藏，返回状态：0 success，1 不存在,2其他原因
    @Override
    public boolean AddCollect(String userid,String seedid){
        EntityManager entitymanager = createEntityManager();
        try {
            JPAQuery<User> query2 = new JPAQuery<>(entitymanager);
            QUser u2 = QUser.user;
            User user = query2.select(u2).from(u2).where(u2.userid.eq(userid)).fetchOne();
            if (user == null) {
                return false;
            }
            JPAQuery<Seed> query3 = new JPAQuery<>(entitymanager);
            QSeed p = QSeed.seed;
            Seed seed = query3.select(p).from(p).where(p.seedid.eq(seedid)).fetchOne();
            if (seed == null) {
                return false;
            }
            JPAQuery<String> query = new JPAQuery<>(entitymanager);
            QUserStar u = QUserStar.userStar;
            List<String> allSeedId = query.select(u.seedid).from(u).where(u.userid.eq(userid)).fetch();

            if (allSeedId.contains(seedid)) {
                return false;
            }
            UserStar userStar = new UserStar();
            userStar.userid = userid;
            userStar.seedid = seedid;
            userStar.seed = seed; // 设置关联的种子对象
            userStar.user = user; // 设置关联的用户对象
            userStar.createdAt = new Date(); // 设置创建时间
            entitymanager.getTransaction().begin();
            entitymanager.persist(userStar);
            entitymanager.getTransaction().commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            return false;
        } finally {
            if (entitymanager != null) {
                entitymanager.close();
            }
        }
    }

    //删除一个收藏，返回状态：0 success，1 不存在,2其他原因
    @Override
    public boolean DeleteCollect(String userid, String seedid){
        EntityManager entitymanager = createEntityManager();
        try {
            JPAQuery<UserStar> query = new JPAQuery<>(entitymanager);
            QUserStar u = QUserStar.userStar;
            UserStar userStar = query.select(u).from(u).where(u.userid.eq(userid).and(u.seedid.eq(seedid))).fetchOne();
            if (userStar == null) {
                return true; // 收藏不存在
            }
            entitymanager.getTransaction().begin();
            entitymanager.remove(userStar);
            entitymanager.getTransaction().commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            return false;
        } finally {
            if (entitymanager != null) {
                entitymanager.close();
            }
        }
        
    }

    @Override
    public int AddBegSeed(BegInfo info) {
        if (info == null || info.begid == null || info.begid.isEmpty()) {
            logger.warn("Invalid parameter: info is null or begid is empty");
            return 2;
        }

        EntityTransaction tx = null;

        try {
            EntityManager entitymanager = createEntityManager();
            tx = entitymanager.getTransaction();
            tx.begin();

            // 检查是否重复
            BegInfo existingBeg = entitymanager.find(BegInfo.class, info.begid);
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
            entitymanager.persist(info);
            tx.commit();

            logger.info("Successfully added new BegSeed with ID: {}", info.begid);
            return 0;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error adding BegSeed: {}", e.getMessage());
            return 2;
        }
    }

    @Override
    public int UpdateBegSeed(BegInfo info) {
        if (info == null || info.begid == null || info.begid.isEmpty()) {
            logger.warn("Invalid parameter: info is null or begid is empty");
            return 2;
        }

        EntityTransaction tx = null;

        try {
            EntityManager entitymanager = createEntityManager();
            tx = entitymanager.getTransaction();
            tx.begin();

            // 检查是否存在
            BegInfo existingBeg = entitymanager.find(BegInfo.class, info.begid);
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
            entitymanager.merge(info);
            tx.commit();

            logger.info("Successfully updated BegSeed with ID: {}", info.begid);
            return 0;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error updating BegSeed: {}", e.getMessage());
            return 2;
        }
    }

    @Override
    public int DeleteBegSeed(String begid) {
        if (begid == null || begid.isEmpty()) {
            logger.warn("Invalid parameter: begid is null or empty");
            return 2;
        }

        EntityTransaction tx = null;

        try {
            EntityManager entitymanager = createEntityManager();
            tx = entitymanager.getTransaction();
            tx.begin();

            // 查找要删除的求种信息
            BegInfo begInfo = entitymanager.find(BegInfo.class, begid);
            if (begInfo == null) {
                logger.warn("BegSeed with ID {} does not exist", begid);
                tx.rollback();
                return 1;
            }

            // 删除求种信息
            entitymanager.remove(begInfo);
            tx.commit();

            logger.info("Successfully deleted BegSeed with ID: {}", begid);
            return 0;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error deleting BegSeed: {}", e.getMessage());
            return 2;
        }
    }

    @Override
    public int VoteSeed(String begId, String seedId, String userId) {
        if (begId == null || seedId == null || userId == null ||
                begId.isEmpty() || seedId.isEmpty() || userId.isEmpty()) {
            logger.warn("Invalid parameters: begId, seedId or userId is null or empty");
            return 2;
        }

        EntityTransaction tx = null;

        try {
            EntityManager entitymanager = createEntityManager();
            tx = entitymanager.getTransaction();
            tx.begin();

            // 检查求种信息是否存在
            BegInfo begInfo = entitymanager.find(BegInfo.class, begId);
            if (begInfo == null) {
                logger.warn("BegSeed with ID {} does not exist", begId);
                return 2;
            }

            // 检查用户是否已投票
            Long voteCount = new JPAQuery<>(entitymanager)
                    .select(QUserVotes.userVotes.count())
                    .from(QUserVotes.userVotes)
                    .where(QUserVotes.userVotes.id.eq(new entity.UserVotesId(userId, begId, seedId)))
                    .fetchOne();

            if (voteCount != null && voteCount > 0) {
                logger.warn("User {} has already voted for seed {} in beg {}", userId, seedId, begId);
                return 1;
            }

            // 创建新的投票记录
            entitymanager.createNativeQuery("INSERT INTO UserVotes (user_id, beg_id, seed_id, created_at) " +
                    "VALUES (?, ?, ?, CURRENT_TIMESTAMP)")
                    .setParameter(1, userId)
                    .setParameter(2, begId)
                    .setParameter(3, seedId)
                    .executeUpdate();

            // 更新SubmitSeed表中的投票数
            entitymanager.createQuery("UPDATE SubmitSeed s SET s.votes = s.votes + 1 " +
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
        }
    }

    @Override
    public int SubmitSeed(String begid, Seed seed) {
        if (begid == null || seed == null || begid.isEmpty() || seed.seedid == null) {
            logger.warn("Invalid parameters: begid or seed is null or empty");
            return 2;
        }

        EntityTransaction tx = null;

        try {
            EntityManager entitymanager = createEntityManager();
            tx = entitymanager.getTransaction();
            tx.begin();

            // 检查求种信息是否存在
            BegInfo begInfo = entitymanager.find(BegInfo.class, begid);
            if (begInfo == null) {
                logger.warn("BegSeed with ID {} does not exist", begid);
                return 2;
            }

            // 检查种子是否已提交过
            QSubmitSeed ss = QSubmitSeed.submitSeed;
            Long submitCount = new JPAQuery<>(entitymanager)
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
            if (entitymanager.find(Seed.class, seed.seedid) == null) {
                entitymanager.persist(seed);
            }

            // 创建提交记录
            entitymanager.createNativeQuery("INSERT INTO SubmitSeed (beg_id, seed_id, votes) VALUES (?, ?, 0)")
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
        }
    }

    @Override
    public void SettleBeg() {
        EntityTransaction tx = null;

        try {
            EntityManager entitymanager = createEntityManager();
            tx = entitymanager.getTransaction();
            tx.begin();

            // 1. 获取所有已过期且未完成的求种信息
            QBegInfo b = QBegInfo.begInfo;
            List<BegInfo> expiredBegs = new JPAQuery<>(entitymanager)
                    .select(b)
                    .from(b)
                    .where(b.endtime.loe(new Date())
                            .and(b.hasseed.eq(0)))
                    .fetch();

            for (BegInfo beg : expiredBegs) {
                // 2. 查找投票最多的提交任务
                QSubmitSeed ss = QSubmitSeed.submitSeed;
                Tuple topSubmission = new JPAQuery<>(entitymanager)
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
                    String ownerId = new JPAQuery<>(entitymanager)
                            .select(s.seeduserid)
                            .from(s)
                            .where(s.seedid.eq(seedId))
                            .fetchOne();

                    // 4. 获取上传者的PT信息并更新魔力值
                    UserPT ownerPT = entitymanager.find(UserPT.class, ownerId);
                    if (ownerPT != null) {
                        // 5. 发放奖励
                        ownerPT.magic += beg.magic;
                        entitymanager.merge(ownerPT);

                        // 6. 更新求种状态
                        beg.hasseed = 1;
                        entitymanager.merge(beg);

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
        }
    }

    @Override
    public int AddPost(Post post) {
        if (post == null || post.postid == null || post.postid.isEmpty()) {
            logger.warn("Invalid parameter: post is null or postid is empty");
            return 2;
        }

        EntityTransaction tx = null;

        try {
            EntityManager entitymanager = createEntityManager();
            tx = entitymanager.getTransaction();
            tx.begin();

            // 检查是否重复
            Post existingPost = entitymanager.find(Post.class, post.postid);
            if (existingPost != null) {
                logger.warn("Post with ID {} already exists", post.postid);
                return 1;
            }

            // 检查用户是否存在
            User user = entitymanager.find(User.class, post.postuserid);
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
            entitymanager.persist(post);
            tx.commit();

            logger.info("Successfully added new post with ID: {}", post.postid);
            return 0;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error adding post: {}", e.getMessage());
            return 2;
        }
    }

    @Override
    public int UpdatePost(Post post) {
        if (post == null || post.postid == null || post.postid.isEmpty()) {
            logger.warn("Invalid parameter: post is null or postid is empty");
            return 2;
        }

        EntityTransaction tx = null;

        try {
            EntityManager entitymanager = createEntityManager();
            tx = entitymanager.getTransaction();
            tx.begin();

            // 检查帖子是否存在
            Post existingPost = entitymanager.find(Post.class, post.postid);
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
            entitymanager.merge(post);
            tx.commit();

            logger.info("Successfully updated post with ID: {}", post.postid);
            return 0;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            logger.error("Error updating post: {}", e.getMessage());
            return 2;
        }
    }

    @Override
    public int DeletePost(String postid){
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
    public int AddComment(String postid, String userid, String comment){
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

            // 创建新的评论
            PostReply reply = new PostReply();
            reply.replyid = UUID.randomUUID().toString();
            reply.postid = postid;
            reply.authorid = userid;
            reply.content = comment;
            reply.createdAt = new Date();

            // 保存评论
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
    public int DeleteComment(String postid,String commentid){
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
    public boolean ExchangeMagicToUpload(String userid,int magic)//将魔力值兑换为上传量，返回状态：0 success，1 不存在,2其他原因
    {
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
            userPT.upload += magic * 10000000;

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
            e.printStackTrace();
            logger.error("魔力值兑换上传量时发生错误: {}", e.getMessage());
            return false;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    @Override
    public boolean ExchangeMagicToDownload(String userid,int magic)
    {
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
            userPT.download = Math.max(0, userPT.download - magic * 10000000);

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
    }//将魔力值兑换为下载量，返回状态：0 success，1 不存在,2其他原因

    @Override
    public boolean ExchangeMagicToVip(String userid,int magic){
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
            userPT.viptime += magic / 100;

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
    //将魔力值兑换为VIP次数，返回状态：0 success，1 不存在,2其他原因

    @Override
    public boolean UploadTransmitProfile(Profile profile){
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
    public Profile GetTransmitProfile(String profileid){
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
    //获取迁移信息
    
    @Override
    public boolean ExamTransmitProfile(String profileid, boolean result, Integer grantedUpload){
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
                    // long uploadToGive = Long.parseLong(profile.uploadtogive);
                    userPT.upload += grantedUpload;
                    profile.uploadgived = String.valueOf(grantedUpload);

                    em.merge(userPT);
                    em.merge(profile);
                }
            } else {
                // 如果审核不通过，删除迁移申请
                em.remove(profile);
                logger.info("迁移申请 {} 审核不通过，已删除", profileid);
            }
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
    //审核迁移信息,0成功，1失败
    @Override
    public Profile[] GetTransmitProfileList(){
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
    //获取所有迁移信息

    @Override
    public Post[] GetPostList() {
        EntityManager entitymanager = createEntityManager();
        try{
            JPAQuery<Post> query = new JPAQuery<>(entitymanager);
            QPost p = QPost.post;
            List<Post> posts = query.select(p).from(p).fetch();
            return posts.toArray(new Post[0]);
        } catch (Exception e) {
            logger.error("获取帖子列表时发生错误: {}", e.getMessage());
            return new Post[0];
        } finally {
            entitymanager.close();
            logger.info("Post list retrieved successfully.");
        }
    }

    @Override
    public Post GetPost(String postid) {
        EntityManager entitymanager = createEntityManager();
        try {
            JPAQuery<Post> query = new JPAQuery<>(entitymanager);
            QPost p = QPost.post;
            Post post = query.select(p).from(p).where(p.postid.eq(postid)).fetchOne();
            return post;
        } catch (Exception e) {
            logger.error("获取帖子列表时发生错误: {}", e.getMessage());
            return new Post();
        } finally {
            entitymanager.close();
            logger.info("Post list retrieved successfully.");
        }
    }

    @Override
    public PostReply[] GetPostReplyList(String postid) {
        EntityManager entitymanager = createEntityManager();
        JPAQuery<PostReply> query = new JPAQuery<>(entitymanager);
        QPostReply p = QPostReply.postReply;
        List<PostReply> replies = query.select(p).from(p).where(p.postid.eq(postid)).fetch();
        return replies.toArray(new PostReply[0]);
    }

    @Override
    public Post[] SearchPost(String postQ) {
        EntityManager entitymanager = createEntityManager();
        try {
            JPAQuery<Post> query = new JPAQuery<>(entitymanager);
            QPost p = QPost.post;
            List<Post> posts = query.select(p).from(p).fetch();

            if (posts == null || postQ == null || postQ.trim().isEmpty()) {
                return posts.toArray(new Post[0]);
            }

            String processedQuery = postQ.toLowerCase().trim();
            Map<Post, Integer> postCountMap = new HashMap<>();
            for(Post post : posts){
                String title = post.posttitle.toLowerCase().trim();
                int count = countCommonCharacter(processedQuery, title);
                postCountMap.put(post, count);
            }
            posts.sort((s1, s2) -> {
                int count1 = postCountMap.getOrDefault(s1, 0);
                int count2 = postCountMap.getOrDefault(s2, 0);
                return Integer.compare(count2, count1);
            });

            return posts.toArray(new Post[0]);
        } catch (Exception e) {
            logger.error("搜索帖子时发生错误: {}", e.getMessage());
            return new Post[0];
        } finally {
            entitymanager.close();
            logger.info("Post search completed successfully.");
        }
    }

    @Override
    public int CheckAdmin(String userid) {
        if (userid == null || userid.isEmpty()) {
            logger.warn("参数无效: userid为空");
            return 2; // 参数无效
        }

        EntityManager entitymanager = createEntityManager();
        JPAQuery<String> query = new JPAQuery<>(entitymanager);
        QAdmin a = QAdmin.admin;
        String adminId = query.select(a.userId).from(a).where(a.userId.eq(userid)).fetchOne();
        if (adminId == null) {
            logger.warn("用户 {} 不是管理员", userid);
            return 1; // 用户不是管理员
        } else {
            logger.info("用户 {} 是管理员", userid);
            return 0; // 用户是管理员
        }
    }

    @Override
    public int InviteNewUser(String inviterid, String invitedemail) {
        EntityManager entitymanager = createEntityManager();
        try {
            User user = entitymanager.find(User.class, inviterid);
            if (user == null) {
                return 3;
            }
            if (user.invitetimes <= 0) {
                return 1;
            }
            user.invitetimes -= 1;

            UserInvite invite = new UserInvite();
            invite.userId = inviterid;
            invite.inviterEmail = invitedemail;
            invite.inviterRegistered = false;
            invite.user = user;

            entitymanager.getTransaction().begin();
            entitymanager.merge(user);
            entitymanager.persist(invite);
            entitymanager.getTransaction().commit();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            return 2;
        }
    }

    @Override
    public UserStar[] GetUserStarList(String userid) {
        if (userid == null || userid.isEmpty()) {
            logger.warn("参数无效: userid为空");
            return new UserStar[0];
        }

        EntityManager entitymanager = createEntityManager();
        try {
            JPAQuery<UserStar> query = new JPAQuery<>(entitymanager);
            QUserStar us = QUserStar.userStar;
            List<UserStar> stars = query.select(us).from(us).where(us.userid.eq(userid)).fetch();
            return stars.toArray(new UserStar[0]);
        } catch (Exception e) {
            e.printStackTrace();
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            return new UserStar[0];
        } finally {
            if (entitymanager != null) {
                entitymanager.close();
            }
        }
    }

    @Override
    public int UploadMigration(String userid, File file, String uploadtogive) {
        if (userid == null || userid.isEmpty() || file == null) {
            return 2; // 参数无效
        }

        EntityManager entitymanager = createEntityManager();

        try {
            // 检查用户是否存在
            User user = entitymanager.find(User.class, userid);
            if (user == null) {
                return 1; // 用户不存在
            }

            // 创建迁移记录
            Profile migration = new Profile();
            migration.profileurl = java.util.UUID.randomUUID().toString();
            migration.userid = userid;
            migration.uploadtogive = uploadtogive; // 初始上传量为0
            migration.magictogive = "0"; // 初始魔力值为0
            migration.downloadgived = "0"; // 初始下载量为0
            migration.uploadgived = "0"; // 初始上传量为0
            migration.magicgived = "0"; // 初始魔力值为0
            migration.downloadgived = "0"; // 初始下载量为0
            migration.exampass = false; // 初始审核状态为未通过
            migration.user = user;
            Path storageDir = Paths.get(config.MIGRATION_STORAGE_DIR);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
            String filename = file.getName();
            Path target = storageDir.resolve(migration.profileurl + "_" + filename);
            Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            migration.applicationurl = target.toString(); // 设置迁移文件的存储路径

            entitymanager.getTransaction().begin();
            entitymanager.persist(migration);
            entitymanager.getTransaction().commit();

            return 0; // 成功

        } catch (Exception e) {
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            e.printStackTrace();
            return 2; // 其他错误
        } finally {
            if (entitymanager != null) {
                entitymanager.close();
            }
        }
    }

    @Override
    public BegSeedDetail[] GetBegList() {
        EntityManager entitymanager = createEntityManager();
        try {
            JPAQuery<BegInfo> query = new JPAQuery<>(entitymanager);
            QBegInfo b = QBegInfo.begInfo;
            List<BegInfo> begList = query.select(b).from(b).fetch();
            
            List<BegSeedDetail> begSeedDetailList = new ArrayList<>();
            for (BegInfo begInfo : begList) {
                // 查询对应的BegInfo表的Info字段
                BegInfoDetail begInfoDetail = entitymanager.find(BegInfoDetail.class, begInfo.begid);
                // 构造BegSeedDetail对象
                BegSeedDetail begSeedDetail = new BegSeedDetail(begInfo, begInfoDetail);
                begSeedDetailList.add(begSeedDetail);
            }
            
            return begSeedDetailList.toArray(new BegSeedDetail[0]);
        } catch (Exception e) {
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            e.printStackTrace();
            return new BegSeedDetail[0];
        } finally {
            if(entitymanager != null){
                entitymanager.close();
            }
        }
    }

    @Override
    public BegInfo GetBegDetail(String begid) {
        EntityManager entitymanager = createEntityManager();
        try {
            JPAQuery<BegInfo> query = new JPAQuery<>(entitymanager);
            QBegInfo b = QBegInfo.begInfo;
            BegInfo begDetail = query.select(b).from(b).where(b.begid.eq(begid)).fetchOne();
            return begDetail;
        } catch (Exception e) {
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;
        } finally {
            if(entitymanager != null){
                entitymanager.close();
            }
        }
    }

    @Override
    public BegSeedDetail GetBegSeedDetail(String begid) {
        if (begid == null || begid.isEmpty()) {
            return null;
        }

        EntityManager entitymanager = createEntityManager();
        try {
            // 查询 BegSeed 表数据
            JPAQuery<BegInfo> begQuery = new JPAQuery<>(entitymanager);
            QBegInfo b = QBegInfo.begInfo;
            BegInfo begInfo = begQuery.select(b).from(b).where(b.begid.eq(begid)).fetchOne();
            
            if (begInfo == null) {
                return null;
            }

            // 查询 BegInfo 表的 Info 字段
            BegInfoDetail begInfoDetail = entitymanager.find(BegInfoDetail.class, begid);
            
            // 构造返回对象
            return new BegSeedDetail(begInfo, begInfoDetail);
        } catch (Exception e) {
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;
        } finally {
            if(entitymanager != null){
                entitymanager.close();
            }
        }
    }

    @Override
    public SeedWithVotes[] GetBegSeedListWithVotes(String begid) {
        if (begid == null || begid.isEmpty()) {
            return new SeedWithVotes[0];
        }

        EntityManager entitymanager = createEntityManager();
        try {
            JPAQuery<Tuple> query = new JPAQuery<>(entitymanager);
            QSubmitSeed ss = QSubmitSeed.submitSeed;
            QSeed s = QSeed.seed;

            List<Tuple> results = query.select(s, ss.votes)
                    .from(ss)
                    .join(ss.seed, s)
                    .where(ss.begInfo.begid.eq(begid))
                    .fetch();

            List<SeedWithVotes> seedsWithVotes = new java.util.ArrayList<>();
            for (Tuple result : results) {
                Seed seed = result.get(s);
                Integer votes = result.get(ss.votes);
                seedsWithVotes.add(new SeedWithVotes(seed, votes != null ? votes : 0));
            }

            return seedsWithVotes.toArray(new SeedWithVotes[0]);
        } catch (Exception e) {
            return new SeedWithVotes[0];
        } finally {
            entitymanager.close();
        }
    }

    @Override
    public int SubmitBegSeed(String begid, String seedid, String userid) {
        if (begid == null || begid.isEmpty() || seedid == null || seedid.isEmpty() || userid == null || userid.isEmpty()) {
            return 2; // 参数无效
        }

        EntityManager entitymanager = createEntityManager();
        EntityTransaction tx = entitymanager.getTransaction();

        try {
            tx.begin();
            
            // 检查种子信息是否存在
            Seed seed = entitymanager.find(Seed.class, seedid);
            if (seed == null || !seed.seeduserid.equals(userid)) {
                return 1;
            }

            // int ret = SubmitSeed(begid, seed);
            BegInfo begInfo = entitymanager.find(BegInfo.class, begid);
            if (begInfo == null) {
                return 2;
            }

            // 检查种子是否已提交过
            QSubmitSeed ss = QSubmitSeed.submitSeed;
            SubmitSeed submitSeed = new JPAQuery<>(entitymanager)
                    .select(ss)
                    .from(ss)
                    .where(ss.begInfo.begid.eq(begid))
                    .where(ss.seed.seedid.eq(seed.seedid))
                    .fetchOne();

            submitSeed = new SubmitSeed();
            // 设置复合主键
            submitSeed.id = new SubmitSeedId(begid, seed.seedid);

            submitSeed.begInfo = begInfo;
            submitSeed.seed = seed;
            submitSeed.votes = 0; // 初始投票数为0
            BegInfoDetail begInfoDetail = entitymanager.find(BegInfoDetail.class, begid);
            if (begInfoDetail == null) {
                return 1;
            }

            entitymanager.persist(submitSeed);
            tx.commit();

            return 0;

        } catch (Exception e) {
            e.printStackTrace();
            if (tx.isActive()) {
                tx.rollback();
            }
            return 3; // 其他错误
        } finally {
            if (entitymanager != null) {
                entitymanager.close();
            }
        }
    }

    @Override
    public int createBagSeed(BegInfo begInfo, String userid, String info) {
        if (begInfo == null || begInfo.begid == null || begInfo.begid.isEmpty()) {
            return 2; // 参数无效
        }

        EntityManager entitymanager = createEntityManager();
        try {
            BegInfoDetail begInfoDetail = entitymanager.find(BegInfoDetail.class, begInfo.begid);
            if (begInfoDetail != null) {
                return 1; // 已存在
            }

            User user = entitymanager.find(User.class, userid);
            if (user == null) {
                return 2; // 用户不存在
            }

            UserPT userPT = entitymanager.find(UserPT.class, userid);
            if (userPT == null) {
                return 2; // 用户PT信息不存在
            }

            if (userPT.magic < begInfo.magic) {
                return 3; // 魔力值不足
            }

            begInfoDetail = new BegInfoDetail();
            begInfoDetail.begId = begInfo.begid;
            begInfoDetail.info = info;
            begInfoDetail.userId = userid;
            begInfoDetail.user = user;
            userPT.magic -= begInfo.magic; // 扣除魔力值

            entitymanager.getTransaction().begin();
            entitymanager.persist(userPT);
            entitymanager.persist(begInfo);
            entitymanager.persist(begInfoDetail);
            entitymanager.getTransaction().commit();

            return 0;
        } catch (Exception e) {
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            e.printStackTrace();
            return 2; // 其他错误
        } finally {
            if (entitymanager != null) {
                entitymanager.close();
            }
        }
    }

    @Override
    public Seed[] getAllSeeds() {
        EntityManager entitymanager = createEntityManager();
        try {
            JPAQuery<Seed> query = new JPAQuery<>(entitymanager);
            QSeed s = QSeed.seed;
            List<Seed> seeds = query.select(s).from(s).fetch();
            return seeds.toArray(new Seed[0]);
        } catch (Exception e) {
            e.printStackTrace();
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            return new Seed[0];
        } finally {
            if (entitymanager != null) {
                entitymanager.close();
            }
        }
    }

    @Override
    public SeedPromotion[] getAllSeedPromotions() {
        EntityManager entitymanager = createEntityManager();
        try {
            JPAQuery<SeedPromotion> query = new JPAQuery<>(entitymanager);
            QSeedPromotion sp = QSeedPromotion.seedPromotion;
            List<SeedPromotion> promotions = query.select(sp).from(sp).fetch();
            return promotions.toArray(new SeedPromotion[0]);
        } catch (Exception e) {
            e.printStackTrace();
            if (entitymanager.getTransaction().isActive()) {
                entitymanager.getTransaction().rollback();
            }
            return new SeedPromotion[0];
        } finally {
            if (entitymanager != null) {
                entitymanager.close();
            }
        }
    }

    @Override
    public int createSeedPromotion(String seedid, Date startTime, Date endTime, Integer discount) {
        if (seedid == null || seedid.isEmpty() || startTime == null || endTime == null || discount == null) {
            return 2; // 参数无效
        }

        EntityManager entitymanager = createEntityManager();
        EntityTransaction tx = entitymanager.getTransaction();

        try {
            tx.begin();

            // 检查种子是否存在
            Seed seed = entitymanager.find(Seed.class, seedid);
            if (seed == null) {
                return 1; // 种子不存在
            }

            // 检查是否已存在相同的促销活动
            QSeedPromotion sp = QSeedPromotion.seedPromotion;
            SeedPromotion existingPromotion = new JPAQuery<>(entitymanager)
                    .select(sp)
                    .from(sp)
                    .where(sp.seed.seedid.eq(seedid))
                    .fetchOne();

            // 创建新的促销活动
            SeedPromotion promotion = new SeedPromotion();
            promotion.promotionId = java.util.UUID.randomUUID().toString();
            promotion.seed = seed;
            promotion.startTime = startTime;
            promotion.endTime = endTime;
            promotion.discount = discount;

            if (existingPromotion != null) {
                promotion.promotionId = existingPromotion.promotionId; // 如果已存在，则使用现有的ID
                entitymanager.merge(promotion); // 更新现有的促销活动
            } else {
                entitymanager.persist(promotion); // 创建新的促销活动
            }

            tx.commit();
            return 0; // 成功

        } catch (Exception e) {
            e.printStackTrace();
            if (tx.isActive()) {
                tx.rollback();
            }
            return 4; // 其他错误
        } finally {
            if (entitymanager != null) {
                entitymanager.close();
            }
        }
    }
}