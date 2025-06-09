package entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.querydsl.core.annotations.QueryEntity;

@QueryEntity
@Entity(name = "Admin")
@Table(name = "admin")
public class Admin {
    
    @Id
    @Column(name = "user_id", length = 36, nullable = false)
    public String userId;
    
    public Admin() {
    }
    
    public Admin(String userId) {
        this.userId = userId;
    }
}
