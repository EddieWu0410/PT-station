package entity;

import javax.persistence.*;

@Entity
@Table(name = "SeedHash")
public class SeedHash {
    @Id
    @Column(name = "seed_id", length = 64)
    public String seedId;

    @Column(name = "info_hash", length = 40, nullable = false)
    public String infoHash;

    // optional back‐ref
    @ManyToOne
    @JoinColumn(name = "seed_id", insertable = false, updatable = false)
    public Seed seed;
}
