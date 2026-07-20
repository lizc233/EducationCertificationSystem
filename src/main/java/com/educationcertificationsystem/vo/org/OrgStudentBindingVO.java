package com.educationcertificationsystem.vo.org;

import lombok.Data;

@Data
public class OrgStudentBindingVO {

    private Long id;

    private Long userId;

    private String realName;

    private String studentNo;

    private Long classId;

    private String className;

    private Integer admissionYear;

    private String gender;

    private Integer status;

    private Integer graduationStatus;

    private String remark;
}
