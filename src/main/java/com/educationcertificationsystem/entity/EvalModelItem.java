package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 模型明细项表
 * @TableName eval_model_item
 */
@TableName(value ="eval_model_item")
@Data
public class EvalModelItem {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long modelId;

    /**
     * 
     */
    private String itemCode;

    /**
     * 
     */
    private String itemName;

    /**
     * 
     */
    private String itemType;

    /**
     * 
     */
    private BigDecimal weightPercent;

    /**
     * 
     */
    private BigDecimal thresholdValue;

    /**
     * 
     */
    private String calcRule;

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