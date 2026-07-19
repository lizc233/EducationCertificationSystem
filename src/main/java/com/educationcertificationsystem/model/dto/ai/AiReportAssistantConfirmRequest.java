package com.educationcertificationsystem.model.dto.ai;

import lombok.Data;

@Data
public class AiReportAssistantConfirmRequest {

    private Long requestId;

    private Long confirmedBy;

    private String applyMode;

    private String remark;
}
