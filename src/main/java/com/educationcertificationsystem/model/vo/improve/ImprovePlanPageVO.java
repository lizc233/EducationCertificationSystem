package com.educationcertificationsystem.model.vo.improve;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ImprovePlanPageVO {

    private Long id;

    private String planCode;

    private String planName;

    private String sourceType;

    private Long sourceId;

    private String targetType;

    private Long targetId;

    private Long ownerUserId;

    private String ownerUserName;

    private LocalDate startDate;

    private LocalDate dueDate;

    private String status;

    private Integer priority;

    private String effectReview;

    private LocalDateTime closedAt;

    private Integer actionCount;

    private Integer completedActionCount;

    private Integer overdueFlag;

    private String remark;
}
