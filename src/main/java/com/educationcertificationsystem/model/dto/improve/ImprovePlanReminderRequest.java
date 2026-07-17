package com.educationcertificationsystem.model.dto.improve;

import lombok.Data;

@Data
public class ImprovePlanReminderRequest {

    private Long operatorUserId;

    private String remark;
}
