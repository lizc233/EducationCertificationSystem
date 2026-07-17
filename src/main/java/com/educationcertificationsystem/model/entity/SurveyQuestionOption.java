package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 问卷选项表
 * @TableName survey_question_option
 */
@TableName(value ="survey_question_option")
@Data
public class SurveyQuestionOption {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long questionId;

    /**
     * 
     */
    private String optionCode;

    /**
     * 
     */
    private String optionText;

    /**
     * 
     */
    private String optionValue;

    /**
     * 
     */
    private BigDecimal optionScore;

    /**
     * 
     */
    private Integer isOther;

    /**
     * 
     */
    private Integer sortNo;

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