package com.daVinci.bilibili.domain.exception;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.domin.exception
 * @Author: daVinci
 * @CreateTime: 2025-01-20  21:45
 * @Description: 自定义一个异常类
 * @Version: 1.0
 */
public class ConditionException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private String code;

    public ConditionException(String code,String name){
        super(name);
        this.code = code;
    }

    public ConditionException(String name){
        super(name);
        this.code = "500";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
