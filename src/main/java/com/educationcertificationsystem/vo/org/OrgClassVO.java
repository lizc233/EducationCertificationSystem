package com.educationcertificationsystem.vo.org;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrgClassVO {

    private Long id;

    private Long majorId;

    private String majorName;

    private Long gradeId;

    private String gradeName;

    private String collegeName;

    private String classCode;

    private String className;

    private Long headTeacherId;

    private String headTeacherName;

    private Integer studentCount;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String remark;
}
