package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 章节任务分配表
 * @TableName report_task_assignment
 */
@TableName(value ="report_task_assignment")
@Data
public class ReportTaskAssignment {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long projectId;

    /**
     * 
     */
    private Long chapterId;

    /**
     * 
     */
    private Long assigneeUserId;

    /**
     * 
     */
    private String roleType;

    /**
     * 
     */
    private LocalDate dueDate;

    /**
     * 
     */
    private String assignmentStatus;

    /**
     * 
     */
    private LocalDateTime completedAt;

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