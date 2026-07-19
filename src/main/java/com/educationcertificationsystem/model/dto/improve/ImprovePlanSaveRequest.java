package com.educationcertificationsystem.model.dto.improve;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ImprovePlanSaveRequest {

    private String planCode;

    private String planName;

    private String sourceType;

    private Long sourceId;

    private String targetType;

    private Long targetId;

    private Long ownerUserId;

    private LocalDate startDate;

    private LocalDate dueDate;

    private Integer priority;

    private String remark;

    private List<ImprovePlanActionRequest> actions;
}
