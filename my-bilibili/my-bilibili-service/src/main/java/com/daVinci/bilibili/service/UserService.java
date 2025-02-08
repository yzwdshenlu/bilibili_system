package com.daVinci.bilibili.service;

import com.alibaba.fastjson.JSONObject;
import com.daVinci.bilibili.dao.UserDao;
import com.daVinci.bilibili.domain.PageResult;
import com.daVinci.bilibili.domain.RefreshTokenDetail;
import com.daVinci.bilibili.domain.User;
import com.daVinci.bilibili.domain.UserInfo;
import com.daVinci.bilibili.domain.constant.UserConstant;
import com.daVinci.bilibili.domain.exception.ConditionException;
import com.daVinci.bilibili.service.util.MD5Util;
import com.daVinci.bilibili.service.util.RSAUtil;
import com.daVinci.bilibili.service.util.TokenUtil;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.service
 * @Author: daVinci
 * @CreateTime: 2025-01-21  10:00
 * @Description: 用户的Service类
 * @Version: 1.0
 */
@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAuthService userAuthService;

    /**
     * 注册用户
     * @param user
     */
    @Transactional
    public void addUser(User user) {
        String phone = user.getPhone();
        if (StringUtils.isNullOrEmpty(phone)){
            throw new ConditionException("手机号不能为空!");
        }
        User dbUser = this.getUserByPhone(phone);
        if (dbUser != null) {
            throw new ConditionException("该手机号已经注册!");
        }
        // 开始注册
        Date now = new Date();
        // 生成盐值
        String salt = String.valueOf(now.getTime());
        String password = user.getPassword();
        String rawPassword;
        try {
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("密码解密失败!");
        }
        // 加密存入数据库
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        user.setSalt(salt);
        user.setPassword(md5Password);
        user.setCreateTime(now);
        // 注册用户
        userDao.addUser(user);
        // 添加用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setNick(UserConstant.DEFAULT_NICK);
        userInfo.setBirth(UserConstant.DEFAULT_BIRTH);
        userInfo.setGender(UserConstant.GENDER_MALE);
        userInfo.setCreateTime(now);
        userDao.addUserInfo(userInfo);
        // 添加用户默认权限角色
        userAuthService.addUserDefaultRole(user.getId());
    }

    /**
     * 数据库中通过手机号查询用户
     * @param phone
     * @return
     */
    public User getUserByPhone(String phone){
        return userDao.getUserByPhone(phone);
    }

    /**
     * 用户登录
     * @param user
     * @return 返回token
     */
    public String login(User user) throws Exception{
        // 判断用户是否合法
        String phone = user.getPhone();
        if (StringUtils.isNullOrEmpty(phone)){
            throw new ConditionException("手机号不能为空!");
        }
        User dbUser = this.getUserByPhone(phone);
        if (dbUser == null) {
            throw new ConditionException("当前用户不存在!");
        }
        String password = user.getPassword();
        String rawPassword;
        try {
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("密码解密失败!");
        }
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        // 当前密码和数据库中用户的密码进行比对
        if (!md5Password.equals(dbUser.getPassword())){
            throw new ConditionException("密码错误!");
        }

        // 用户合法，生成用户令牌
        return TokenUtil.generateToken(dbUser.getId());
    }

    /**
     * 获取用户信息
     * @param userId
     * @return
     */
    public User getUserInfo(Long userId) {
        User user = userDao.getUserById(userId);
        UserInfo userInfo = userDao.getUserInfoByUserId(userId);
        user.setUserInfo(userInfo);
        return user;
    }

    /**
     * 修改用户
     * @param user
     */
    public void updateUsers(User user) throws Exception{
        Long id = user.getId();
        User dbUser = userDao.getUserById(id);
        if (dbUser == null) {
            throw new ConditionException("用户不存在!");
        }
        if (!StringUtils.isNullOrEmpty(user.getPassword())){
            String rawPassword = RSAUtil.decrypt(user.getPassword());
            String md5Password = MD5Util.sign(rawPassword,dbUser.getSalt(),"UTF-8");
            user.setPassword(md5Password);
        }
        user.setUpdateTime(new Date());
        userDao.updateUsers(user);
    }

    /**
     * 修改用户信息
     * @param userInfo
     */
    public void updateUserInfos(UserInfo userInfo) {
        userInfo.setUpdateTime(new Date());
        userDao.updateUserInfos(userInfo);
    }

    /**
     * 根据id查询单个用户
     * @param id
     * @return
     */
    public User getUserById(Long id) {
        return userDao.getUserById(id);
    }

    /**
     * 批量查询用户信息
     * @param userIdList
     * @return
     */
    public List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList) {
        return userDao.getUserInfoByUserIds(userIdList);
    }

    /**
     * 分页查询用户信息列表
     * @param params
     * @return
     */
    public PageResult<UserInfo> pageListUserInfos(JSONObject params) {
        Integer no = params.getInteger("no");
        Integer size = params.getInteger("size");
        params.put("start",(no - 1) * size); //起始位置
        params.put("limit",size);
        Integer total = userDao.pageCountUserInfos(params);
        List<UserInfo> list = new ArrayList<>();
        if (total > 0){
            list = userDao.pageListUserInfos(params);
        }
        return new PageResult<>(total,list);
    }

    /**
     * 双token实现用户登录
     * @param user
     * @return
     * @throws Exception
     */
    public Map<String, Object> loginForDts(User user) throws Exception{

        // 判断用户是否合法
        String phone = user.getPhone();
        if (StringUtils.isNullOrEmpty(phone)){
            throw new ConditionException("手机号不能为空!");
        }
        User dbUser = this.getUserByPhone(phone);
        if (dbUser == null) {
            throw new ConditionException("当前用户不存在!");
        }
        String password = user.getPassword();
        String rawPassword;
        try {
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("密码解密失败!");
        }
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        // 当前密码和数据库中用户的密码进行比对
        if (!md5Password.equals(dbUser.getPassword())){
            throw new ConditionException("密码错误!");
        }
        Long userId = dbUser.getId();
        // 用户合法，生成用户令牌
        String accessToken = TokenUtil.generateToken(userId);
        String refreshToken = TokenUtil.generateRefreshToken(userId);

        // 保存refreshToken到数据库,删除再添加
        userDao.deleteRefreshToken(refreshToken,userId);
        userDao.addRefreshToken(refreshToken,userId,new Date());
        Map<String,Object> result = new HashMap<>();
        result.put("accessToken",accessToken);
        result.put("refreshToken",refreshToken);
        return result;
    }

    /**
     * 用户登出时删除refreshToken
     * @param refreshToken
     * @param userId
     */
    public void logout(String refreshToken, Long userId) {
        userDao.deleteRefreshToken(refreshToken, userId);
    }

    /**
     * 刷新accessToken
     * @param refreshToken
     * @return
     */
    public String refreshAccessToken(String refreshToken) throws Exception{
        RefreshTokenDetail refreshTokenDetail = userDao.getRefreshTokenDetail(refreshToken); // 查询refreshToken
        if (refreshTokenDetail == null){
            throw new ConditionException("555","token过期!"); //refreshToken过期，需要重新登录
        }
        Long userId = refreshTokenDetail.getUserId();
        return TokenUtil.generateToken(userId); // refreshToken未过期，刷新accessToken
    }

    /**
     * 批量查询用户信息
     * @param userIdList
     * @return
     */
    public List<UserInfo> batchGetUserInfoByUserIds(Set<Long> userIdList) {
        return userDao.batchGetUserInfoByUserIds(userIdList);
    }
}
