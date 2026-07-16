package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 专业表
 * @TableName org_major
 */
@TableName(value ="org_major")
@Data
public class OrgMajor {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long collegeId;

    /**
     * 
     */
    private String majorCode;

    /**
     * 
     */
    private String majorName;

    /**
     * 
     */
    private String degreeType;

    /**
     * 
     */
    private Integer sortNo;

    /**
     * 
     */
    private Integer status;

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