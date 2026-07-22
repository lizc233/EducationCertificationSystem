package com.educationcertificationsystem.vo.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoleOptionVO {

    private Long id;

    private String roleCode;

    private String roleName;
}
