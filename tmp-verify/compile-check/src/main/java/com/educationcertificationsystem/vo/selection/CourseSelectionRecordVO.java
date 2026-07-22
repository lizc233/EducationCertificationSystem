package com.educationcertificationsystem.vo.selection;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CourseSelectionRecordVO {

    private Long id;

    private String studentNo;

    private String studentName;

    private String className;

    private LocalDateTime selectedAt;

    private String status;

    private String statusLabel;
}
