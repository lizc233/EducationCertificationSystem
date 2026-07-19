package com.educationcertificationsystem.dto.org;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MajorSaveRequest {

    @NotNull(message = "所属院系不能为空")
    private Long collegeId;

    private String majorCode;

    @NotBlank(message = "专业名称不能为空")
    private String majorName;

    @NotBlank(message = "学历层次不能为空")
    private String degreeType;

    private Integer sortNo;

    private Integer status;

    private String remark;
}
