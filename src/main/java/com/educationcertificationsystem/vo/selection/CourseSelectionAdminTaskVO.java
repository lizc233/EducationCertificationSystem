package com.educationcertificationsystem.vo.selection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CourseSelectionAdminTaskVO {

    private Long id;

    private String taskCode;

    private String term;

    private String courseName;

    private String teacherName;

    private BigDecimal credit;

    private Integer selectedCount;

    private Integer capacity;

    private LocalDateTime selectionStartTime;

    private LocalDateTime selectionEndTime;

    private String status;

    private String statusLabel;

    private String remark;
}
