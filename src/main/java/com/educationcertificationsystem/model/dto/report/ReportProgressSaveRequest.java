package com.educationcertificationsystem.model.dto.report;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ReportProgressSaveRequest {

    private Long chapterId;

    private Long userId;

    private BigDecimal progressPercent;

    private String comment;

    private String remark;
}
