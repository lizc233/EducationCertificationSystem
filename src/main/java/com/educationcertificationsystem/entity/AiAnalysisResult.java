package com.educationcertificationsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * AI分析结果表
 * @TableName ai_analysis_result
 */
@TableName(value ="ai_analysis_result")
@Data
public class AiAnalysisResult {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long requestId;

    /**
     * 
     */
    private String resultType;

    /**
     * 
     */
    private String resultText;

    /**
     * 
     */
    private Object resultJson;

    /**
     * 
     */
    private BigDecimal confidenceScore;

    /**
     * 
     */
    private Integer humanConfirmedFlag;

    /**
     * 
     */
    private Long confirmedBy;

    /**
     * 
     */
    private LocalDateTime confirmedAt;

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