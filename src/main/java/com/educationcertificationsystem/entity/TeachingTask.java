package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 授课任务表
 * @TableName teaching_task
 */
@TableName(value ="teaching_task")
@Data
public class TeachingTask {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String taskCode;

    /**
     * 
     */
    private Long semesterId;

    /**
     * 
     */
    private Long courseId;

    /**
     * 
     */
    private Long classId;

    /**
     * 
     */
    private Long teacherId;

    /**
     * 
     */
    private Long programVersionId;

    /**
     * 
     */
    private String taskStatus;

    /**
     * 
     */
    private Integer totalHours;

    /**
     * 
     */
    private String scheduleDesc;

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