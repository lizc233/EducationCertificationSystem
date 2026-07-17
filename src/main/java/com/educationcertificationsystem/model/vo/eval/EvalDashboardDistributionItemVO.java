package com.educationcertificationsystem.model.vo.eval;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class EvalDashboardDistributionItemVO {

    private Long id;

    private String code;

    private String name;

    private Long resultCount;

    private BigDecimal avgAttainmentRate;

    private BigDecimal avgAttainmentValue;

    private Integer warningCount;
}
