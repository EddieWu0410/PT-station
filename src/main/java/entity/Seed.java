package entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
// import com.querydsl.core.annotations.QueryEntity;

// @QueryEntity
@Entity
@Table(name = "Seed")
public class Seed {
    @Id
    @Column(name = "seed_id", length = 64, nullable = false)
    public String seedid;

    @Column(name = "owner_user_id", length = 36, nullable = false)
    public String seeduserid;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_user_id", referencedColumnName = "user_id", foreignKey = @ForeignKey(name = "fk_seed_user"), insertable = false, updatable = false)
    public User user;

    @Column(name = "fake_hits", nullable = false)
    public int faketime;

    @Column(name = "last_fake_check")
    @Temporal(TemporalType.TIMESTAMP)
    public Date lastfakecheck;

    @Column(name = "external_url")
    public String outurl;

    @Column(name = "title", length = 255, nullable = false)
    public String title;

    @Column(name = "subtitle", length = 255)
    public String subtitle;

    @Column(name = "size", length = 50, nullable = false)
    public String seedsize;

    @Column(name = "tags")
    public String seedtag;

    @Column(name = "popularity", nullable = false)
    public int downloadtimes;

    @Column(name = "URL", columnDefinition = "TEXT")
    public String url;
}
