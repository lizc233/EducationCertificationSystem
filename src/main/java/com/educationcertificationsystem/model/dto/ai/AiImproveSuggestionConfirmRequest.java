package com.educationcertificationsystem.model.dto.ai;

import java.time.LocalDate;
import lombok.Data;

@Data
public class AiImproveSuggestionConfirmRequest {

    private Long requestId;

    private Long confirmedBy;

    private Long ownerUserId;

    private Long responsibleUserId;

    private String planCode;

    private String planName;

    private LocalDate startDate;

    private LocalDate dueDate;

    private String remark;
}
