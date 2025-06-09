package entity;

import java.io.Serializable;
import java.util.Objects;

public class UserId implements Serializable {
    private String userid;
    private String email;

    public UserId() {}

    public UserId(String userid, String email) {
        this.userid = userid;
        this.email = email;
    }

    // getters and setters
    public String getUserid() { return userid; }
    public void setUserid(String userid) { this.userid = userid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(userid, userId.userid) && Objects.equals(email, userId.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userid, email);
    }
}