package entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "BegDetail")
public class BegInfoDetail {
    @Id
    @Column(name = "beg_id", length = 64, nullable = false)
    public String begId;

    @Column(name = "user_id", length = 36, nullable = false)
    public String userId;

    @Column(name = "Info", nullable = false, columnDefinition = "TEXT")
    public String info;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    public User user;

    public BegInfoDetail() {}

    public BegInfoDetail(String begId, String userId, String info) {
        this.begId = begId;
        this.userId = userId;
        this.info = info;
    }
}
