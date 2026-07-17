package com.educationcertificationsystem.model.vo.survey;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SurveyOptionStatsVO {

    private Long optionId;

    private String optionCode;

    private String optionText;

    private String optionValue;

    private Long count;

    private BigDecimal rate;
}
