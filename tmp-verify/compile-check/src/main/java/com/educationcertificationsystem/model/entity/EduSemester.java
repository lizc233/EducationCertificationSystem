package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 学期表
 * @TableName edu_semester
 */
@TableName(value ="edu_semester")
@Data
public class EduSemester {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String semesterCode;

    /**
     * 
     */
    private String semesterName;

    /**
     * 
     */
    private LocalDate startDate;

    /**
     * 
     */
    private LocalDate endDate;

    /**
     * 
     */
    private Integer activeFlag;

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