package com.educationcertificationsystem.vo.org;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrgGradeVO {

    private Long id;

    private Long majorId;

    private String majorName;

    private String collegeName;

    private Integer gradeYear;

    private Integer admissionYear;

    private Integer expectedGraduationYear;

    private Integer status;

    private LocalDateTime createdAt;

    private String remark;
}
