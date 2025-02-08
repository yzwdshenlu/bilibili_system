package com.daVinci.bilibili.api;

import com.alibaba.fastjson.JSONObject;
import com.daVinci.bilibili.api.support.UserSupport;
import com.daVinci.bilibili.domain.JsonResponse;
import com.daVinci.bilibili.domain.PageResult;
import com.daVinci.bilibili.domain.User;
import com.daVinci.bilibili.domain.UserInfo;
import com.daVinci.bilibili.service.UserFollowingService;
import com.daVinci.bilibili.service.UserService;
import com.daVinci.bilibili.service.util.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.api
 * @Author: daVinci
 * @CreateTime: 2025-01-21  10:01
 * @Description: 用户controller
 * @Version: 1.0
 */
@RestController
public class UserApi {
    @Autowired
    private UserService userService;

    @Autowired
    private UserFollowingService userFollowingService;

    @Autowired
    private UserSupport userSupport;

    /**
     * 查询用户信息
     * @return
     */
    @GetMapping("/users")
    public JsonResponse<User> getUserInfo(){
        Long userId = userSupport.getCurrentUserId();
        User user = userService.getUserInfo(userId);
        return new JsonResponse<>(user);
    }

    /**
     * 获取RSA公钥
     * @return
     */
    @GetMapping("/rsa-pks")
    public JsonResponse<String> getRsaPublicKey(){
        String pk = RSAUtil.getPublicKeyStr();
        return new JsonResponse<>(pk);
    }

    /**
     * 注册用户
     * @param user
     * @return
     */
    @PostMapping("/users")
    public JsonResponse<String> addUser(@RequestBody User user){
        userService.addUser(user);
        return JsonResponse.success();
    }

    /**
     * 用户登录
     * @param user
     * @return
     * @throws Exception
     */
    @PostMapping("/user-tokens")
    public JsonResponse<String> login(@RequestBody User user) throws Exception{
        String token = userService.login(user);
        return new JsonResponse<>(token);
    }

    /**
     * 修改用户
     * @param user
     * @return
     * @throws Exception
     */
    @PutMapping("/users")
    public JsonResponse<String> updateUsers(@RequestBody User user) throws Exception{
        Long userId = userSupport.getCurrentUserId();
        user.setId(userId);
        userService.updateUsers(user);
        return JsonResponse.success();
    }

    /**
     * 修改用户信息
     * @param userInfo
     * @return
     */
    @PutMapping("/user-infos")
    public JsonResponse<String> updateUserInfos(@RequestBody UserInfo userInfo){
        Long userId = userSupport.getCurrentUserId();
        userInfo.setUserId(userId);
        userService.updateUserInfos(userInfo);
        return JsonResponse.success();
    }

    /**
     * 分页查询用户列表
     * @param no
     * @param size
     * @param nick
     * @return
     */
    @GetMapping("/user-infos")
    public JsonResponse<PageResult<UserInfo>> pageListUserInfos(@RequestParam Integer no,@RequestParam Integer size,String nick){
        Long userId = userSupport.getCurrentUserId();
        JSONObject params = new JSONObject();
        params.put("no",no); // 页码
        params.put("size",size); // 每页查询数量
        params.put("nick",nick);
        params.put("userId",userId);
        // 首先检查是否可以查到用户信息
        PageResult<UserInfo> result = userService.pageListUserInfos(params);

        if (result.getTotal() > 0){
            // 可以查到用户信息则开启分页查询,否则返回空
            List<UserInfo> checkedUserInfoList = userFollowingService.checkFollowingStatus(result.getList(),userId);
            result.setList(checkedUserInfoList);
        }
        return new JsonResponse<>(result);
    }

    /**
     * 使用双token实现用户登录
     * @param user
     * @return
     */
    @PostMapping("user-dts")
    public JsonResponse<Map<String,Object>> loginForDts(@RequestBody User user) throws Exception{
        Map<String,Object> map = userService.loginForDts(user);
        return new JsonResponse<>(map);
    }

    /**
     * 用户退出登录，删除refreshToken
     * @param request
     * @return
     */
    @DeleteMapping("refresh-tokens")
    public JsonResponse<String> logout(HttpServletRequest request){
        String refreshToken = request.getHeader("refreshToken");
        Long userId = userSupport.getCurrentUserId();
        userService.logout(refreshToken,userId);
        return JsonResponse.success();
    }

    /**
     * 刷新accessToken
     * @param request
     * @return
     */
    @PostMapping("access-tokens")
    public JsonResponse<String> refreshAccessToken(HttpServletRequest request) throws Exception{
        String refreshToken = request.getHeader("refreshToken");
        String accessToken = userService.refreshAccessToken(refreshToken);
        return new JsonResponse<>(accessToken);
    }

}
