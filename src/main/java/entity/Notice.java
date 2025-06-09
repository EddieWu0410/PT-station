package entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import com.querydsl.core.annotations.QueryEntity;
@QueryEntity
@Entity
@Table(name = "Announcement")
public class Notice {
    @Id
    @Column(name = "announce_id", length = 64, nullable = false)
    public String noticeid;

    @Lob
    @Column(name = "content", nullable = false)
    public String noticecontent;

    @Column(name = "is_public", nullable = false)
    public boolean state;

    @Column(name = "tag", length = 100)
    public String posttag;
    
    // 添加默认无参构造函数，JPA规范要求
    public Notice() {
    }
}
