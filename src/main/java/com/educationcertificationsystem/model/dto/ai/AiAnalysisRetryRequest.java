package com.educationcertificationsystem.model.dto.ai;

import lombok.Data;

@Data
public class AiAnalysisRetryRequest {

    private Long operatorUserId;

    private String remark;
}
