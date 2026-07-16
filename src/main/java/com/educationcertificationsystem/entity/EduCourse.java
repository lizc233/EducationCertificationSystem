package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 课程表
 * @TableName edu_course
 */
@TableName(value ="edu_course")
@Data
public class EduCourse {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String courseCode;

    /**
     * 
     */
    private String courseName;

    /**
     * 
     */
    private String courseType;

    /**
     * 
     */
    private BigDecimal credit;

    /**
     * 
     */
    private Integer totalHours;

    /**
     * 
     */
    private Integer theoryHours;

    /**
     * 
     */
    private Integer practiceHours;

    /**
     * 
     */
    private Long offeringUnitId;

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