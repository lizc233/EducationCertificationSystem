package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 课程目标表
 * @TableName edu_course_objective
 */
@TableName(value ="edu_course_objective")
@Data
public class EduCourseObjective {
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
    private String objectiveCode;

    /**
     * 
     */
    private String objectiveName;

    /**
     * 
     */
    private String objectiveDesc;

    /**
     * 
     */
    private String achievementStandard;

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