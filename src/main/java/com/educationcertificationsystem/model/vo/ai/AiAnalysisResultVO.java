package com.educationcertificationsystem.model.vo.ai;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class AiAnalysisResultVO {

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

    private String resultText;

    private String resultJson;

    private Integer humanConfirmedFlag;

    private Long confirmedBy;

    private LocalDateTime confirmedAt;

    private LocalDateTime requestedAt;

    private LocalDateTime finishedAt;

    private String remark;

    private List<AiRetrievedChunkVO> retrievedChunks;
}
