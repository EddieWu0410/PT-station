package entity;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryEntity;

@QueryEntity
@Entity(name = "User")
@Table(name = "User")
public class User {
    @Id
    @Column(name = "user_id", length = 36, nullable = false)
    public String userid;
    @Column(name = "email", nullable = false, length = 255, unique = true)
    public String email;
    @Column(name = "username", length = 100, nullable = false)
    public String username;
    @Column(name = "password", length = 255, nullable = false)
    public String password;
    @Column(name = "gender")
    @JsonProperty("gender")
    public String sex;

    @Column(name = "detectedCount", nullable = false)
    public int detectedCount = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastDetectedTime", nullable = false)
    public Date lastDetectedTime = new Date();

    @Column(name = "fake_detected_count", nullable = false)
    public int fakeDetectedCount = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fake_last_detected_time", nullable = false)
    public Date fakeLastDetectedTime = new Date();

    @Transient
    public String age;
    @Column(name = "school")
    public String school;
    @Column(name = "avatar_url")
    @JsonProperty("avatar_url")
    public String pictureurl;
    @Column(name = "bio")
    public String profile;
    @Column(name = "account_status", nullable = false)
    @JsonProperty("account_status")
    public boolean accountstate;
    @Column(name = "invite_left", nullable = false)
    @JsonProperty("invite_left")
    public int invitetimes;
    // @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    // public UserPT userPT;

    // Constructors, getters, setters
    public User() {
    }
}