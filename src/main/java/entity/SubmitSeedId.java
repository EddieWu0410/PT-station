package entity;

import java.io.Serializable;
import java.util.Objects;

public class SubmitSeedId implements Serializable {
    private String begId;
    private String seedId;

    public SubmitSeedId() {}

    public SubmitSeedId(String begId, String seedId) {
        this.begId = begId;
        this.seedId = seedId;
    }

    // 重写 equals 和 hashCode 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubmitSeedId that = (SubmitSeedId) o;
        return begId.equals(that.begId) && seedId.equals(that.seedId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(begId, seedId);
    }

    // getters and setters
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
