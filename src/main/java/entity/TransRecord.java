package entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Transport")
public class TransRecord{
    @Id
    @Column(name = "task_id", length = 64, nullable = false)
    public String taskid;

    @Column(name = "uploader_id", length = 36, nullable = true)
    public String uploaduserid;

    @ManyToOne(optional = true)
    @JoinColumn(name = "uploader_id", referencedColumnName = "user_id", foreignKey = @ForeignKey(name = "fk_tr_user_up"), insertable = false, updatable = false)
    public User uploader;

    @Column(name = "downloader_id", length = 36, nullable = true)
    public String downloaduserid;

    @ManyToOne(optional = true)
    @JoinColumn(name = "downloader_id", referencedColumnName = "user_id", foreignKey = @ForeignKey(name = "fk_tr_user_down"), insertable = false, updatable = false)
    public User downloader;

    @Column(name = "seed_id", length = 64, nullable = true)
    public String seedid;

    @ManyToOne(optional = true)
    @JoinColumn(name = "seed_id", referencedColumnName = "seed_id", foreignKey = @ForeignKey(name = "fk_tr_seed"), insertable = false, updatable = false)
    public Seed seed;

    @Column(name = "uploaded", nullable = true)
    public Long upload = 0L;

    @Column(name = "downloaded", nullable = true)
    public Long download = 0L;

    @Column(name = "upload_peak", nullable = true)
    public Long maxupload = 0L;

    @Column(name = "download_peak", nullable = true)
    public Long maxdownload = 0L;
    
    // 默认构造函数，确保字段初始化
    public TransRecord() {
        this.upload = 0L;
        this.download = 0L;
        this.maxupload = 0L;
        this.maxdownload = 0L;
    }
}