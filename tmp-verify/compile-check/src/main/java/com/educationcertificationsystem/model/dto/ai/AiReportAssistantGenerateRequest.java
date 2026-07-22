package com.educationcertificationsystem.model.dto.ai;

import lombok.Data;

@Data
public class AiReportAssistantGenerateRequest {

    private Long requesterUserId;

    private String operationType;

    private String templateCode;

    private Integer useRagFlag;

    private Integer topK;

    private String remark;
}
