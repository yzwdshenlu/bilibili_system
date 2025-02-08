package com.daVinci.bilibili.service.handler;

import com.daVinci.bilibili.domain.JsonResponse;
import com.daVinci.bilibili.domain.exception.ConditionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.service.handler
 * @Author: daVinci
 * @CreateTime: 2025-01-20  21:43
 * @Description: 全局异常处理器
 * @Version: 1.0
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CommonGlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public JsonResponse<String> commonExceptionHandler(HttpServletRequest request,Exception e){
        String errorMsg = e.getMessage();
        if (e instanceof ConditionException){
            String errorCode = ((ConditionException) e).getCode();
            return new JsonResponse<>(errorCode,errorMsg);
        }else{
            return new JsonResponse<>("500",errorMsg);
        }
    }
}
