package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 课程支撑毕业要求表
 * @TableName tr_course_requirement_support
 */
@TableName(value ="tr_course_requirement_support")
@Data
public class TrCourseRequirementSupport {
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
    private Long courseId;

    /**
     * 
     */
    private Long graduationRequirementId;

    /**
     * 
     */
    private String supportLevel;

    /**
     * 
     */
    private BigDecimal supportWeight;

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