package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 问卷发布任务表
 * @TableName survey_publish_task
 */
@TableName(value ="survey_publish_task")
@Data
public class SurveyPublishTask {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long questionnaireId;

    /**
     * 
     */
    private String publishBatchNo;

    /**
     * 
     */
    private String publishStatus;

    /**
     * 
     */
    private String mqStatus;

    /**
     * 
     */
    private Integer retryCount;

    /**
     * 
     */
    private LocalDateTime publishedAt;

    /**
     * 
     */
    private LocalDateTime revokedAt;

    /**
     * 
     */
    private String errorMessage;

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