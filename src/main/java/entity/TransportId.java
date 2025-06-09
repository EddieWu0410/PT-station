package entity;

import java.io.Serializable;
import java.util.Objects;

public class TransportId implements Serializable {
    private String taskid;
    private String uploaduserid;
    private String downloaduserid;

    public TransportId() {}

    public TransportId(String taskid, String uploaduserid, String downloaduserid) {
        this.taskid = taskid;
        this.uploaduserid = uploaduserid;
        this.downloaduserid = downloaduserid;
    }

    public String getTaskid() { return taskid; }
    public void setTaskid(String taskid) { this.taskid = taskid; }
    public String getUploaduserid() { return uploaduserid; }
    public void setUploaduserid(String uploaduserid) { this.uploaduserid = uploaduserid; }
    public String getDownloaduserid() { return downloaduserid; }
    public void setDownloaduserid(String downloaduserid) { this.downloaduserid = downloaduserid; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransportId that = (TransportId) o;
        return Objects.equals(taskid, that.taskid) && Objects.equals(uploaduserid, that.uploaduserid) && Objects.equals(downloaduserid, that.downloaduserid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskid, uploaduserid, downloaduserid);
    }
}