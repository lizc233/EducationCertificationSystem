package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 课程考核标准表
 * @TableName edu_course_assessment_standard
 */
@TableName(value ="edu_course_assessment_standard")
@Data
public class EduCourseAssessmentStandard {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long methodId;

    /**
     * 
     */
    private String standardName;

    /**
     * 
     */
    private String standardDesc;

    /**
     * 
     */
    private BigDecimal scoreMin;

    /**
     * 
     */
    private BigDecimal scoreMax;

    /**
     * 
     */
    private Integer sortNo;

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