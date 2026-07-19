package com.educationcertificationsystem.model.vo.report;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class ReportChapterNodeVO {

    private Long id;

    private Long parentId;

    private String chapterCode;

    private String chapterTitle;

    private String sourceType;

    private Long sourceRefId;

    private String sourceDisplayName;

    private String contentText;

    private String chapterStatus;

    private Integer sortNo;

    private Integer lockedFlag;

    private BigDecimal progressPercent;

    private Integer editableFlag;

    private List<ReportTaskAssignmentVO> assignments;

    private List<ReportDraftVO> drafts;

    private List<ReportChapterNodeVO> children;
}
