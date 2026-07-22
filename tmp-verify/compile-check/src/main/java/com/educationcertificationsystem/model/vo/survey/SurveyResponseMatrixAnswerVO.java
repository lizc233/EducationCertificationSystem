package com.educationcertificationsystem.model.vo.survey;

import lombok.Data;

@Data
public class SurveyResponseMatrixAnswerVO {

    private Long rowId;

    private String rowText;

    private Long columnId;

    private String columnText;
}
