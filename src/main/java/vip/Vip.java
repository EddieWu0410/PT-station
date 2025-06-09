package vip;
import java.io.File;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.tuple.Pair;

import com.querydsl.jpa.impl.JPAQuery;

import entity.QSeed;
import entity.QUser;
import entity.QVipSeed;
import entity.Seed;
import entity.User;
import entity.VipSeed;

public class Vip implements VipInterface {

    int seedKeepNumber = 10;

    @PersistenceContext
    private EntityManager entitymanager;
    
    //获取专线下载的文件，并记录用户的下载行为
    //如果用户权限足够，文件存在，integer为0，如果用户权限足够，文件不存在，
    //integer为1，file为null 如果用户权限不足，file为null，integer为2；
    @Override
    public Pair<File,Integer>  GetTTorent(String seedid,String userid,String ip){
        EntityManager em = this.entitymanager;
        File file = null;
        try {
            if (seedid == null || userid == null || ip == null) {
                return Pair.of(null, 2);
            }
            Seed seed = em.find(Seed.class, seedid);
            if (seed == null || seed.url == null) {
                return Pair.of(null, 1);
            }
            User user = em.find(User.class, userid);
            if (user == null) {
                return Pair.of(null, 2);
            }
            file = new File(seed.url);
            JPAQuery<Boolean> query = new JPAQuery<>(em);
            QUser u = QUser.user;
            Boolean status = query.select(u.accountstate).where(u.userid.eq(userid)).fetchOne();
            if (!file.exists()) {
                if (status){
                    return Pair.of(null, 2);
                } else {
                    return Pair.of(null, 1);
                }
            }
            if (status){
                return Pair.of(null, 2);
            }
            
            // 记录下载行为
            javax.persistence.EntityTransaction tx = em.getTransaction();
            tx.begin();
            entity.SeedDownload sd = new entity.SeedDownload();
            sd.seedId = seedid;
            sd.userId = userid;
            sd.clientIp = ip;
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            sd.downloadStart = now;
            sd.downloadEnd = now;
            em.persist(sd);
            tx.commit();
        } catch (Exception e) {
            // ignore persistence errors and still return the file
        }
        return Pair.of(file, 0);
    }

    //种子增加了新的保种人数,返回值：0，写入成功，1写入失败,其他待定
    @Override
    public int AddFarmerNumber(int number,String seedid){
        try {
            VipSeed vipseed = entitymanager.find(VipSeed.class, seedid);
            if(vipseed == null){
                return 1;
            }
            vipseed.farmernumber = vipseed.farmernumber + number;
            entitymanager.merge(vipseed);

            return 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

    }

    //种子降低的保种人数，返回值:0,写入成功，1:写入失败,其他待定
    @Override
    public int ReduceFarmerNumber(int number,String seedid){
        try {
            VipSeed vipSeed = entitymanager.find(VipSeed.class, seedid);
            if(vipSeed == null){
                return 1;
            }
            vipSeed.farmernumber = vipSeed.farmernumber - number;
            entitymanager.merge(vipSeed);

            return 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

    }

    //将种子加入保种列表
    // @Override
    public int KeepSeed(String seedid){
        try {
            Seed seed = entitymanager.find(Seed.class, seedid);
            if(seed == null){
                return 1; //种子不存在
            }
            VipSeed vipSeed = new VipSeed();
            vipSeed.seedid = seedid;
            vipSeed.seed = seed;
            vipSeed.seedercount = 1; // 初始保种人数为1
            vipSeed.rewardmagic = 50; // 初始奖励魔法值为50
            vipSeed.stopcaching = 0; // 初始停止缓存状态为0
            vipSeed.bonus = 50; // 初始奖励为50
            vipSeed.cachestate = true; // 初始缓存状态为true
            vipSeed.farmernumber = 0; // 初始农民人数为0
            
            entitymanager.persist(vipSeed);
            return 0; // 成功添加到保种列表
            
        } catch (Exception e) {
            e.printStackTrace();
            return 2; // 添加失败
        }

    }

    //将种子移除保种列表
    @Override
    public int RemoveSeed(String seedid){
        try {
            VipSeed vipSeed = entitymanager.find(VipSeed.class, seedid);
            if(vipSeed == null){
                return 0;
            }
            entitymanager.remove(vipSeed);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

    }

    //由外部触发，调用类内函数更新保种列表
    @Override
    public void CheckSeed(){
        JPAQuery<VipSeed> query = new JPAQuery<>(entitymanager);
        QVipSeed v = QVipSeed.vipSeed;
        // 保证 select(v) 返回 List<VipSeed>
        List<VipSeed> allVipSeeds = query.select(v).from(v).fetch();
        for (VipSeed vipSeed : allVipSeeds) {
            if (vipSeed.farmernumber > seedKeepNumber) {
                entitymanager.remove(vipSeed);
            }
        }
    }

    //获取当前需要保种的所有种子信息
    @Override
    public Seed[] GetSeedToPlant(){
        JPAQuery<String> query = new JPAQuery<>(entitymanager);
        QVipSeed v = QVipSeed.vipSeed;
        List<String> allSeedId = query.select(v.seedid).from(v).fetch();
        
        if(allSeedId.isEmpty()){
            return new Seed[0];
        }
        JPAQuery<Seed> query2 = new JPAQuery<>(entitymanager);
        QSeed s = QSeed.seed;
        List<Seed> allSeeds = query2.select(s).from(s).where(s.seedid.in(allSeedId)).fetch();
        
        return allSeeds.toArray(new Seed[0]);
    }

}

