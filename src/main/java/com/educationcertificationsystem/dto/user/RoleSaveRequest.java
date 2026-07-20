package com.educationcertificationsystem.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class RoleSaveRequest {

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色类型不能为空")
    private String roleType;

    @NotBlank(message = "数据权限不能为空")
    private String dataScope;

    @NotNull(message = "排序不能为空")
    private Integer sortNo;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;

    private List<Long> menuIds;
}
