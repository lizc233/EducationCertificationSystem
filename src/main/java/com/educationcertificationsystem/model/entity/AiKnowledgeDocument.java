package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@TableName(value = "ai_knowledge_document")
@Data
public class AiKnowledgeDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceType;

    private Long sourceId;

    private String title;

    private String summary;

    private String bizScope;

    private String contentChecksum;

    private String indexStatus;

    private LocalDateTime lastIndexedAt;

    private Integer versionNo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer isDeleted;

    private String remark;
}
