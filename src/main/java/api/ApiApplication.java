package api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import scheduler.SchedulerService;
import tracker.DataCaptureServer;
import java.io.IOException;

@SpringBootApplication
public class ApiApplication {
    private static SchedulerService schedulerService;
    
    public static void main(String[] args) {
        try{
            DataCaptureServer.start();
        }catch(IOException e){
            System.out.println(e);
        }
        
        // 启动定时任务服务
        schedulerService = new SchedulerService();
        schedulerService.start();
        
        // 添加关闭钩子，确保应用关闭时停止定时任务
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (schedulerService != null) {
                schedulerService.stop();
            }
        }));
        
        SpringApplication.run(ApiApplication.class, args);
    }
}