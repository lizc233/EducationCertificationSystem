package com.educationcertificationsystem.model.dto.improve;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ImprovePlanRecordSaveRequest {

    private String recordType;

    private String recordContent;

    private LocalDateTime recordTime;

    private Long recorderUserId;

    private Long attachmentFileId;

    private BigDecimal progressPercent;

    private String actionStatus;

    private String remark;
}
