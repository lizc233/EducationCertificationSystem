package com.educationcertificationsystem.model.vo.survey;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SurveyQuestionOptionVO {

    private Long id;

    private String optionCode;

    private String optionText;

    private String optionValue;

    private BigDecimal optionScore;

    private Integer isOther;

    private Integer sortNo;

    private String remark;
}
