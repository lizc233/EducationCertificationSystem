package com.educationcertificationsystem.dto.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DictItemSaveRequest {

    @NotNull(message = "字典类型不能为空")
    private Long dictTypeId;

    @NotBlank(message = "字典标签不能为空")
    private String itemLabel;

    @NotBlank(message = "字典值不能为空")
    private String itemValue;

    private Integer itemSort;

    private Integer isDefault;

    private Integer status;

    private String remark;
}
