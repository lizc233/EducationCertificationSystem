package com.educationcertificationsystem.vo.user;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class UserListItemVO {

    private Long id;

    private String accountId;

    private String realName;

    private String role;

    private String department;

    private String phone;

    private String email;

    private Integer status;

    private LocalDateTime createdAt;

    private List<String> loginAccounts;

    private Long collegeId;

    private Long majorId;

    private String title;

    private String jobTitle;

    private Long classId;

    private Integer admissionYear;

    private String gender;

    private Integer graduationStatus;

    private String positionName;
}
