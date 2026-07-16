package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 毕业要求支撑指标点表
 * @TableName tr_requirement_indicator_support
 */
@TableName(value ="tr_requirement_indicator_support")
@Data
public class TrRequirementIndicatorSupport {
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
    private Long indicatorPointId;

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