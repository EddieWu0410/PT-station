package entity;

import java.util.Date;

/**
 * 包含 BegSeed 表数据和对应 BegInfo 表 Info 字段的复合类
 * 用于 getBegSeedDetail 方法返回完整的求种详情
 */
public class BegSeedDetail {
    // BegSeed 表的字段
    public String begid;
    public int begnumbers;
    public int magic;
    public Date endtime;
    public int hasseed;
    
    // BegInfo 表的 Info 字段
    public String info;

    public BegSeedDetail() {}

    public BegSeedDetail(String begid, int begnumbers, int magic, Date endtime, int hasseed, String info) {
        this.begid = begid;
        this.begnumbers = begnumbers;
        this.magic = magic;
        this.endtime = endtime;
        this.hasseed = hasseed;
        this.info = info;
    }

    // 从 BegInfo 和 BegInfoDetail 构造
    public BegSeedDetail(BegInfo begInfo, BegInfoDetail begInfoDetail) {
        if (begInfo != null) {
            this.begid = begInfo.begid;
            this.begnumbers = begInfo.begnumbers;
            this.magic = begInfo.magic;
            this.endtime = begInfo.endtime;
            this.hasseed = begInfo.hasseed;
        }
        
        if (begInfoDetail != null) {
            this.info = begInfoDetail.info;
        }
    }
}
