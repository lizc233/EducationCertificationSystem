package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 矩阵行表
 * @TableName survey_question_matrix_row
 */
@TableName(value ="survey_question_matrix_row")
@Data
public class SurveyQuestionMatrixRow {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long questionId;

    /**
     * 
     */
    private String rowCode;

    /**
     * 
     */
    private String rowText;

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