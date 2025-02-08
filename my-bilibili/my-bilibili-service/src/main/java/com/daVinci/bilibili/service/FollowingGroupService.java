package com.daVinci.bilibili.service;

import com.daVinci.bilibili.dao.FollowingGroupDao;
import com.daVinci.bilibili.domain.FollowingGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.service
 * @Author: daVinci
 * @CreateTime: 2025-01-21  15:49
 * @Description: 关注分组service
 * @Version: 1.0
 */
@Service
public class FollowingGroupService {
    @Autowired
    private FollowingGroupDao followingGroupDao;

    /**
     * 通过分组类型查询分组信息
     * @param type
     * @return
     */
    public FollowingGroup getByType(String type){
        return followingGroupDao.getByType(type);
    }

    /**
     * 通过id查询分组信息
     * @param id
     * @return
     */
    public FollowingGroup getById(Long id){
        return followingGroupDao.getById(id);
    }

    /**
     * 查询所有关注用户
     * @param userId
     * @return
     */
    public List<FollowingGroup> getByUserId(Long userId) {
        return followingGroupDao.getByUserId(userId);
    }

    /**
     * 添加用户关注分组
     * @param followingGroup
     */
    public void addFollowingGroup(FollowingGroup followingGroup) {
        followingGroupDao.addFollowingGroup(followingGroup);
    }

    /**
     * 查询用户关注分组列表
     * @param userId
     * @return
     */
    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupDao.getUserFollowingGroups(userId);
    }
}
