package com.educationcertificationsystem.model.vo.eval;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ScoreTaskDetailVO {

    private Long id;

    private Long batchId;

    private Long studentId;

    private String studentNo;

    private String studentName;

    private BigDecimal rawScore;

    private BigDecimal weightedScore;

    private BigDecimal totalScore;

    private String submitStatus;

    private Integer lockedFlag;

    private String remark;
}
