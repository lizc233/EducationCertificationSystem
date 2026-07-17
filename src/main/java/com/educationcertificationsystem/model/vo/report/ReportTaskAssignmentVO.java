package com.educationcertificationsystem.model.vo.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReportTaskAssignmentVO {

    private Long id;

    private Long projectId;

    private Long chapterId;

    private Long assigneeUserId;

    private String assigneeUserName;

    private String roleType;

    private LocalDate dueDate;

    private String assignmentStatus;

    private LocalDateTime completedAt;

    private String remark;
}
