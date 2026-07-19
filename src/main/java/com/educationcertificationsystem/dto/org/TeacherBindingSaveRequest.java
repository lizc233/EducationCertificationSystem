package com.educationcertificationsystem.dto.org;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeacherBindingSaveRequest {

    @NotNull(message = "教师用户不能为空")
    private Long userId;

    @NotBlank(message = "工号不能为空")
    private String teacherNo;

    @NotNull(message = "所属学院不能为空")
    private Long collegeId;

    private Long majorId;

    private String title;

    private String jobTitle;

    private String phone;

    private String email;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;
}
