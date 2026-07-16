package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 报告导出日志表
 * @TableName report_export_log
 */
@TableName(value ="report_export_log")
@Data
public class ReportExportLog {
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
    private String exportType;

    /**
     * 
     */
    private Long fileId;

    /**
     * 
     */
    private Long exportedBy;

    /**
     * 
     */
    private LocalDateTime exportedAt;

    /**
     * 
     */
    private String exportStatus;

    /**
     * 
     */
    private String errorMessage;

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