package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 改进计划表
 * @TableName improve_plan
 */
@TableName(value ="improve_plan")
@Data
public class ImprovePlan {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String planCode;

    /**
     * 
     */
    private String planName;

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
    private String targetType;

    /**
     * 
     */
    private Long targetId;

    /**
     * 
     */
    private Long ownerUserId;

    /**
     * 
     */
    private LocalDate startDate;

    /**
     * 
     */
    private LocalDate dueDate;

    /**
     * 
     */
    private String status;

    /**
     * 
     */
    private Integer priority;

    /**
     * 
     */
    private String effectReview;

    /**
     * 
     */
    private LocalDateTime closedAt;

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