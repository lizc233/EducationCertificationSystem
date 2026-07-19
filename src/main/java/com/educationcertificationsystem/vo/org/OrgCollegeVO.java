package com.educationcertificationsystem.vo.org;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrgCollegeVO {

    private Long id;

    private String collegeCode;

    private String collegeName;

    private Integer sortNo;

    private Integer status;

    private LocalDateTime createdAt;

    private String remark;
}
