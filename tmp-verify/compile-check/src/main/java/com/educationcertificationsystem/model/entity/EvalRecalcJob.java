package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 重算任务表
 * @TableName eval_recalc_job
 */
@TableName(value ="eval_recalc_job")
@Data
public class EvalRecalcJob {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String jobNo;

    /**
     * 
     */
    private String jobType;

    /**
     * 
     */
    private String relationType;

    /**
     * 
     */
    private Long relationId;

    /**
     * 
     */
    private String status;

    /**
     * 
     */
    private Integer retryCount;

    /**
     * 
     */
    private String errorMessage;

    /**
     * 
     */
    private LocalDateTime queuedAt;

    /**
     * 
     */
    private LocalDateTime startedAt;

    /**
     * 
     */
    private LocalDateTime finishedAt;

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