package com.educationcertificationsystem.dto.org;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GradeSaveRequest {

    @NotNull(message = "所属专业不能为空")
    private Long majorId;

    @NotNull(message = "年级不能为空")
    private Integer gradeYear;

    @NotNull(message = "入学年份不能为空")
    private Integer admissionYear;

    @NotNull(message = "预计毕业年份不能为空")
    private Integer expectedGraduationYear;

    private Integer status;

    private String remark;
}
