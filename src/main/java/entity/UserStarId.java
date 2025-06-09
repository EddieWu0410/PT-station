package entity;

import java.io.Serializable;
import java.util.Objects;

public class UserStarId implements Serializable {
    public String userid;  
    public String seedid;

    public UserStarId() {}  

    public UserStarId(String userid, String seedid) {
        this.userid = userid;
        this.seedid = seedid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserStarId that = (UserStarId) o;
        return Objects.equals(userid, that.userid) && Objects.equals(seedid, that.seedid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userid, seedid);
    }
}