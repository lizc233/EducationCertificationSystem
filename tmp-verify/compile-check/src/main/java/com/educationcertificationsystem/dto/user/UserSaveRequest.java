package com.educationcertificationsystem.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserSaveRequest {

    @NotBlank(message = "角色不能为空")
    private String role;

    @NotBlank(message = "账号不能为空")
    private String accountId;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    private String department;

    private String phone;

    private String email;

    private Long collegeId;

    private Long majorId;

    private String title;

    private String jobTitle;

    private Long classId;

    private Integer admissionYear;

    private String gender;

    private Integer graduationStatus;

    private String positionName;
}
