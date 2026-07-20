package com.educationcertificationsystem.vo.system;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ParamVO {

    private Long id;

    private String group;

    private String key;

    private String value;

    private String type;

    private String desc;

    private String status;

    private LocalDateTime updatedAt;
}
