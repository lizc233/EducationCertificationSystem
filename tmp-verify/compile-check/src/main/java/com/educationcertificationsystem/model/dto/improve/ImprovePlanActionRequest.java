package com.educationcertificationsystem.model.dto.improve;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ImprovePlanActionRequest {

    private String actionCode;

    private String actionTitle;

    private String actionDesc;

    private Long responsibleUserId;

    private LocalDate startDate;

    private LocalDate dueDate;

    private BigDecimal progressPercent;

    private String status;

    private Integer sortNo;

    private String remark;
}
