package com.educationcertificationsystem.vo.system;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OperationLogVO {

    private Long id;

    private LocalDateTime time;

    private String operator;

    private String module;

    private String type;

    private String ip;

    private String result;

    private String detail;
}
