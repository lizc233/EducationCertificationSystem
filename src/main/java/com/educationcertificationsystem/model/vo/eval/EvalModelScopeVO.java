package com.educationcertificationsystem.model.vo.eval;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EvalModelScopeVO {

    private Long id;

    private Long modelId;

    private String scopeType;

    private Long scopeId;

    private String scopeName;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
