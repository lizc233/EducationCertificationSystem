package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 培养目标表
 * @TableName tr_program_target
 */
@TableName(value ="tr_program_target")
@Data
public class TrProgramTarget {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long programVersionId;

    /**
     * 
     */
    private String targetCode;

    /**
     * 
     */
    private String targetName;

    /**
     * 
     */
    private String targetDesc;

    /**
     * 
     */
    private Integer sortNo;

    /**
     * 
     */
    private Integer enabled;

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