package com.daVinci.bilibili.api.aspect;

import com.daVinci.bilibili.api.support.UserSupport;
import com.daVinci.bilibili.domain.annotation.ApiLimitedRole;
import com.daVinci.bilibili.domain.auth.UserRole;
import com.daVinci.bilibili.domain.exception.ConditionException;
import com.daVinci.bilibili.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.api.aspect
 * @Author: daVinci
 * @CreateTime: 2025-01-23  10:37
 * @Description: 接口权限控制的切面
 * @Version: 1.0
 */

@Order(1)
@Component
@Aspect
public class ApiLimitedRoleAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    // 定义切点
    @Pointcut("@annotation(com.daVinci.bilibili.domain.annotation.ApiLimitedRole)") // 有这个注解标识的方法都是切点
    public void check(){}

    /**
     * 连接点调用前执行的方法，查询用户的角色权限，与受限制的角色权限求交集，若交集不为空，则返回异常
     * @param joinPoint
     * @param apiLimitedRole
     */
    @Before("check() && @annotation(apiLimitedRole)")
    public void deBefore(JoinPoint joinPoint, ApiLimitedRole apiLimitedRole){ // 通过apiLimitedRole直接获取注解里的值
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        String[] limitedRoleCodeList = apiLimitedRole.limitedRoleCodeList();
        Set<String> limitedRoleCodeSet = Arrays.stream(limitedRoleCodeList).collect(Collectors.toSet());
        Set<String> roleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        roleCodeSet.retainAll(limitedRoleCodeSet);
        if (roleCodeSet.size() > 0){
            throw new ConditionException("权限不足!");
        }
    }
}
