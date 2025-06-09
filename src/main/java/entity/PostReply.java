package entity;

import java.util.Date;
import javax.persistence.*;

@Entity
@Table(name = "PostReply")
public class PostReply {
    @Id
    @Column(name = "reply_id", length = 64, nullable = false)
    public String replyid;

    @Column(name = "post_id", length = 64, nullable = false)
    public String postid;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id", referencedColumnName = "post_id", 
                foreignKey = @ForeignKey(name = "fk_pr_post"), 
                insertable = false, updatable = false)
    public Post post;

    @Column(name = "author_id", length = 36, nullable = false)
    public String authorid;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", referencedColumnName = "user_id", 
                foreignKey = @ForeignKey(name = "fk_pr_user"), 
                insertable = false, updatable = false)
    public User author;

    @Lob
    @Column(name = "content", nullable = false)
    public String content;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date createdAt;
}