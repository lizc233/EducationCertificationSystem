package com.educationcertificationsystem.model.dto.eval;

import lombok.Data;

@Data
public class EvalDashboardQueryRequest {

    private Long majorId;

    private Long gradeId;

    private Long semesterId;

    private Long programVersionId;

    private Long modelId;

    private Long courseId;

    private String keyword;

    private Long pageNum = 1L;

    private Long pageSize = 10L;
}
