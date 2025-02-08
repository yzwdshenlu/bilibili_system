package com.daVinci.bilibili.service;

import com.daVinci.bilibili.dao.AuthRoleDao;
import com.daVinci.bilibili.domain.auth.AuthRole;
import com.daVinci.bilibili.domain.auth.AuthRoleElementOperation;
import com.daVinci.bilibili.domain.auth.AuthRoleMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.service
 * @Author: daVinci
 * @CreateTime: 2025-01-22  19:02
 * @Description: 角色关联权限的service
 * @Version: 1.0
 */
@Service
public class AuthRoleService {

    @Autowired
    private AuthRoleElementOperationService authRoleElementOperationService; // 查询角色关联的操作权限

    @Autowired
    private AuthRoleMenuService authRoleMenuService; // 查询用户关联的页面权限

    @Autowired
    private AuthRoleDao authRoleDao;

    /**
     * 通过角色id的集合查询角色拥有的操作权限，两表联查
     * @param roleIdSet
     * @return
     */
    public List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(Set<Long> roleIdSet) {
        return authRoleElementOperationService.getRoleElementOperationsByRoleIds(roleIdSet);
    }

    /**
     * 通过角色id的集合查询角色拥有的页面权限，两表联查
     * @param roleIdSet
     * @return
     */
    public List<AuthRoleMenu> getAuthRoleMenusByRoleIds(Set<Long> roleIdSet) {
        return authRoleMenuService.getAuthRoleMenusByRoleIds(roleIdSet);
    }

    /**
     * 通过角色编码查询对应的角色
     * @param code
     * @return
     */
    public AuthRole getRoleByCode(String code) {
        return authRoleDao.getRoleByCode(code);
    }
}
