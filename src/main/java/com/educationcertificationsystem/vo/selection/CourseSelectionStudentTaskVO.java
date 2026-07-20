package com.educationcertificationsystem.vo.selection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CourseSelectionStudentTaskVO {

    private Long id;

    private String term;

    private String courseName;

    private String teacherName;

    private BigDecimal credit;

    private Integer selectedCount;

    private Integer capacity;

    private LocalDateTime selectionEndTime;

    private String selectionStatus;

    private String selectionStatusLabel;

    private Boolean selected;
}
