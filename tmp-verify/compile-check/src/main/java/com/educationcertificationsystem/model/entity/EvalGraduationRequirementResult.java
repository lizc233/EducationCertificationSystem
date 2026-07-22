package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 毕业要求达成结果表
 * @TableName eval_graduation_requirement_result
 */
@TableName(value ="eval_graduation_requirement_result")
@Data
public class EvalGraduationRequirementResult {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long programVersionId;

    /**
     * 
     */
    private Long requirementId;

    /**
     * 
     */
    private Long modelId;

    /**
     * 
     */
    private BigDecimal attainmentRate;

    /**
     * 
     */
    private BigDecimal attainmentValue;

    /**
     * 
     */
    private BigDecimal thresholdValue;

    /**
     * 
     */
    private Integer warningFlag;

    /**
     * 
     */
    private LocalDateTime calcTime;

    /**
     * 
     */
    private Integer lockFlag;

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