package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 课程考核方式表
 * @TableName edu_course_assessment_method
 */
@TableName(value ="edu_course_assessment_method")
@Data
public class EduCourseAssessmentMethod {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long courseId;

    /**
     * 
     */
    private String methodCode;

    /**
     * 
     */
    private String methodName;

    /**
     * 
     */
    private BigDecimal ratioPercent;

    /**
     * 
     */
    private String dueRule;

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