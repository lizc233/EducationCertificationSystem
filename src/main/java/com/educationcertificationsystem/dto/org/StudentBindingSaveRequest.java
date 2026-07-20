package com.educationcertificationsystem.dto.org;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StudentBindingSaveRequest {

    @NotNull(message = "学生用户不能为空")
    private Long userId;

    @NotBlank(message = "学号不能为空")
    private String studentNo;

    @NotNull(message = "所属班级不能为空")
    private Long classId;

    @NotNull(message = "入学年份不能为空")
    private Integer admissionYear;

    private String gender;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private Integer graduationStatus;

    private String remark;
}
