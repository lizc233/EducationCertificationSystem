package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 自评报告项目表
 * @TableName report_project
 */
@TableName(value ="report_project")
@Data
public class ReportProject {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String reportCode;

    /**
     * 
     */
    private String projectName;

    /**
     * 
     */
    private String academicYear;

    /**
     * 
     */
    private Long semesterId;

    /**
     * 
     */
    private Long ownerUserId;

    /**
     * 
     */
    private String generationMode;

    /**
     * 
     */
    private String status;

    /**
     * 
     */
    private Integer totalChapters;

    /**
     * 
     */
    private Integer lockedFlag;

    /**
     * 
     */
    private LocalDateTime exportedAt;

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