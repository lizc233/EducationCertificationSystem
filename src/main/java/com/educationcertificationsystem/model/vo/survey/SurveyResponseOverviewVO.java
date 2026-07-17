package com.educationcertificationsystem.model.vo.survey;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SurveyResponseOverviewVO {

    private Long questionnaireId;

    private String title;

    private Integer anonymousFlag;

    private String publishStatus;

    private Long targetCount;

    private Long submittedCount;

    private Long pendingCount;

    private BigDecimal recoveryRate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
