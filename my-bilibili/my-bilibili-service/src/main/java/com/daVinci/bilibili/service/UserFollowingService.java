package com.daVinci.bilibili.service;

import com.daVinci.bilibili.dao.UserFollowingDao;
import com.daVinci.bilibili.domain.FollowingGroup;
import com.daVinci.bilibili.domain.User;
import com.daVinci.bilibili.domain.UserFollowing;
import com.daVinci.bilibili.domain.UserInfo;
import com.daVinci.bilibili.domain.constant.UserConstant;
import com.daVinci.bilibili.domain.exception.ConditionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.service
 * @Author: daVinci
 * @CreateTime: 2025-01-21  15:48
 * @Description: 用户关注service
 * @Version: 1.0
 */
@Service
public class UserFollowingService {
    @Autowired
    private UserFollowingDao userFollowingDao;

    @Autowired
    private FollowingGroupService followingGroupService;

    @Autowired
    private UserService userService;


    /**
     * 新增用户关注
     * @param userFollowing
     */
    @Transactional
    public void addUserFollowings(UserFollowing userFollowing) {
        //首次关注用户需要分组，如果用户不指定分组，则添加到默认分组
        Long groupId = userFollowing.getGroupId();
        if (groupId == null) {
            // 查询默认分组的信息
            FollowingGroup followingGroup = followingGroupService.getByType(UserConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT);
            userFollowing.setGroupId(followingGroup.getId());
        } else {
            // 用户选择了分组类型
            FollowingGroup followingGroup = followingGroupService.getById(groupId);
            if (followingGroup == null){
                throw new ConditionException("关注分组不存在!");
            }
            userFollowing.setGroupId(followingGroup.getId());
        }

        // 判断关注用户是否存在
        Long followingId = userFollowing.getFollowingId();
        User user = userService.getUserById(followingId);
        if (user == null){
            throw new ConditionException("关注用户不存在!");
        }

        // 先删除原有关注的关系，再重新创建关注关系
        userFollowingDao.deleteUserFollowing(userFollowing.getUserId(),followingId);
        // 添加关注关系
        userFollowing.setCreateTime(new Date());
        userFollowingDao.addUserFollowing(userFollowing);
    }

    /**
     * 获取用户所有关注
     * @param userId
     * @return 以分组方式返回
     */
    public List<FollowingGroup> getUserFollowings(Long userId){
        // 1.获取关注的用户列表
        List<UserFollowing> list = userFollowingDao.getUserFollowings(userId); //根据id查询当前用户的所有关注
        Set<Long> followingIdSet = list.stream().map(UserFollowing::getFollowingId).collect(Collectors.toSet()); //得到所有关注用户的id
        // 2.根据关注用户的id查询关注用户的基本信息
        List<UserInfo> userInfoList = new ArrayList<>();
        if (followingIdSet.size() > 0){
            userInfoList =  userService.getUserInfoByUserIds(followingIdSet); //查询所有关注用户的信息
        }
        for(UserFollowing userFollowing : list){ // 遍历所有关注的用户
            for (UserInfo userInfo : userInfoList){ // 遍历所有关注的用户信息
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())){ // 匹配
                    userFollowing.setUserInfo(userInfo); //设置关注用户的信息
                }
            }
        }
        // 3.将关注用户按关注分组进行分类
        List<FollowingGroup> groupList = followingGroupService.getByUserId(userId); //获取默认分组以及用户自定义分组
        FollowingGroup allGroup = new FollowingGroup(); // "所有关注"不属于数据库中的关注分组，自行创建返回给前端用于页面显示
        allGroup.setName(UserConstant.USER_FOLLOWING_GROUP_ALL_NAME); // 设置分组名（这里是"所有关注"）
        allGroup.setFollowingUserInfoList(userInfoList); // 设置分组中的所有关注用户
        List<FollowingGroup> result = new ArrayList<>(); // 用于返回的结果，包含所有分组，每个分组包含对应关注用户信息
        result.add(allGroup); //将"所有关注"添加到关注分组的列表中
        // 向关注分组的列表中添加用户自定义分组以及默认分组
        for (FollowingGroup group : groupList){ // 遍历所有关注分组
            List<UserInfo> infoList = new ArrayList<>();
            for(UserFollowing userFollowing : list){ // 遍历所有关注用户
                if (group.getId().equals(userFollowing.getGroupId())){ // 当前关注分组中存在当前关注用户
                    infoList.add(userFollowing.getUserInfo()); // 添加到关注用户信息列表
                }
            }
            group.setFollowingUserInfoList(infoList); // 设置当前分组的所有关注用户
            result.add(group); // 将当前分组添加到结果中
        }
        return result; // 返回结果
    }

    /**
     * 获取用户所有粉丝
     * @param userId
     * @return 以一个集合返回
     */
    public List<UserFollowing> getUserFans(Long userId){
        // 1.获取当前用户的粉丝列表
        List<UserFollowing> fanList = userFollowingDao.getUserFans(userId);
        Set<Long> fanIdSet = fanList.stream().map(UserFollowing::getUserId).collect(Collectors.toSet()); //得到所有粉丝用户的id
        // 2.根据粉丝用户的id查询粉丝用户的基本信息
        List<UserInfo> userInfoList = new ArrayList<>();
        if (fanIdSet.size() > 0){
            userInfoList =  userService.getUserInfoByUserIds(fanIdSet); //查询所有关注用户的信息
        }
        // 3.查询当前用户是否已经关注该粉丝,单独处理互相关注的情况
        List<UserFollowing> followingList = userFollowingDao.getUserFollowings(userId); // 查询当前用户的关注用户
        for (UserFollowing fan : fanList){
            for (UserInfo userInfo : userInfoList){
                if (fan.getUserId().equals(userInfo.getUserId())){
                    userInfo.setFollowed(false); // 初始化为false
                    fan.setUserInfo(userInfo);
                }
            }
            for (UserFollowing following : followingList){
                if (following.getFollowingId().equals(fan.getUserId())){ //既是关注又是粉丝(互相关注)
                    fan.getUserInfo().setFollowed(true);
                }
            }
        }
        return fanList;
    }

    /**
     * 新建用户关注分组
     * @param followingGroup
     * @return
     */
    public Long addUserFollowingGroups(FollowingGroup followingGroup) {
        followingGroup.setCreateTime(new Date());
        followingGroup.setType(UserConstant.USER_FOLLOWING_GROUP_TYPE_USER);
        followingGroupService.addFollowingGroup(followingGroup);
        return followingGroup.getId();
    }

    /**
     * 查询用户关注分组列表
     * @param userId
     * @return
     */
    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupService.getUserFollowingGroups(userId);
    }

    public List<UserInfo> checkFollowingStatus(List<UserInfo> userInfoList, Long userId) {
        List<UserFollowing> userFollowingList = userFollowingDao.getUserFollowings(userId);
        for (UserInfo userInfo: userInfoList){
            userInfo.setFollowed(false);
            for (UserFollowing userFollowing: userFollowingList){
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())){
                    userInfo.setFollowed(true);
                }
            }
        }
        return userInfoList;
    }
}
