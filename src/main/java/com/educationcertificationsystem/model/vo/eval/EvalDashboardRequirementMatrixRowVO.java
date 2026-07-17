package com.educationcertificationsystem.model.vo.eval;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EvalDashboardRequirementMatrixRowVO {

    private Long resultId;

    private Long requirementId;

    private String requirementCode;

    private String requirementName;

    private BigDecimal attainmentRate;

    private BigDecimal attainmentValue;

    private BigDecimal thresholdValue;

    private Integer warningFlag;

    private Integer lockFlag;

    private LocalDateTime calcTime;
}
