package com.educationcertificationsystem.model.vo.survey;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SurveyResponsePageVO {

    private Long id;

    private Long questionnaireId;

    private Long respondentUserId;

    private String respondentName;

    private String respondentType;

    private String responseToken;

    private String submitStatus;

    private LocalDateTime submittedAt;

    private String ipAddress;

    private Integer answerCount;

    private String remark;
}
