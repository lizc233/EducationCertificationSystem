package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 达成度明细表
 * @TableName eval_result_detail
 */
@TableName(value ="eval_result_detail")
@Data
public class EvalResultDetail {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String resultType;

    /**
     * 
     */
    private Long resultId;

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
    private BigDecimal weightPercent;

    /**
     * 
     */
    private BigDecimal sourceValue;

    /**
     * 
     */
    private BigDecimal contributionValue;

    /**
     * 
     */
    private String remark;

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
}