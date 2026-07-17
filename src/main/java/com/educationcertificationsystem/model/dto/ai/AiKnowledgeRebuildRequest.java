package com.educationcertificationsystem.model.dto.ai;

import lombok.Data;

@Data
public class AiKnowledgeRebuildRequest {

    private String rebuildType;

    private Long projectId;

    private Long planId;
}
