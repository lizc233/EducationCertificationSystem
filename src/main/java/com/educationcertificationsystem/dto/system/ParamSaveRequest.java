package com.educationcertificationsystem.dto.system;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParamSaveRequest {

    @NotBlank(message = "参数键不能为空")
    private String paramKey;

    @NotBlank(message = "参数值不能为空")
    private String paramValue;

    @NotBlank(message = "参数类型不能为空")
    private String paramType;

    private Integer isSystem;

    private Integer status;

    private String remark;
}
