package tracker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import cheat.Cheat;

/**
 * 作弊检测定时任务调度器
 * 管理两个定时任务：
 * 1. 每20秒检测假种子
 * 2. 每1分钟检测传输异常
 */
public class CheatDetectionScheduler {
    
    private final ScheduledExecutorService scheduler;
    private final Cheat cheat;
    private volatile boolean isRunning = false;
    
    public CheatDetectionScheduler() {
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true); // 设置为守护线程
            return t;
        });
        this.cheat = new Cheat();
    }
    
    /**
     * 启动定时任务
     */
    public void start() {
        if (isRunning) {
            System.out.println("CheatDetectionScheduler is already running");
            return;
        }
        
        System.out.println("Starting CheatDetectionScheduler...");
        
        // 启动假种子检测任务 - 每20秒执行一次
        scheduler.scheduleAtFixedRate(
            this::runFakeSeedDetection, 
            10, // 初始延迟10秒
            20, // 每20秒执行一次
            TimeUnit.SECONDS
        );
        
        // 启动传输异常检测任务 - 每1分钟执行一次
        scheduler.scheduleAtFixedRate(
            this::runTransDetection, 
            30, // 初始延迟30秒
            60, // 每60秒执行一次
            TimeUnit.SECONDS
        );
        
        isRunning = true;
        System.out.println("CheatDetectionScheduler started successfully");
    }
    
    /**
     * 停止定时任务
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        System.out.println("Stopping CheatDetectionScheduler...");
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("CheatDetectionScheduler did not terminate gracefully");
                }
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        isRunning = false;
        System.out.println("CheatDetectionScheduler stopped");
    }
    
    /**
     * 执行假种子检测
     */
    private void runFakeSeedDetection() {
        try {
            System.out.println("[" + new java.util.Date() + "] Running fake seed detection...");
            cheat.DetectFakeSeed();
            System.out.println("[" + new java.util.Date() + "] Fake seed detection completed");
        } catch (Exception e) {
            System.err.println("[" + new java.util.Date() + "] Error in fake seed detection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 执行传输异常检测
     */
    private void runTransDetection() {
        try {
            System.out.println("[" + new java.util.Date() + "] Running transaction detection...");
            cheat.DetectTrans();
            System.out.println("[" + new java.util.Date() + "] Transaction detection completed");
        } catch (Exception e) {
            System.err.println("[" + new java.util.Date() + "] Error in transaction detection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检查调度器是否正在运行
     */
    public boolean isRunning() {
        return isRunning && !scheduler.isShutdown();
    }
}
