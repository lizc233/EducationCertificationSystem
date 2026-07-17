package com.educationcertificationsystem.model.vo.eval;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EvalDashboardCourseTargetDetailVO {

    private Long resultId;

    private String majorName;

    private String programVersionName;

    private String semesterName;

    private String className;

    private String courseName;

    private String taskCode;

    private String objectiveCode;

    private String objectiveName;

    private String modelName;

    private BigDecimal attainmentRate;

    private BigDecimal attainmentValue;

    private BigDecimal targetValue;

    private String resultLevel;

    private Integer lockedFlag;

    private LocalDateTime calcTime;

    private String remark;
}
