package com.educationcertificationsystem.model.vo.survey;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class SurveyResponseQuestionAnswerVO {

    private Long questionId;

    private String questionCode;

    private String questionText;

    private String questionType;

    private String answerText;

    private BigDecimal answerNumber;

    private List<String> selectedOptionTexts;

    private List<SurveyResponseMatrixAnswerVO> matrixAnswers;
}
