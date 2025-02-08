package com.daVinci.bilibili.service;

import com.daVinci.bilibili.dao.AuthRoleMenuDao;
import com.daVinci.bilibili.domain.auth.AuthRoleMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.service
 * @Author: daVinci
 * @CreateTime: 2025-01-22  19:28
 * @Description: 角色的页面权限service
 * @Version: 1.0
 */
@Service
public class AuthRoleMenuService {

    @Autowired
    private AuthRoleMenuDao authRoleMenuDao;

    /**
     * 通过角色id的集合查询角色拥有的页面权限
     * @param roleIdSet
     * @return
     */
    public List<AuthRoleMenu> getAuthRoleMenusByRoleIds(Set<Long> roleIdSet) {
        return authRoleMenuDao.getAuthRoleMenusByRoleIds(roleIdSet);
    }
}
