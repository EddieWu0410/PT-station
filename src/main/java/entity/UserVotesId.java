package entity;

import java.io.Serializable;
import java.util.Objects;

public class UserVotesId implements Serializable {
    private String userId;
    private String begId;
    private String seedId;

    public UserVotesId() {}

    public UserVotesId(String userId, String begId, String seedId) {
        this.userId = userId;
        this.begId = begId;
        this.seedId = seedId;
    }

    // 重写 equals 和 hashCode 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserVotesId that = (UserVotesId) o;
        return userId.equals(that.userId) && begId.equals(that.begId) && seedId.equals(that.seedId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, begId, seedId);
    }

    // getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBegId() {
        return begId;
    }

    public void setBegId(String begId) {
        this.begId = begId;
    }

    public String getSeedId() {
        return seedId;
    }

    public void setSeedId(String seedId) {
        this.seedId = seedId;
    }
}
