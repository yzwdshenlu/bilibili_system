package com.daVinci.bilibili.api;


import com.daVinci.bilibili.api.support.UserSupport;
import com.daVinci.bilibili.domain.JsonResponse;
import com.daVinci.bilibili.domain.auth.UserAuthorities;
import com.daVinci.bilibili.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.api
 * @Author: daVinci
 * @CreateTime: 2025-01-22  18:55
 * @Description: 用户权限控制器
 * @Version: 1.0
 */
@RestController
public class UserAuthApi {
    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserAuthService userAuthService;

    /**
     * 获取用户权限
     * @return
     */
    @GetMapping("/user-authorities")
    public JsonResponse<UserAuthorities> getUserAuthorities(){
        Long userId = userSupport.getCurrentUserId();
        UserAuthorities userAuthorities = userAuthService.getUserAuthorities(userId);
        return new JsonResponse<>(userAuthorities);

    }
}
