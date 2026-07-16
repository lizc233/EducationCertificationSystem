package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 问卷主表
 * @TableName survey_questionnaire
 */
@TableName(value ="survey_questionnaire")
@Data
public class SurveyQuestionnaire {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String questionnaireCode;

    /**
     * 
     */
    private String title;

    /**
     * 
     */
    private String subtitle;

    /**
     * 
     */
    private String questionnaireType;

    /**
     * 
     */
    private String targetObjectType;

    /**
     * 
     */
    private Long targetObjectId;

    /**
     * 
     */
    private Integer anonymousFlag;

    /**
     * 
     */
    private String publishStatus;

    /**
     * 
     */
    private LocalDateTime startTime;

    /**
     * 
     */
    private LocalDateTime endTime;

    /**
     * 
     */
    private String mqStatus;

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