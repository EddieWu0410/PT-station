package entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import com.querydsl.core.annotations.QueryEntity;

@QueryEntity
@Entity
@Table(name = "UserMigration")
public class Profile {// 迁移信息
    @Id
    @Column(name = "migration_id", length = 64, nullable = false)
    public String profileurl;

    @Column(name = "user_id", length = 36, nullable = false)
    public String userid;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", foreignKey = @ForeignKey(name = "fk_um_user"), insertable = false, updatable = false)
    public User user;

    @Column(name = "approved", nullable = false)
    public boolean exampass;

    @Column(name = "pending_magic", nullable = false)
    public String magictogive;

    @Column(name = "pending_uploaded", nullable = false)
    public String uploadtogive;

    @Transient
    public String downloadtogive;

    @Column(name = "granted_magic", nullable = false)
    public String magicgived;

    @Column(name = "granted_uploaded", nullable = false)
    public String uploadgived;

    @Transient
    public String downloadgived;

    @Column(name = "application_url", nullable = false)
    public String applicationurl;
}
