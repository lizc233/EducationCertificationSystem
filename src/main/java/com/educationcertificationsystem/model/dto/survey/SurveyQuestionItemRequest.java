package com.educationcertificationsystem.model.dto.survey;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class SurveyQuestionItemRequest {

    private String questionCode;

    private String questionText;

    private String questionType;

    private Integer isRequired;

    private Integer sortNo;

    private Integer minSelect;

    private Integer maxSelect;

    private BigDecimal scoreWeight;

    private String matrixType;

    private String remark;

    private List<SurveyQuestionOptionRequest> options;

    private List<SurveyQuestionMatrixRowRequest> matrixRows;

    private List<SurveyQuestionMatrixColumnRequest> matrixColumns;
}
