package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * AI分析请求表
 * @TableName ai_analysis_request
 */
@TableName(value ="ai_analysis_request")
@Data
public class AiAnalysisRequest {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String requestNo;

    /**
     * 
     */
    private String scenarioType;

    /**
     * 
     */
    private String sourceType;

    /**
     * 
     */
    private Long sourceId;

    /**
     * 
     */
    private Long templateId;

    /**
     * 
     */
    private Long requesterUserId;

    /**
     * 
     */
    private String modelName;

    /**
     * 
     */
    private String requestStatus;

    /**
     * 
     */
    private Integer retryCount;

    /**
     * 
     */
    private LocalDateTime requestedAt;

    /**
     * 
     */
    private LocalDateTime finishedAt;

    /**
     * 
     */
    private String promptSnapshot;

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