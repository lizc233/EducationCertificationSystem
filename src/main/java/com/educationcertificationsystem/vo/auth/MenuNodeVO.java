package com.educationcertificationsystem.vo.auth;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class MenuNodeVO {

    private Long id;

    private Long parentId;

    private String menuType;

    private String menuName;

    private String routePath;

    private String permissionCode;

    private Integer visible;

    private Integer sortNo;

    private List<MenuNodeVO> children = new ArrayList<>();
}
