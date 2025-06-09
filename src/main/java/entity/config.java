package entity;

public class config {
    // 可配置的参数 - 使用静态变量而非final，以便动态修改
    private static int farmNumber = 3;
    private static int fakeTime = 3;
    private static int begVote = 3;
    private static int cheatTime = 5;
    
    // 数据库相关配置 - 保持final，因为运行时不应修改
    public static final String SqlURL = "10.126.59.25:3306";
    public static final String Database = "pt_database_test";
    public static final String TestDatabase = "pt_database_test";
    public static final String SqlPassword = "123456";
    public static final String SqlUsername = "root";
    public static final String TORRENT_STORAGE_DIR = "torrents";
    public static final String APPEAL_STORAGE_DIR = "appeals";
    public static final String MIGRATION_STORAGE_DIR = "migrations";
    public static final String trackerHost = "0.0.0.0";
    public static final int trackerPort = 6969;
    public static final int capturePort = 6971;
    
    // FarmNumber 的 getter 和 setter
    public static int getFarmNumber() {
        return farmNumber;
    }
    
    public static void setFarmNumber(int farmNumber) {
        if (farmNumber > 0) {
            config.farmNumber = farmNumber;
        } else {
            throw new IllegalArgumentException("FarmNumber must be positive");
        }
    }
    
    // FakeTime 的 getter 和 setter
    public static int getFakeTime() {
        return fakeTime;
    }
    
    public static void setFakeTime(int fakeTime) {
        if (fakeTime > 0) {
            config.fakeTime = fakeTime;
        } else {
            throw new IllegalArgumentException("FakeTime must be positive");
        }
    }
    
    // BegVote 的 getter 和 setter
    public static int getBegVote() {
        return begVote;
    }
    
    public static void setBegVote(int begVote) {
        if (begVote > 0) {
            config.begVote = begVote;
        } else {
            throw new IllegalArgumentException("BegVote must be positive");
        }
    }
    
    // CheatTime 的 getter 和 setter
    public static int getCheatTime() {
        return cheatTime;
    }
    
    public static void setCheatTime(int cheatTime) {
        if (cheatTime > 0) {
            config.cheatTime = cheatTime;
        } else {
            throw new IllegalArgumentException("CheatTime must be positive");
        }
    }
    
    // 获取所有配置参数的方法
    public static String getAllConfigs() {
        return String.format("Config: FarmNumber=%d, FakeTime=%d, BegVote=%d, CheatTime=%d",
                farmNumber, fakeTime, begVote, cheatTime);
    }
    
    // 重置所有参数为默认值
    public static void resetToDefaults() {
        farmNumber = 3;
        fakeTime = 3;
        begVote = 3;
        cheatTime = 5;
    }
    // public static final String trackerHost="0.0.0.0";
    // public static final int trackerPort=6969;
    // public static final int capturePort=6670;
}
