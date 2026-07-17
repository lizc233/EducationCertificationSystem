package com.educationcertificationsystem.model.vo.eval;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class EvalDashboardTrendPointVO {

    private Long semesterId;

    private String semesterName;

    private Long resultCount;

    private BigDecimal avgAttainmentRate;

    private BigDecimal avgAttainmentValue;
}
