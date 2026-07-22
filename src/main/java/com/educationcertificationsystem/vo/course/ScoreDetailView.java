package com.educationcertificationsystem.vo.course;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 成绩明细展示对象（含学生学号、姓名，供 F15 按课程目标成绩管理列表使用）。
 */
@Data
public class ScoreDetailView {

    private Long id;

    private Long batchId;

    private Long studentId;

    private String studentNo;

    private String studentName;

    private String className;

    private BigDecimal rawScore;

    private BigDecimal weightedScore;

    private BigDecimal totalScore;

    private String sourceType;

    private Long sourceRefId;

    private String submitStatus;

    private Integer lockedFlag;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String remark;
}
