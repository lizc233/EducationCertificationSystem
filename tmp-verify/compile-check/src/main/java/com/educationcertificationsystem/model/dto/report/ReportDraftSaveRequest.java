package com.educationcertificationsystem.model.dto.report;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ReportDraftSaveRequest {

    private Long editedBy;

    private String draftContent;

    private String chapterStatus;

    private BigDecimal progressPercent;

    private String comment;

    private String remark;
}
