package com.educationcertificationsystem.model.vo.eval;

import com.educationcertificationsystem.model.dto.LookupOption;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ScoreTaskWorkspaceVO {

    private Long taskId;

    private String taskCode;

    private Long semesterId;

    private String semesterName;

    private Long courseId;

    private String courseName;

    private Long classId;

    private String className;

    private Long teacherId;

    private Integer batchCount;

    private Integer studentCount;

    private Integer objectiveCount;

    private Integer methodCount;

    private List<LookupOption> objectives = new ArrayList<>();

    private List<LookupOption> methods = new ArrayList<>();

    private List<ScoreTaskStudentVO> students = new ArrayList<>();

    private List<ScoreTaskBatchVO> batches = new ArrayList<>();
}
