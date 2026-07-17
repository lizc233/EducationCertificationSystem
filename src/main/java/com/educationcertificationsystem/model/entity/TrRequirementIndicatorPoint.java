package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 指标点表
 * @TableName tr_requirement_indicator_point
 */
@TableName(value ="tr_requirement_indicator_point")
@Data
public class TrRequirementIndicatorPoint {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long graduationRequirementId;

    /**
     * 
     */
    private String indicatorCode;

    /**
     * 
     */
    private String indicatorName;

    /**
     * 
     */
    private String indicatorDesc;

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