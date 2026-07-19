package com.educationcertificationsystem.model.dto.report;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ReportDraftUploadRequest {

    private Long editedBy;

    private String chapterStatus;

    private BigDecimal progressPercent;

    private String comment;

    private String remark;
}
