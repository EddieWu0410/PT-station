package scheduler;

import cheat.Cheat;
import database.Database1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务服务，负责定期执行各种维护任务
 */
public class SchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
    private final ScheduledExecutorService scheduler;
    private final Cheat cheat;
    private final Database1 database1;
    
    public SchedulerService() {
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.cheat = new Cheat();
        this.database1 = new Database1();
        logger.info("SchedulerService 初始化完成");
    }
    
    /**
     * 启动所有定时任务
     */
    public void start() {
        // 每分钟执行一次 PunishUser 函数
        scheduler.scheduleAtFixedRate(
            this::executePunishUser,
            0, // 初始延迟1分钟
            1, // 每1分钟执行一次
            TimeUnit.MINUTES
        );
        
        logger.info("定时任务已启动 - PunishUser 任务将每分钟执行一次");
    }
    
    /**
     * 执行用户惩罚任务
     */
    private void executePunishUser() {
        try {
            logger.info("开始执行 PunishUser 定时任务");
            cheat.PunishUser();
            database1.SettleBeg();
            logger.info("PunishUser 定时任务执行完成");
        } catch (Exception e) {
            logger.error("执行 PunishUser 定时任务时发生错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 停止所有定时任务
     */
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                logger.info("定时任务服务已停止");
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
                logger.warn("停止定时任务服务时被中断");
            }
        }
    }
}
