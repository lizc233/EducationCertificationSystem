package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 通知推送日志表
 * @TableName notice_push_log
 */
@TableName(value ="notice_push_log")
@Data
public class NoticePushLog {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long noticeId;

    /**
     * 
     */
    private String mqTopic;

    /**
     * 
     */
    private String mqKey;

    /**
     * 
     */
    private Integer retryCount;

    /**
     * 
     */
    private String sendStatus;

    /**
     * 
     */
    private String errorMessage;

    /**
     * 
     */
    private LocalDateTime sentAt;

    /**
     * 
     */
    private LocalDateTime ackedAt;

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