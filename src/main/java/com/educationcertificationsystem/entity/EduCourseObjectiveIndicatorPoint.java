package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 课程目标映射指标点表
 * @TableName edu_course_objective_indicator_point
 */
@TableName(value ="edu_course_objective_indicator_point")
@Data
public class EduCourseObjectiveIndicatorPoint {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long courseObjectiveId;

    /**
     * 
     */
    private Long indicatorPointId;

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