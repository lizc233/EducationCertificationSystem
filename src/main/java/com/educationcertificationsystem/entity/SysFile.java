package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文件表
 * @TableName sys_file
 */
@TableName(value ="sys_file")
@Data
public class SysFile {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String bizType;

    /**
     * 
     */
    private Long bizId;

    /**
     * 
     */
    private String originalName;

    /**
     * 
     */
    private String storedName;

    /**
     * 
     */
    private String fileExt;

    /**
     * 
     */
    private Long fileSize;

    /**
     * 
     */
    private String mimeType;

    /**
     * 
     */
    private String storageType;

    /**
     * 
     */
    private String storagePath;

    /**
     * 
     */
    private String md5;

    /**
     * 
     */
    private Long uploadUserId;

    /**
     * 
     */
    private String visibilityScope;

    /**
     * 
     */
    private Integer fileStatus;

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