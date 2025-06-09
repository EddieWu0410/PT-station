package entity;

/**
 * 包含种子信息和投票数的复合类
 */
public class SeedWithVotes {
    public Seed seed;
    public int votes;
    
    public SeedWithVotes() {
    }
    
    public SeedWithVotes(Seed seed, int votes) {
        this.seed = seed;
        this.votes = votes;
    }
}
