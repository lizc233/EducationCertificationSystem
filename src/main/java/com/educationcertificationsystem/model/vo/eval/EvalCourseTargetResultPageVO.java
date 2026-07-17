package com.educationcertificationsystem.model.vo.eval;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EvalCourseTargetResultPageVO {

    private Long id;

    private Long taskId;

    private String taskCode;

    private Long courseId;

    private String courseName;

    private Long semesterId;

    private String semesterName;

    private Long classId;

    private String className;

    private Long objectiveId;

    private String objectiveCode;

    private String objectiveName;

    private Long modelId;

    private String modelName;

    private BigDecimal attainmentRate;

    private BigDecimal attainmentValue;

    private BigDecimal targetValue;

    private String resultLevel;

    private LocalDateTime calcTime;

    private Integer recalculationCount;

    private Integer lockedFlag;

    private String remark;
}
