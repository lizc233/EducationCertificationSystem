package com.educationcertificationsystem.model.dto.survey;

import lombok.Data;

@Data
public class SurveyQuestionMatrixColumnRequest {

    private String colCode;

    private String colText;

    private String colValue;

    private Integer sortNo;

    private String remark;
}
