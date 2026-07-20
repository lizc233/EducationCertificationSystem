package com.educationcertificationsystem.vo.org;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrgMajorVO {

    private Long id;

    private Long collegeId;

    private String collegeName;

    private String majorCode;

    private String majorName;

    private String degreeType;

    private Integer sortNo;

    private Integer status;

    private LocalDateTime createdAt;

    private String remark;
}
