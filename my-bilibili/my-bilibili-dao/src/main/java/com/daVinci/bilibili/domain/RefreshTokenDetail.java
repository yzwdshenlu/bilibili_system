package com.daVinci.bilibili.domain;

import java.util.Date;

/**
 * @BelongsProject: my-bilibili
 * @BelongsPackage: com.daVinci.bilibili.domain
 * @Author: daVinci
 * @CreateTime: 2025-01-23  15:08
 * @Description: refreshToken实体类
 * @Version: 1.0
 */
public class RefreshTokenDetail {

    private Long id;

    private String refreshToken;

    private Long userId;

    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
