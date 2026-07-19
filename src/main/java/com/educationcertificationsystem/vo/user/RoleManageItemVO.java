package com.educationcertificationsystem.vo.user;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class RoleManageItemVO {

    private Long id;

    private String roleCode;

    private String roleName;

    private String roleType;

    private String dataScope;

    private Integer sortNo;

    private Integer status;

    private String remark;

    private Integer userCount;

    private List<Long> menuIds = new ArrayList<>();
}
