package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 问卷题目表
 * @TableName survey_question
 */
@TableName(value ="survey_question")
@Data
public class SurveyQuestion {
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
    private String questionCode;

    /**
     * 
     */
    private String questionText;

    /**
     * 
     */
    private String questionType;

    /**
     * 
     */
    private Integer isRequired;

    /**
     * 
     */
    private Integer sortNo;

    /**
     * 
     */
    private Integer minSelect;

    /**
     * 
     */
    private Integer maxSelect;

    /**
     * 
     */
    private BigDecimal scoreWeight;

    /**
     * 
     */
    private String matrixType;

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