package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 培养方案版本表
 * @TableName tr_program_version
 */
@TableName(value ="tr_program_version")
@Data
public class TrProgramVersion {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long majorId;

    /**
     * 
     */
    private String versionNo;

    /**
     * 
     */
    private String versionName;

    /**
     * 
     */
    private LocalDate effectiveDate;

    /**
     * 
     */
    private String status;

    /**
     * 
     */
    private Long copyFromVersionId;

    /**
     * 
     */
    private LocalDateTime releasedAt;

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