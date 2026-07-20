package com.educationcertificationsystem.dto.system;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DictTypeSaveRequest {

    @NotBlank(message = "字典类型编码不能为空")
    private String dictType;

    @NotBlank(message = "字典类型名称不能为空")
    private String dictName;

    private Integer status;

    private String remark;
}
