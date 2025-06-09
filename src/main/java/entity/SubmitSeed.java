package entity;

import javax.persistence.*;

@Entity
@Table(name = "SubmitSeed")
public class SubmitSeed {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "begId", column = @Column(name = "beg_id")),
            @AttributeOverride(name = "seedId", column = @Column(name = "seed_id"))
    })
    public SubmitSeedId id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "beg_id", referencedColumnName = "beg_id", foreignKey = @ForeignKey(name = "fk_ss_beg"), insertable = false, updatable = false)
    public BegInfo begInfo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seed_id", referencedColumnName = "seed_id", foreignKey = @ForeignKey(name = "fk_ss_seed"), insertable = false, updatable = false)
    public Seed seed;

    @Column(name = "votes", nullable = false)
    public int votes;

    public SubmitSeed() {
    }
}