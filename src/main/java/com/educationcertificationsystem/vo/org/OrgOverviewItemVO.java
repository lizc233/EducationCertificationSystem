package com.educationcertificationsystem.vo.org;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrgOverviewItemVO {

    private String id;

    private String name;

    private String type;

    private String director;

    private Integer students;

    private LocalDateTime updatedAt;
}
