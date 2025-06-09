package entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "VipSeed")
public class VipSeed {
    @Id
    @Column(name = "seed_id", length = 64, nullable = false)
    public String seedid;

    @OneToOne(optional = false)
    @JoinColumn(name = "seed_id", referencedColumnName = "seed_id", foreignKey = @ForeignKey(name = "fk_vip_seed"), insertable = false, updatable = false)
    public Seed seed;

    @Column(name = "seeder_count", nullable = false)
    public int seedercount;

    @Column(name = "reward_magic", nullable = false)
    public int rewardmagic;

    @Column(name = "stop_caching", nullable = false)
    public int stopcaching;

    @Column(name = "bonus", nullable = false)
    public int bonus;

    @Column(name = "cache_status", nullable = false)
    public boolean cachestate;

    @Column(name = "farmer_number", nullable = false)
    public int farmernumber;

    public VipSeed() {
        // 默认构造函数
    }
}