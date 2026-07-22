package com.educationcertificationsystem.model.vo.survey;

import lombok.Data;

@Data
public class SurveyQuestionnaireScopeVO {

    private Long id;

    private String scopeType;

    private Long scopeId;

    private String scopeName;

    private String remark;
}
