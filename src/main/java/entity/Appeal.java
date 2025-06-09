package entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Appeal")
public class Appeal {
    @Id
    @Column(name = "appeal_id", length = 64, nullable = false)
    public String appealid;

    @Column(name = "user_id", length = 36, nullable = false)
    public String appealuserid;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", foreignKey = @ForeignKey(name = "fk_appeal_user"), insertable = false, updatable = false)
    public User user;

    @Lob
    @Column(name = "content", nullable = false)
    public String content;

    @Column(name = "file_url", length = 255)
    public String fileURL;

    @Column(name = "status", nullable = false)
    public int status; // 0: pending, 1: approved, 2: rejected
}
