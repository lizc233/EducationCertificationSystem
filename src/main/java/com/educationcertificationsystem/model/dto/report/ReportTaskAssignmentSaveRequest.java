package com.educationcertificationsystem.model.dto.report;

import java.time.LocalDate;
import lombok.Data;

@Data
public class ReportTaskAssignmentSaveRequest {

    private Long chapterId;

    private Long assigneeUserId;

    private String roleType;

    private LocalDate dueDate;

    private String assignmentStatus;

    private String remark;
}
