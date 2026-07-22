package com.educationcertificationsystem.model.dto.survey;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SurveyQuestionnaireSaveRequest {

    private String questionnaireCode;

    private String title;

    private String subtitle;

    private String questionnaireType;

    private String targetObjectType;

    private Long targetObjectId;

    private Integer anonymousFlag;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String remark;

    private List<SurveyQuestionnaireScopeRequest> scopes;

    private List<SurveyQuestionItemRequest> questions;
}
