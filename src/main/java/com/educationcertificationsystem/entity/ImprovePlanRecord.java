package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 改进记录表
 * @TableName improve_plan_record
 */
@TableName(value ="improve_plan_record")
@Data
public class ImprovePlanRecord {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long actionId;

    /**
     * 
     */
    private String recordType;

    /**
     * 
     */
    private String recordContent;

    /**
     * 
     */
    private LocalDateTime recordTime;

    /**
     * 
     */
    private Long recorderUserId;

    /**
     * 
     */
    private Long attachmentFileId;

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