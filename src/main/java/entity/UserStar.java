package entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "UserFavorite")
@IdClass(UserStarId.class)
public class UserStar {
    @Id
    @Column(name = "user_id", length = 36, nullable = false)
    public String userid;

    @Id
    @Column(name = "seed_id", length = 64, nullable = false)
    public String seedid;

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "user_id",
        referencedColumnName = "user_id",
        foreignKey = @ForeignKey(name = "fk_uf_user"),
        insertable = false, updatable = false
    )
    public User user;

    @ManyToOne(optional = false)
    @JoinColumn(
        name = "seed_id",
        referencedColumnName = "seed_id",
        foreignKey = @ForeignKey(name = "fk_uf_seed"),
        insertable = false, updatable = false
    )
    public Seed seed;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date createdAt;

    public UserStar() {}
}