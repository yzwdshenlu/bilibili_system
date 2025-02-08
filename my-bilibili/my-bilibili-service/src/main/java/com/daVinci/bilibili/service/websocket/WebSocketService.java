package com.daVinci.bilibili.service.websocket;

import com.alibaba.fastjson.JSONObject;
import com.daVinci.bilibili.domain.Danmu;
import com.daVinci.bilibili.domain.constant.UserMomentsConstant;
import com.daVinci.bilibili.service.DanmuService;
import com.daVinci.bilibili.service.util.RocketMQUtil;
import com.daVinci.bilibili.service.util.TokenUtil;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.service.websocket
 * @Author: daVinci
 * @CreateTime: 2025-02-05  22:00
 * @Description: WebSocket的Service
 * @Version: 1.0
 */
@Component
@ServerEndpoint("/imserver/{token}")
public class WebSocketService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass()); // 日志记录

    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0); // 保证线程安全的Integer类,用于统计在线人数

    public static final ConcurrentHashMap<String,WebSocketService> WEBSOCKET_MAP = new ConcurrentHashMap<>(); // 保证线程安全的Map,用于存储所有客户端的WebSocket

    private Session session; // 用于和客户端通信

    private String sessionId;

    private Long userId;

    private static ApplicationContext APPLICATION_CONTEXT; // 多例共用的上下文,可以从共用的上下文中获取bean
    
    public static void setApplicationContext(ApplicationContext applicationContext){
        WebSocketService.APPLICATION_CONTEXT = applicationContext;
    }


    /**
     * 连接成功时调用此方法
     * @param session
     */
    @OnOpen
    public void openConnection(Session session, @PathParam("token") String token){
        try {
            this.userId = TokenUtil.verifyToken(token); // 游客登录也可以访问，因此放入try-catch
        }catch (Exception ignored){}
        this.sessionId = session.getId(); // 当前会话的id
        this.session = session; // 当前id

        if (WEBSOCKET_MAP.containsKey(sessionId)){
            WEBSOCKET_MAP.remove(sessionId);
            WEBSOCKET_MAP.put(sessionId,this);
        }else{
            WEBSOCKET_MAP.put(sessionId,this);
            ONLINE_COUNT.getAndIncrement(); // 在线人数加1
        }
        logger.info("用户连接成功:" + sessionId + ", 当前在线人数为:" + ONLINE_COUNT.get());
        try {
            this.sendMessage("0"); // 0代表连接成功
        }catch (Exception e){
            logger.error("连接异常!");
        }
    }

    /**
     * 断开连接时调用此方法
     */
    @OnClose
    public void closeConnection(){
        if (WEBSOCKET_MAP.containsKey(sessionId)){
            WEBSOCKET_MAP.remove(sessionId);
            ONLINE_COUNT.getAndDecrement(); // 在线人数减1
        }
        logger.info("用户退出:" + sessionId + ", 当前在线人数为:" + ONLINE_COUNT.get());
    }

    /**
     * 收到弹幕时调用此方法
     * @param message
     */
    @OnMessage
    public void onMessage(String message){
        logger.info("用户信息:" + sessionId + ",报文:" + message);
        if (!StringUtil.isNullOrEmpty(message)){
            try {
                // 群发消息
                for (Map.Entry<String,WebSocketService> entry : WEBSOCKET_MAP.entrySet()){
                    WebSocketService webSocketService = entry.getValue();
                    DefaultMQProducer danmusProducer = (DefaultMQProducer) APPLICATION_CONTEXT.getBean("danmusProducer"); // 获取弹幕生产者
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("message",message);
                    jsonObject.put("sessionId",webSocketService.getSessionId());
                    Message msg = new Message(UserMomentsConstant.TOPIC_DANMUS, jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8)); // 设置消息与话题
                    RocketMQUtil.asyncSendMsg(danmusProducer,msg); // 消息的异步发送
                }
                if (this.userId != null){
                    // 保存弹幕到数据库
                    Danmu danmu = JSONObject.parseObject(message,Danmu.class);
                    danmu.setUserId(userId);
                    danmu.setCreateTime(new Date());
                    DanmuService danmuService = (DanmuService)APPLICATION_CONTEXT.getBean("danmuService");
                    danmuService.asyncAddDanmu(danmu);

                    //保存弹幕到redis
                    danmuService.addDanmusToRedis(danmu);
                }
            }catch (Exception e){
                logger.error("弹幕接收异常!");
                e.printStackTrace();
            }
        }
    }

    /**
     * 发生异常时调用此方法
     * @param error
     */
    @OnError
    public void OnError(Throwable error){

    }

    /**
     * 向客户端发送消息
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    //或直接指定时间间隔，例如：5秒
    @Scheduled(fixedRate=5000)
    private void noticeOnlineCount() throws IOException {
        for(Map.Entry<String, WebSocketService> entry : WebSocketService.WEBSOCKET_MAP.entrySet()){
            WebSocketService webSocketService = entry.getValue();
            if(webSocketService.session.isOpen()){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("onlineCount", ONLINE_COUNT.get());
                jsonObject.put("msg", "当前在线人数为" + ONLINE_COUNT.get());
                webSocketService.sendMessage(jsonObject.toJSONString());
            }
        }
    }

    public Session getSession() {
        return session;
    }

    public String getSessionId() {
        return sessionId;
    }
}
