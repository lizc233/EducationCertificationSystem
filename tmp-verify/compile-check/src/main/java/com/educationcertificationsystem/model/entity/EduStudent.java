package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 学生表
 * @TableName edu_student
 */
@TableName(value ="edu_student")
@Data
public class EduStudent {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long userId;

    /**
     * 
     */
    private String studentNo;

    /**
     * 
     */
    private Long classId;

    /**
     * 
     */
    private Integer admissionYear;

    /**
     * 
     */
    private String gender;

    /**
     * 
     */
    private Integer status;

    /**
     * 
     */
    private Integer graduationStatus;

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