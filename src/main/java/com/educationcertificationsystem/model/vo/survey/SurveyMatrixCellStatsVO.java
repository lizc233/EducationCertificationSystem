package com.educationcertificationsystem.model.vo.survey;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SurveyMatrixCellStatsVO {

    private Long rowId;

    private String rowText;

    private Long columnId;

    private String columnText;

    private Long count;

    private BigDecimal rate;
}
