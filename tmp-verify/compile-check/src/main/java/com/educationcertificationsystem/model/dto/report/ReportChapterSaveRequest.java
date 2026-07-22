package com.educationcertificationsystem.model.dto.report;

import java.util.List;
import lombok.Data;

@Data
public class ReportChapterSaveRequest {

    private Long id;

    private String chapterCode;

    private String chapterTitle;

    private String sourceType;

    private Long sourceRefId;

    private Integer sortNo;

    private String remark;

    private List<ReportChapterSaveRequest> children;
}
