package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 登录会话表
 * @TableName sys_login_session
 */
@TableName(value ="sys_login_session")
@Data
public class SysLoginSession {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long userId;

    /**
     * 
     */
    private String accessTokenHash;

    /**
     * 
     */
    private String refreshTokenHash;

    /**
     * 
     */
    private LocalDateTime loginTime;

    /**
     * 
     */
    private LocalDateTime expireTime;

    /**
     * 
     */
    private LocalDateTime lastActiveTime;

    /**
     * 
     */
    private LocalDateTime logoutTime;

    /**
     * 
     */
    private Integer revokedFlag;

    /**
     * 
     */
    private String loginIp;

    /**
     * 
     */
    private String clientType;

    /**
     * 
     */
    private LocalDateTime createdAt;

    /**
     * 
     */
    private LocalDateTime updatedAt;

    /**
     * 
     */
    private Integer isDeleted;

    /**
     * 
     */
    private String remark;
}