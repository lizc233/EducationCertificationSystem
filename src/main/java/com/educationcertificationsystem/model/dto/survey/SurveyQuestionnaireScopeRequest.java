package com.educationcertificationsystem.model.dto.survey;

import lombok.Data;

@Data
public class SurveyQuestionnaireScopeRequest {

    private String scopeType;

    private Long scopeId;

    private String remark;
}
