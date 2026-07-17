package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 问卷投放范围表
 * @TableName survey_questionnaire_scope
 */
@TableName(value ="survey_questionnaire_scope")
@Data
public class SurveyQuestionnaireScope {
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
    private String scopeType;

    /**
     * 
     */
    private Long scopeId;

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