package com.educationcertificationsystem.dto.org;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassSaveRequest {

    @NotNull(message = "所属专业不能为空")
    private Long majorId;

    @NotNull(message = "所属年级不能为空")
    private Long gradeId;

    private String classCode;

    @NotBlank(message = "班级名称不能为空")
    private String className;

    private Long headTeacherId;

    private Integer studentCount;

    private Integer status;

    private String remark;
}
