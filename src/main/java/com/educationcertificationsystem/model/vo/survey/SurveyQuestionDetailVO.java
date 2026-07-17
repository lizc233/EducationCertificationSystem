package com.educationcertificationsystem.model.vo.survey;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class SurveyQuestionDetailVO {

    private Long id;

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

    private List<SurveyQuestionOptionVO> options;

    private List<SurveyQuestionMatrixRowVO> matrixRows;

    private List<SurveyQuestionMatrixColumnVO> matrixColumns;
}
