package com.educationcertificationsystem.model.vo.eval;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class EvalGraduationRequirementContributionVO {

    private Long detailId;

    private String sourceType;

    private Long sourceId;

    private String sourceName;

    private BigDecimal weightPercent;

    private BigDecimal sourceValue;

    private BigDecimal contributionValue;

    private String remark;
}
