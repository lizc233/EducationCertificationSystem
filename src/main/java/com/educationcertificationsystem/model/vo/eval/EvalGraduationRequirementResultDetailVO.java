package com.educationcertificationsystem.model.vo.eval;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class EvalGraduationRequirementResultDetailVO {

    private Long id;

    private Long programVersionId;

    private String programVersionName;

    private Long majorId;

    private String majorName;

    private Long requirementId;

    private String requirementCode;

    private String requirementName;

    private Long modelId;

    private String modelCode;

    private String modelName;

    private BigDecimal attainmentRate;

    private BigDecimal attainmentValue;

    private BigDecimal thresholdValue;

    private Integer warningFlag;

    private LocalDateTime calcTime;

    private Integer lockFlag;

    private String remark;

    private List<EvalGraduationRequirementContributionVO> contributions;
}
