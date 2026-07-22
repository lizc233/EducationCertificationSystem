package com.educationcertificationsystem.model.vo.survey;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SurveyFillVO {

    private Long questionnaireId;

    private String title;

    private Integer anonymousFlag;

    private String publishStatus;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer canSubmit;

    private Integer alreadySubmitted;

    private Long submittedResponseId;

    private String submitMessage;

    private SurveyQuestionnaireDetailVO questionnaire;
}
