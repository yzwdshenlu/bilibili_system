package com.daVinci.bilibili.service;

import com.daVinci.bilibili.dao.AuthRoleElementOperationDao;
import com.daVinci.bilibili.domain.auth.AuthRoleElementOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.service
 * @Author: daVinci
 * @CreateTime: 2025-01-22  19:28
 * @Description: 角色的操作权限service
 * @Version: 1.0
 */
@Service
public class AuthRoleElementOperationService {

    @Autowired
    private AuthRoleElementOperationDao authRoleElementOperationDao;

    /**
     * 通过角色id的集合查询关联的操作权限
     * @param roleIdSet
     * @return
     */
    public List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(Set<Long> roleIdSet) {
        return authRoleElementOperationDao.getRoleElementOperationsByRoleIds(roleIdSet);
    }
}
