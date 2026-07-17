package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 操作日志表
 * @TableName sys_operation_log
 */
@TableName(value ="sys_operation_log")
@Data
public class SysOperationLog {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long operatorUserId;

    /**
     * 
     */
    private String operatorName;

    /**
     * 
     */
    private String logType;

    /**
     * 
     */
    private String moduleName;

    /**
     * 
     */
    private String bizType;

    /**
     * 
     */
    private Long bizId;

    /**
     * 
     */
    private String requestUri;

    /**
     * 
     */
    private String requestMethod;

    /**
     * 
     */
    private String requestParams;

    /**
     * 
     */
    private String responseResult;

    /**
     * 
     */
    private Integer successFlag;

    /**
     * 
     */
    private String errorMessage;

    /**
     * 
     */
    private Integer durationMs;

    /**
     * 
     */
    private String ipAddress;

    /**
     * 
     */
    private String userAgent;

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