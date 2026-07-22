package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 课程资源表
 * @TableName course_resource
 */
@TableName(value ="course_resource")
@Data
public class CourseResource {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long courseId;

    /**
     * 
     */
    private Long taskId;

    /**
     * 
     */
    private String resourceType;

    /**
     * 
     */
    private String resourceName;

    /**
     * 
     */
    private Long fileId;

    /**
     * 
     */
    private String resourceDesc;

    /**
     * 
     */
    private String visibleScopeType;

    /**
     * 
     */
    private Long visibleScopeId;

    /**
     * 
     */
    private Integer publishStatus;

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