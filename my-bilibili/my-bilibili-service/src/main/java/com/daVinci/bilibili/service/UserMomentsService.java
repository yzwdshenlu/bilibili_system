package com.daVinci.bilibili.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.daVinci.bilibili.dao.UserMomentsDao;
import com.daVinci.bilibili.domain.UserMoment;
import com.daVinci.bilibili.domain.constant.UserMomentsConstant;
import com.daVinci.bilibili.service.util.RocketMQUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.service
 * @Author: daVinci
 * @CreateTime: 2025-01-22  15:52
 * @Description: 用户动态的service
 * @Version: 1.0
 */
@Service
public class UserMomentsService {
    @Autowired
    private UserMomentsDao userMomentsDao;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 新建用户动态
     * @param userMoment
     */
    public void addUserMoments(UserMoment userMoment) throws Exception{
        userMoment.setCreateTime(new Date());
        userMomentsDao.addUserMoments(userMoment); // 新建用户动态
        DefaultMQProducer producer = (DefaultMQProducer)applicationContext.getBean("momentsProducer"); //通过应用上下文获取RocketMQ配置类中的生产者Bean
        Message msg = new Message(UserMomentsConstant.TOPIC_MOMENTS, JSONObject.toJSONString(userMoment).getBytes(StandardCharsets.UTF_8)); // 设置消息与话题
        RocketMQUtil.syncSendMsg(producer,msg); // 同步发送消息到消息队列中
    }

    /**
     * 获取当前用户订阅的所有动态
     * @param userId
     * @return
     */
    public List<UserMoment> getUserSubscribedMoments(Long userId) {
        String key = "subscribed-" + userId;
        String listStr = redisTemplate.opsForValue().get(key);
        return JSONArray.parseArray(listStr,UserMoment.class);

    }
}
