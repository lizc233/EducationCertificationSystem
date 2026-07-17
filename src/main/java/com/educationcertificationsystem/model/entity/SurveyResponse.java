package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 问卷作答表
 * @TableName survey_response
 */
@TableName(value ="survey_response")
@Data
public class SurveyResponse {
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
    private Long respondentUserId;

    /**
     * 
     */
    private String respondentName;

    /**
     * 
     */
    private String respondentType;

    /**
     * 
     */
    private String responseToken;

    /**
     * 
     */
    private String submitStatus;

    /**
     * 
     */
    private LocalDateTime submittedAt;

    /**
     * 
     */
    private String ipAddress;

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