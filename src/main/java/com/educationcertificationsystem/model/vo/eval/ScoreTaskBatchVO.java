package com.educationcertificationsystem.model.vo.eval;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ScoreTaskBatchVO {

    private Long id;

    private String batchNo;

    private Long taskId;

    private Long objectiveId;

    private String objectiveCode;

    private String objectiveName;

    private Long methodId;

    private String methodCode;

    private String methodName;

    private String calcStatus;

    private Integer lockedFlag;

    private String remark;

    private LocalDateTime calculatedAt;

    private int detailCount;

    private int submittedCount;

    private List<ScoreTaskDetailVO> details = new ArrayList<>();
}
