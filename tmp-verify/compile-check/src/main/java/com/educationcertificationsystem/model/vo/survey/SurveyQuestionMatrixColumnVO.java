package com.educationcertificationsystem.model.vo.survey;

import lombok.Data;

@Data
public class SurveyQuestionMatrixColumnVO {

    private Long id;

    private String colCode;

    private String colText;

    private String colValue;

    private Integer sortNo;

    private String remark;
}
