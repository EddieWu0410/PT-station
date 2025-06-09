package entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.querydsl.core.annotations.QueryEntity;

@QueryEntity
@Entity(name="UserPT")
@Table(name = "UserPT")
public class UserPT {
    @Id
    @Column(name = "user_id", length = 36, nullable = false)
    public String userid;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", foreignKey = @ForeignKey(name = "fk_appeal_user"), insertable = false, updatable = false)
    public User user;

    @Column(name = "magic", nullable = false)
    public int magic;

    @Column(name = "uploaded", nullable = false)
    public long upload;

    @Column(name = "downloaded", nullable = false)
    public long download;

    @Column(name = "ratio", nullable = false, precision = 5, scale = 2)
    public double share;

    @Column(name = "default_seed_path")
    public String farmurl;

    @Column(name = "vip_downloads", nullable = false)
    public int viptime;
}
