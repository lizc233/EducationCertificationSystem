package com.educationcertificationsystem.model.dto.survey;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SurveyQuestionOptionRequest {

    private String optionCode;

    private String optionText;

    private String optionValue;

    private BigDecimal optionScore;

    private Integer isOther;

    private Integer sortNo;

    private String remark;
}
