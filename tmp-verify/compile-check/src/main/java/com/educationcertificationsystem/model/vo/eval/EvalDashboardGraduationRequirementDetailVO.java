package com.educationcertificationsystem.model.vo.eval;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EvalDashboardGraduationRequirementDetailVO {

    private Long resultId;

    private String majorName;

    private String programVersionName;

    private String requirementCode;

    private String requirementName;

    private String modelName;

    private BigDecimal attainmentRate;

    private BigDecimal attainmentValue;

    private BigDecimal thresholdValue;

    private Integer warningFlag;

    private Integer lockFlag;

    private LocalDateTime calcTime;

    private String remark;
}
