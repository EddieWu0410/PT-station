package entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "BegSeed")
public class BegInfo {
    @Id
    @Column(name = "beg_id", length = 64, nullable = false)
    public String begid;

    @Column(name = "beg_count", nullable = false)
    public int begnumbers;

    @Column(name = "reward_magic", nullable = false)
    public int magic;

    @Column(name = "deadline", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date endtime;

    @Column(name = "has_match", nullable = false)
    public int hasseed;
}
