package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 考核证据材料表
 * @TableName course_evidence_material
 */
@TableName(value ="course_evidence_material")
@Data
public class CourseEvidenceMaterial {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long taskId;

    /**
     * 
     */
    private Long methodId;

    /**
     * 
     */
    private String materialType;

    /**
     * 
     */
    private Long fileId;

    /**
     * 
     */
    private Long sourceStudentId;

    /**
     * 
     */
    private String reviewStatus;

    /**
     * 
     */
    private Long reviewUserId;

    /**
     * 
     */
    private String reviewComment;

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