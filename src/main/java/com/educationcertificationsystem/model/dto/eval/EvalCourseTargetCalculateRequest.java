package com.educationcertificationsystem.model.dto.eval;

import java.util.List;
import lombok.Data;

@Data
public class EvalCourseTargetCalculateRequest {

    private Long taskId;

    private Long modelId;

    private List<Long> objectiveIds;

    private String remark;
}
