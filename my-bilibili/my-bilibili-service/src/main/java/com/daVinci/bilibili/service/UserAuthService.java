package com.daVinci.bilibili.service;

import com.daVinci.bilibili.domain.auth.*;
import com.daVinci.bilibili.domain.constant.AuthRoleConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.service
 * @Author: daVinci
 * @CreateTime: 2025-01-22  18:56
 * @Description: 用户权限service
 * @Version: 1.0
 */
@Service
public class UserAuthService {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private AuthRoleService authRoleService;

    /**
     * 获取用户权限
     *
     * @param userId
     * @return
     */
    public UserAuthorities getUserAuthorities(Long userId) {
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId); // 获取用户对应的角色
        Set<Long> roleIdSet = userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toSet()); // 从集合中抽取角色对应的id
        List<AuthRoleElementOperation> roleElementOperationList = authRoleService.getRoleElementOperationsByRoleIds(roleIdSet); // 查询用户的操作权限
        List<AuthRoleMenu> authRoleMenuList = authRoleService.getAuthRoleMenusByRoleIds(roleIdSet); // 查询用户的页面权限
        UserAuthorities userAuthorities = new UserAuthorities();
        userAuthorities.setRoleElementOperationList(roleElementOperationList);
        userAuthorities.setRoleMenuList(authRoleMenuList);
        return userAuthorities;
    }

    /**
     * 注册用户后添加用户的默认角色(Lv0)
     * @param id
     */
    public void addUserDefaultRole(Long id) {
        UserRole userRole = new UserRole();
        AuthRole role = authRoleService.getRoleByCode(AuthRoleConstant.ROLE_LV0); // 通过角色编码查询对应的角色
        userRole.setUserId(id); // 设置用户id
        userRole.setRoleId(role.getId()); // 设置角色id
        userRoleService.addUserRole(userRole); // 添加用户默认角色
    }
}
