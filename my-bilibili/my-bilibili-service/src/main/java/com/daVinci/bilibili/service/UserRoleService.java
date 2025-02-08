package com.daVinci.bilibili.service;

import com.daVinci.bilibili.dao.UserRoleDao;
import com.daVinci.bilibili.domain.auth.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.service
 * @Author: daVinci
 * @CreateTime: 2025-01-22  19:01
 * @Description: 用户关联角色的service
 * @Version: 1.0
 */
@Service
public class UserRoleService {

    @Autowired
    private UserRoleDao userRoleDao;

    /**
     * 通过用户id查询用户对应的角色
     * @param userId
     * @return
     */
    public List<UserRole> getUserRoleByUserId(Long userId) {
        return userRoleDao.getUserRoleByUserId(userId);
    }

    public void addUserRole(UserRole userRole) {
        userRole.setCreateTime(new Date()); // 设置创建时间
        userRoleDao.addUserRole(userRole);
    }
}
