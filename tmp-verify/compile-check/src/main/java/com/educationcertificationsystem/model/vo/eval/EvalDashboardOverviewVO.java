package com.educationcertificationsystem.model.vo.eval;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class EvalDashboardOverviewVO {

    private Long courseTargetResultCount;

    private Long graduationRequirementResultCount;

    private Long warningCount;

    private BigDecimal avgCourseTargetAttainmentRate;

    private BigDecimal avgGraduationRequirementAttainmentRate;

    private BigDecimal warningRate;
}
