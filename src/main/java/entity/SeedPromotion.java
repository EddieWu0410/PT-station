package entity;

import javax.persistence.*;
import java.util.Date;
import com.querydsl.core.annotations.QueryEntity;

@QueryEntity
@Entity
@Table(name = "SeedPromotion")
public class SeedPromotion {

    @Id
    @Column(name = "promotion_id", length = 64, nullable = false)
    public String promotionId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seed_id", referencedColumnName = "seed_id", foreignKey = @ForeignKey(name = "fk_seed_promotion"), nullable = false)
    public Seed seed;

    @Column(name = "start_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date startTime;

    @Column(name = "end_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date endTime;

    @Column(name = "discount", nullable = false)
    public int discount;

    public SeedPromotion() {}
}