package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 问卷答案表
 * @TableName survey_response_answer
 */
@TableName(value ="survey_response_answer")
@Data
public class SurveyResponseAnswer {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long responseId;

    /**
     * 
     */
    private Long questionId;

    /**
     * 
     */
    private Long optionId;

    /**
     * 
     */
    private Long rowId;

    /**
     * 
     */
    private Long columnId;

    /**
     * 
     */
    private String answerText;

    /**
     * 
     */
    private BigDecimal answerNumber;

    /**
     * 
     */
    private Object answerJson;

    /**
     * 
     */
    private BigDecimal scoreValue;

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