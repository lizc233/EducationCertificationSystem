package com.educationcertificationsystem.model.vo.eval;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class EvalModelDetailVO {

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

    private String remark;

    private Integer itemCount;

    private Integer scopeCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<EvalModelItemVO> items;

    private List<EvalModelScopeVO> scopes;
}
