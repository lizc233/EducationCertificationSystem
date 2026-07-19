package com.educationcertificationsystem.model.vo.report;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class ReportProgressBoardVO {

    private Long projectId;

    private Integer totalChapters;

    private Integer visibleChapterCount;

    private Integer completedChapterCount;

    private Integer assignmentCount;

    private Integer completedAssignmentCount;

    private Integer overdueAssignmentCount;

    private Integer lockedChapterCount;

    private BigDecimal progressPercent;

    private List<ReportProgressLogVO> latestLogs;
}
