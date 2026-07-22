package com.educationcertificationsystem.vo.auth;

import java.util.List;
import lombok.Data;

@Data
public class AuthLoginResponse {

    private String token;

    private UserInfoVO userInfo;

    private List<String> permissions;

    private List<String> menuPaths;

    private List<MenuNodeVO> menus;
}
