package entity;

import java.io.Serializable;
import java.util.Objects;

public class UserInviteId implements Serializable {
    private static final long serialVersionUID = 1L;

    public String userId;
    public String inviterEmail;

    public UserInviteId() {
    }

    public UserInviteId(String userId, String inviterEmail) {
        this.userId = userId;
        this.inviterEmail = inviterEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInviteId that = (UserInviteId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(inviterEmail, that.inviterEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, inviterEmail);
    }
}
