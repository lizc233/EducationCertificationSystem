package com.educationcertificationsystem.vo.org;

import lombok.Data;

@Data
public class OrgTeacherBindingVO {

    private Long id;

    private Long userId;

    private String realName;

    private String teacherNo;

    private Long collegeId;

    private String collegeName;

    private Long majorId;

    private String majorName;

    private String title;

    private String jobTitle;

    private String phone;

    private String email;

    private Integer status;

    private String remark;
}
