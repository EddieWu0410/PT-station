package entity;

import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "UserVotes")
public class UserVotes {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "userId", column = @Column(name = "user_id")),
            @AttributeOverride(name = "begId", column = @Column(name = "beg_id")),
            @AttributeOverride(name = "seedId", column = @Column(name = "seed_id"))
    })
    public UserVotesId id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", foreignKey = @ForeignKey(name = "fk_user_votes_user"), insertable = false, updatable = false)
    public User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "beg_id", referencedColumnName = "beg_id", foreignKey = @ForeignKey(name = "fk_user_votes_beg"), insertable = false, updatable = false)
    public BegInfo begInfo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "seed_id", referencedColumnName = "seed_id", foreignKey = @ForeignKey(name = "fk_user_votes_seed"), insertable = false, updatable = false)
    public Seed seed;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date createdAt;

    public UserVotes() {
    }
}