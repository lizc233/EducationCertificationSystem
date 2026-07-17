package com.educationcertificationsystem.model.dto.survey;

import java.util.List;
import lombok.Data;

@Data
public class SurveySubmitRequest {

    private Long respondentUserId;

    private String respondentName;

    private String respondentType;

    private String responseToken;

    private String ipAddress;

    private String remark;

    private List<SurveySubmitAnswerRequest> answers;
}
