package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 达成度评价模型表
 * @TableName eval_model
 */
@TableName(value ="eval_model")
@Data
public class EvalModel {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String modelCode;

    /**
     * 
     */
    private String modelName;

    /**
     * 
     */
    private String modelType;

    /**
     * 
     */
    private String scopeType;

    /**
     * 
     */
    private String formulaExpression;

    /**
     * 
     */
    private BigDecimal thresholdValue;

    /**
     * 
     */
    private Integer includeQuestionnaireFlag;

    /**
     * 
     */
    private Integer enabled;

    /**
     * 
     */
    private String status;

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