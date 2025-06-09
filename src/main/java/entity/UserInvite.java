package entity;

import java.io.Serializable;

import javax.persistence.*;
import com.querydsl.core.annotations.QueryEntity;

@QueryEntity
@Entity
@Table(name = "UserInvite")
@IdClass(UserInviteId.class)
public class UserInvite implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "user_id")
    public String userId;

    @Id
    @Column(name = "inviter_email")
    public String inviterEmail;

    @Column(name = "inviter_registered")
    public boolean inviterRegistered;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    public User user;
}
