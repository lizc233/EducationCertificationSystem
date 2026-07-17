package com.educationcertificationsystem.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@TableName(value = "ai_knowledge_chunk")
@Data
public class AiKnowledgeChunk {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long documentId;

    private Integer chunkNo;

    private String chunkText;

    private String metadataJson;

    private String qdrantPointId;

    private String embeddingModel;

    private String indexStatus;

    private LocalDateTime lastIndexedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer isDeleted;

    private String remark;
}
