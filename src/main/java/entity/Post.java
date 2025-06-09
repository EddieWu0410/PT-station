package entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import com.querydsl.core.annotations.QueryEntity;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;

@QueryEntity
@Entity
@Table(name = "Post")
public class Post {
    @Id
    @Column(name = "post_id", length = 64, nullable = false)
    public String postid;

    @Column(name = "title", length = 255, nullable = false)
    public String posttitle;

    @Lob
    @Column(name = "content", nullable = false)
    public String postcontent;

    @Column(name = "author_id", length = 36, nullable = false)
    public String postuserid;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", referencedColumnName = "user_id", foreignKey = @ForeignKey(name = "fk_post_user"), insertable = false, updatable = false)
    public User author;
    
    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date posttime;

    @Column(name = "reply_count", nullable = false)
    public int replytime;

    @Column(name = "view_count", nullable = false)
    public int readtime;
}
