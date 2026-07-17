package com.educationcertificationsystem.model.dto.eval;

import lombok.Data;

@Data
public class EvalModelScopeRequest {

    private String scopeType;

    private Long scopeId;

    private String remark;
}
