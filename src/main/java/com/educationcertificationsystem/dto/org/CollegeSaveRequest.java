package com.educationcertificationsystem.dto.org;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CollegeSaveRequest {

    private String collegeCode;

    @NotBlank(message = "院系名称不能为空")
    private String collegeName;

    private Integer sortNo;

    private Integer status;

    private String remark;
}
