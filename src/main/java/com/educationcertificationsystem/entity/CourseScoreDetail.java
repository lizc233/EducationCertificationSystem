package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 成绩明细表
 * @TableName course_score_detail
 */
@TableName(value ="course_score_detail")
@Data
public class CourseScoreDetail {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long batchId;

    /**
     * 
     */
    private Long studentId;

    /**
     * 
     */
    private BigDecimal rawScore;

    /**
     * 
     */
    private BigDecimal weightedScore;

    /**
     * 
     */
    private BigDecimal totalScore;

    /**
     * 
     */
    private String sourceType;

    /**
     * 
     */
    private Long sourceRefId;

    /**
     * 
     */
    private String submitStatus;

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