package com.educationcertificationsystem.model.dto.survey;

import lombok.Data;

@Data
public class SurveyDispatchRequest {

    private Long operatorUserId;

    private String remark;
}
