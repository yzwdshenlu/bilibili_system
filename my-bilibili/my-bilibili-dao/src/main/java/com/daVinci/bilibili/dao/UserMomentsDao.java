package com.daVinci.bilibili.dao;

import com.daVinci.bilibili.domain.UserMoment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMomentsDao {
    Integer addUserMoments(UserMoment userMoment);
}
