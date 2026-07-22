package com.educationcertificationsystem.model.dto.report;

import java.util.List;
import lombok.Data;

@Data
public class ReportProjectSaveRequest {

    private String reportCode;

    private String projectName;

    private String academicYear;

    private Long semesterId;

    private Long ownerUserId;

    private String generationMode;

    private String remark;

    private List<ReportChapterSaveRequest> chapters;
}
