package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 报告草稿表
 * @TableName report_draft
 */
@TableName(value ="report_draft")
@Data
public class ReportDraft {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long chapterId;

    /**
     * 
     */
    private Integer versionNo;

    /**
     * 
     */
    private String draftContent;

    /**
     * 
     */
    private Long editedBy;

    /**
     * 
     */
    private LocalDateTime editedAt;

    /**
     * 
     */
    private Integer lockFlag;

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