package com.educationcertificationsystem.model.vo.survey;

import lombok.Data;

@Data
public class SurveyQuestionMatrixRowVO {

    private Long id;

    private String rowCode;

    private String rowText;

    private Integer sortNo;

    private String remark;
}
