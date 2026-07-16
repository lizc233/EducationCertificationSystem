package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 成绩批次表
 * @TableName course_score_batch
 */
@TableName(value ="course_score_batch")
@Data
public class CourseScoreBatch {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String batchNo;

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
    private Long methodId;

    /**
     * 
     */
    private String calcStatus;

    /**
     * 
     */
    private Integer lockedFlag;

    /**
     * 
     */
    private LocalDateTime importedAt;

    /**
     * 
     */
    private LocalDateTime calculatedAt;

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