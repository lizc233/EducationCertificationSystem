package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 班级表
 * @TableName org_class
 */
@TableName(value ="org_class")
@Data
public class OrgClass {
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
    private Long gradeId;

    /**
     * 
     */
    private String classCode;

    /**
     * 
     */
    private String className;

    /**
     * 
     */
    private Long headTeacherId;

    /**
     * 
     */
    private Integer studentCount;

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