package com.educationcertificationsystem.model.dto.survey;

import lombok.Data;

@Data
public class SurveyQuestionMatrixRowRequest {

    private String rowCode;

    private String rowText;

    private Integer sortNo;

    private String remark;
}
