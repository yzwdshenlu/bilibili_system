package com.daVinci.bilibili.api.aspect;

import com.daVinci.bilibili.api.support.UserSupport;
import com.daVinci.bilibili.domain.UserMoment;
import com.daVinci.bilibili.domain.auth.UserRole;
import com.daVinci.bilibili.domain.constant.AuthRoleConstant;
import com.daVinci.bilibili.domain.exception.ConditionException;
import com.daVinci.bilibili.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.api.aspect
 * @Author: daVinci
 * @CreateTime: 2025-01-23  10:37
 * @Description: 数据权限控制的切面
 * @Version: 1.0
 */

@Order(1)
@Component
@Aspect
public class DataLimitedAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    // 定义切点
    @Pointcut("@annotation(com.daVinci.bilibili.domain.annotation.DataLimited)") // 有这个注解标识的方法都是切点
    public void check(){}

    /**
     *
     * @param joinPoint
     *
     */
    @Before("check()")
    public void deBefore(JoinPoint joinPoint){ // 通过apiLimitedRole直接获取注解里的值
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        Set<String> roleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        Object[] args = joinPoint.getArgs();
        for (Object arg : args){
            if (arg instanceof UserMoment){
                UserMoment userMoment = (UserMoment) arg;
                String type = userMoment.getType();
                if (roleCodeSet.contains(AuthRoleConstant.ROLE_LV0) && !"0".equals(type)){ // Lv0角色发布了非视频类动态，不满足权限控制
                    throw new ConditionException("参数异常!");
                }
            }
        }
    }
}
