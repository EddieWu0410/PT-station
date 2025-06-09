package entity;

import java.util.Date;

public class SeedWithPromotionDTO {
    public String seedid;
    public String seeduserid;
    public String username; // 只包含用户名，而不是整个User对象
    public int faketime;
    public Date lastfakecheck;
    public String outurl;
    public String title;
    public String subtitle;
    public String seedsize;
    public String seedtag;
    public int downloadtimes;
    public String url;
    public Integer discount; // 促销折扣，可能为null如果没有促销
    
    public SeedWithPromotionDTO() {}
    
    public SeedWithPromotionDTO(Seed seed, Integer discount) {
        this.seedid = seed.seedid;
        this.seeduserid = seed.seeduserid;
        this.username = seed.user != null ? seed.user.username : null;
        this.faketime = seed.faketime;
        this.lastfakecheck = seed.lastfakecheck;
        this.outurl = seed.outurl;
        this.title = seed.title;
        this.subtitle = seed.subtitle;
        this.seedsize = seed.seedsize;
        this.seedtag = seed.seedtag;
        this.downloadtimes = seed.downloadtimes;
        this.url = seed.url;
        this.discount = discount;
    }
}
