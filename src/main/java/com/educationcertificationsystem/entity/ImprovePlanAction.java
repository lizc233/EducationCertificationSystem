package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 改进行动表
 * @TableName improve_plan_action
 */
@TableName(value ="improve_plan_action")
@Data
public class ImprovePlanAction {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long planId;

    /**
     * 
     */
    private String actionCode;

    /**
     * 
     */
    private String actionTitle;

    /**
     * 
     */
    private String actionDesc;

    /**
     * 
     */
    private Long responsibleUserId;

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
    private BigDecimal progressPercent;

    /**
     * 
     */
    private String status;

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