package com.educationcertificationsystem.model.vo.survey;

import java.util.List;
import lombok.Data;

@Data
public class SurveyQuestionStatsVO {

    private Long questionId;

    private String questionCode;

    private String questionText;

    private String questionType;

    private Integer isRequired;

    private Long responseCount;

    private List<SurveyOptionStatsVO> optionStats;

    private List<SurveyMatrixCellStatsVO> matrixCellStats;

    private List<String> textAnswers;
}
