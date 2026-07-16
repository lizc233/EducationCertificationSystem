package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 课程目标达成结果表
 * @TableName eval_course_target_result
 */
@TableName(value ="eval_course_target_result")
@Data
public class EvalCourseTargetResult {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long taskId;

    /**
     * 
     */
    private Long objectiveId;

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
    private BigDecimal targetValue;

    /**
     * 
     */
    private String resultLevel;

    /**
     * 
     */
    private LocalDateTime calcTime;

    /**
     * 
     */
    private Integer recalculationCount;

    /**
     * 
     */
    private Integer lockedFlag;

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