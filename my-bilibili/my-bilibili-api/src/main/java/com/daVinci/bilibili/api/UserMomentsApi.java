package com.daVinci.bilibili.api;

import com.daVinci.bilibili.api.support.UserSupport;
import com.daVinci.bilibili.domain.JsonResponse;
import com.daVinci.bilibili.domain.UserMoment;
import com.daVinci.bilibili.domain.annotation.ApiLimitedRole;
import com.daVinci.bilibili.domain.annotation.DataLimited;
import com.daVinci.bilibili.domain.constant.AuthRoleConstant;
import com.daVinci.bilibili.service.UserMomentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.api
 * @Author: daVinci
 * @CreateTime: 2025-01-22  15:51
 * @Description: 用户动态控制器
 * @Version: 1.0
 */
@RestController
public class UserMomentsApi {

    @Autowired
    private UserMomentsService userMomentsService;

    @Autowired
    private UserSupport userSupport;

    /**
     * 新建用户动态
     * @param userMoment
     * @return
     */
    @ApiLimitedRole(limitedRoleCodeList = {AuthRoleConstant.ROLE_LV0}) // Lv0等级不可发布动态
    @DataLimited // Lv0等级只能发布视频动态(type = 0)
    @PostMapping("user-moments")
    public JsonResponse<String> addUserMoments(@RequestBody UserMoment userMoment) throws Exception{
        Long userId = userSupport.getCurrentUserId();
        userMoment.setUserId(userId);
        userMomentsService.addUserMoments(userMoment);
        return JsonResponse.success();
    }

    /**
     * 获取当前用户订阅的所有动态
     * @return
     */
    @GetMapping("/user-subscribed-moment")
    public JsonResponse<List<UserMoment>> getUserSubscribedMoments(){
        Long userId = userSupport.getCurrentUserId();
        List<UserMoment> list = userMomentsService.getUserSubscribedMoments(userId);
        return new JsonResponse<>(list);
    }
}
