package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * AI提示词模板表
 * @TableName ai_prompt_template
 */
@TableName(value ="ai_prompt_template")
@Data
public class AiPromptTemplate {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String templateCode;

    /**
     * 
     */
    private String templateName;

    /**
     * 
     */
    private String scenarioType;

    /**
     * 
     */
    private String systemPrompt;

    /**
     * 
     */
    private String userPrompt;

    /**
     * 
     */
    private String inputSchemaJson;

    /**
     * 
     */
    private String outputSchemaJson;

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
