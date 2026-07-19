package com.educationcertificationsystem.model.dto.ai;

import lombok.Data;

@Data
public class AiImproveSuggestionGenerateRequest {

    private Long requesterUserId;

    private Long ownerUserId;

    private Long responsibleUserId;

    private String sourceType;

    private Long sourceId;

    private String targetType;

    private Long targetId;

    private String templateCode;

    private Integer priority;

    private Integer dueDays;

    private Integer topK;

    private String remark;
}
