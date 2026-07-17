package com.educationcertificationsystem.model.dto.eval;

import java.util.List;
import lombok.Data;

@Data
public class EvalGraduationRequirementCalculateRequest {

    private Long programVersionId;

    private Long modelId;

    private List<Long> requirementIds;

    private String remark;
}
