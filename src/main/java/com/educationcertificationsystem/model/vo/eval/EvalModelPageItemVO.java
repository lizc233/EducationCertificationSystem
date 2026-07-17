package com.educationcertificationsystem.model.vo.eval;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EvalModelPageItemVO {

    private Long id;

    private String modelCode;

    private String modelName;

    private String modelType;

    private String scopeType;

    private String formulaExpression;

    private BigDecimal thresholdValue;

    private Integer includeQuestionnaireFlag;

    private Integer enabled;

    private String status;

    private Integer itemCount;

    private Integer scopeCount;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
