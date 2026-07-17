package com.educationcertificationsystem.model.vo.ai;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AiAnalysisHistoryVO {

    private Long requestId;

    private String requestNo;

    private String scenarioType;

    private String sourceType;

    private Long sourceId;

    private Long requesterUserId;

    private String requestStatus;

    private Integer retryCount;

    private String modelName;

    private String resultType;

    private Integer humanConfirmedFlag;

    private LocalDateTime requestedAt;

    private LocalDateTime finishedAt;
}
