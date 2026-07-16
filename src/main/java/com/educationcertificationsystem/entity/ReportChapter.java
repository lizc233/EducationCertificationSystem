package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 自评章节表
 * @TableName report_chapter
 */
@TableName(value ="report_chapter")
@Data
public class ReportChapter {
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
    private Long parentId;

    /**
     * 
     */
    private String chapterCode;

    /**
     * 
     */
    private String chapterTitle;

    /**
     * 
     */
    private String sourceType;

    /**
     * 
     */
    private Long sourceRefId;

    /**
     * 
     */
    private String contentText;

    /**
     * 
     */
    private String chapterStatus;

    /**
     * 
     */
    private Integer sortNo;

    /**
     * 
     */
    private Integer lockedFlag;

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