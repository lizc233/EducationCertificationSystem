package com.educationcertificationsystem.model.dto.survey;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class SurveySubmitAnswerRequest {

    private Long questionId;

    private List<Long> optionIds;

    private String answerText;

    private BigDecimal answerNumber;

    private List<SurveyMatrixSelectionRequest> matrixSelections;
}
