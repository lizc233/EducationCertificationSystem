package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 培养目标支撑毕业要求表
 * @TableName tr_target_requirement_support
 */
@TableName(value ="tr_target_requirement_support")
@Data
public class TrTargetRequirementSupport {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long programTargetId;

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