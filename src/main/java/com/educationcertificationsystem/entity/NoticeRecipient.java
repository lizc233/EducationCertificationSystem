package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 通知接收人表
 * @TableName notice_recipient
 */
@TableName(value ="notice_recipient")
@Data
public class NoticeRecipient {
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
    private Long recipientUserId;

    /**
     * 
     */
    private Integer readStatus;

    /**
     * 
     */
    private LocalDateTime readAt;

    /**
     * 
     */
    private Integer deletedFlag;

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