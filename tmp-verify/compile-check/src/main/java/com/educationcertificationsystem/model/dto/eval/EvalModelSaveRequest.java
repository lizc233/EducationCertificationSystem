package com.educationcertificationsystem.model.dto.eval;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class EvalModelSaveRequest {

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

    private List<EvalModelItemRequest> items;

    private List<EvalModelScopeRequest> scopes;
}
