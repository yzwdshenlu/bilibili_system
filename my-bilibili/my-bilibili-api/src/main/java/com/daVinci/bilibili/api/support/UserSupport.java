package com.daVinci.bilibili.api.support;

import com.daVinci.bilibili.domain.exception.ConditionException;
import com.daVinci.bilibili.service.util.TokenUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.api.support
 * @Author: daVinci
 * @CreateTime: 2025-01-21  14:25
 * @Description: 验证token
 * @Version: 1.0
 */
@Component
public class UserSupport {
    /**
     * 从请求头中获取token令牌,解析出id
     * @return
     */
    public Long getCurrentUserId(){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        String token = requestAttributes.getRequest().getHeader("token");
        Long userId = TokenUtil.verifyToken(token);
        // userId是主键自增，一定大于0
        if (userId < 0){
            throw new ConditionException("非法用户!");
        }
        return userId;
    }
}
