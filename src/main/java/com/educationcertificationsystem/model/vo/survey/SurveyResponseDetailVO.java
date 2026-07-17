package com.educationcertificationsystem.model.vo.survey;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SurveyResponseDetailVO {

    private Long id;

    private Long questionnaireId;

    private Long respondentUserId;

    private String respondentName;

    private String respondentType;

    private String responseToken;

    private String submitStatus;

    private LocalDateTime submittedAt;

    private String ipAddress;

    private String remark;

    private List<SurveyResponseQuestionAnswerVO> answers;
}
