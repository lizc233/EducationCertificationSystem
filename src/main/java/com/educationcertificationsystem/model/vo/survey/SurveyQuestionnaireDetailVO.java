package com.educationcertificationsystem.model.vo.survey;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SurveyQuestionnaireDetailVO {

    private Long id;

    private String questionnaireCode;

    private String title;

    private String subtitle;

    private String questionnaireType;

    private String targetObjectType;

    private Long targetObjectId;

    private Integer anonymousFlag;

    private String publishStatus;

    private String mqStatus;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer questionCount;

    private Integer scopeCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String remark;

    private List<SurveyQuestionnaireScopeVO> scopes;

    private List<SurveyQuestionDetailVO> questions;
}
