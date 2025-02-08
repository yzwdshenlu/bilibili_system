package com.daVinci;/**
 * Created by daVinci on 2025/1/20.
 */

import com.daVinci.bilibili.service.websocket.WebSocketService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @title: MyBilibiliApp
 * @Author daVinci
 * @Date: 2025/1/20 16:49
 * @Version 1.0
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
public class MyBilibiliApp {
    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(MyBilibiliApp.class,args);
        WebSocketService.setApplicationContext(app);
    }
}
