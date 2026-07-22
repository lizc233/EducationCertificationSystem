package com.educationcertificationsystem.vo.auth;

import lombok.Data;

@Data
public class UserInfoVO {

    private Long id;

    private String accountId;

    private String realName;

    private String role;

    private String department;

    private String phone;

    private String email;

    private Integer status;
}
