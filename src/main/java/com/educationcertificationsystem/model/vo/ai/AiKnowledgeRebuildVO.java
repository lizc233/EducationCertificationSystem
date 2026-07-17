package com.educationcertificationsystem.model.vo.ai;

import lombok.Data;

@Data
public class AiKnowledgeRebuildVO {

    private Integer documentCount;

    private Integer chunkCount;

    private String rebuildType;
}
