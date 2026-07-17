package com.educationcertificationsystem.model.vo.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReportProgressLogVO {

    private Long id;

    private Long chapterId;

    private String chapterCode;

    private String chapterTitle;

    private Long userId;

    private String userName;

    private BigDecimal progressPercent;

    private String comment;

    private LocalDateTime createdAt;

    private String remark;
}
