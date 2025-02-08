package com.daVinci.bilibili.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.service.config
 * @Author: daVinci
 * @CreateTime: 2025-02-05  21:56
 * @Description: Websocket配置类
 * @Version: 1.0
 */
@Configuration
public class WebSocketConfig {

    /**
     * 启动WebSocket服务
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        return new ServerEndpointExporter();
    }
}
