package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 方案课程表
 * @TableName tr_program_course
 */
@TableName(value ="tr_program_course")
@Data
public class TrProgramCourse {
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
    private String semesterRecommend;

    /**
     * 
     */
    private String courseCategory;

    /**
     * 
     */
    private Integer isRequired;

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