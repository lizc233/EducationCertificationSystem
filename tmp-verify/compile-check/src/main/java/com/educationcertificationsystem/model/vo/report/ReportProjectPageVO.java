package com.educationcertificationsystem.model.vo.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReportProjectPageVO {

    private Long id;

    private String reportCode;

    private String projectName;

    private String academicYear;

    private Long semesterId;

    private String semesterName;

    private Long ownerUserId;

    private String ownerUserName;

    private String generationMode;

    private String status;

    private Integer totalChapters;

    private Integer visibleChapterCount;

    private Integer completedChapterCount;

    private BigDecimal progressPercent;

    private Integer lockedFlag;

    private LocalDateTime exportedAt;

    private String remark;
}
