package entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "SeedDownload")
public class SeedDownload {

    @Id
    @Column(name = "task_id", length = 64, nullable = false)
    public String taskId;

    @Column(name = "user_id", length = 36, nullable = false)
    public String userId;

    @Column(name = "seed_id", length = 64, nullable = false)
    public String seedId;

    @Column(name = "download_start", nullable = false)
    public LocalDateTime downloadStart;

    @Column(name = "download_end")
    public LocalDateTime downloadEnd;

    @Column(name = "is_dedicated", nullable = false)
    public boolean isDedicated;

    @Column(name = "client_ip", length = 45)
    public String clientIp;

    // Constructors
    public SeedDownload() {}

    public SeedDownload(String taskId, String userId, String seedId, LocalDateTime downloadStart, LocalDateTime downloadEnd, boolean isDedicated, String clientIp) {
        this.taskId = taskId;
        this.userId = userId;
        this.seedId = seedId;
        this.downloadStart = downloadStart;
        this.downloadEnd = downloadEnd;
        this.isDedicated = isDedicated;
        this.clientIp = clientIp;
    }

    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSeedId() {
        return seedId;
    }

    public void setSeedId(String seedId) {
        this.seedId = seedId;
    }

    public LocalDateTime getDownloadStart() {
        return downloadStart;
    }

    public void setDownloadStart(LocalDateTime downloadStart) {
        this.downloadStart = downloadStart;
    }

    public LocalDateTime getDownloadEnd() {
        return downloadEnd;
    }

    public void setDownloadEnd(LocalDateTime downloadEnd) {
        this.downloadEnd = downloadEnd;
    }

    public boolean isDedicated() {
        return isDedicated;
    }

    public void setDedicated(boolean dedicated) {
        isDedicated = dedicated;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    // equals and hashCode based on taskId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeedDownload)) return false;
        SeedDownload that = (SeedDownload) o;
        return taskId != null && taskId.equals(that.taskId);
    }

    @Override
    public int hashCode() {
        return 31 + (taskId != null ? taskId.hashCode() : 0);
    }
}