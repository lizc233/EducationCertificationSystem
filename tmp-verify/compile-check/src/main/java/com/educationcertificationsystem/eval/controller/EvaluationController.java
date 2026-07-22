package com.educationcertificationsystem.eval.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.course.service.CourseScoreBatchService;
import com.educationcertificationsystem.course.service.CourseScoreDetailService;
import com.educationcertificationsystem.course.service.EduCourseAssessmentMethodService;
import com.educationcertificationsystem.course.service.EduCourseObjectiveService;
import com.educationcertificationsystem.course.service.EduCourseService;
import com.educationcertificationsystem.course.service.EduSemesterService;
import com.educationcertificationsystem.course.service.EduStudentService;
import com.educationcertificationsystem.course.service.TeachingTaskService;
import com.educationcertificationsystem.model.dto.LookupOption;
import com.educationcertificationsystem.model.entity.CourseScoreBatch;
import com.educationcertificationsystem.model.entity.CourseScoreDetail;
import com.educationcertificationsystem.model.entity.EduCourse;
import com.educationcertificationsystem.model.entity.EduCourseAssessmentMethod;
import com.educationcertificationsystem.model.entity.EduCourseObjective;
import com.educationcertificationsystem.model.entity.EduSemester;
import com.educationcertificationsystem.model.entity.EduStudent;
import com.educationcertificationsystem.model.entity.OrgClass;
import com.educationcertificationsystem.model.entity.SysUser;
import com.educationcertificationsystem.model.entity.TeachingTask;
import com.educationcertificationsystem.model.vo.eval.ScoreTaskBatchVO;
import com.educationcertificationsystem.model.vo.eval.ScoreTaskDetailVO;
import com.educationcertificationsystem.model.vo.eval.ScoreTaskStudentVO;
import com.educationcertificationsystem.model.vo.eval.ScoreTaskWorkspaceVO;
import com.educationcertificationsystem.org.service.OrgClassService;
import com.educationcertificationsystem.support.CsvExportSupport;
import com.educationcertificationsystem.support.EntityAuditSupport;
import com.educationcertificationsystem.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class EvaluationController {

    private final CourseScoreBatchService batchService;
    private final CourseScoreDetailService detailService;
    private final TeachingTaskService teachingTaskService;
    private final EduCourseObjectiveService courseObjectiveService;
    private final EduCourseAssessmentMethodService assessmentMethodService;
    private final EduStudentService studentService;
    private final EduCourseService courseService;
    private final EduSemesterService semesterService;
    private final OrgClassService orgClassService;
    private final SysUserService sysUserService;

    @PostMapping("/tasks/{taskId}/initialize")
    @Transactional
    public Result<ScoreTaskWorkspaceVO> initializeTaskScores(@PathVariable Long taskId) {
        TeachingTask task = requireTask(taskId);
        initializeTaskWorkspace(task);
        return Result.success(buildTaskWorkspace(task));
    }

    @GetMapping("/tasks/{taskId}/workspace")
    public Result<ScoreTaskWorkspaceVO> taskWorkspace(@PathVariable Long taskId) {
        return Result.success(buildTaskWorkspace(requireTask(taskId)));
    }

    @GetMapping("/batches")
    public Result<Page<CourseScoreBatch>> batches(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "10") long size,
                                                  @RequestParam(required = false) Long taskId,
                                                  @RequestParam(required = false) Long objectiveId,
                                                  @RequestParam(required = false) Long methodId,
                                                  @RequestParam(required = false) String calcStatus,
                                                  @RequestParam(required = false) String keyword) {
        QueryWrapper<CourseScoreBatch> wrapper = buildBatchWrapper(taskId, objectiveId, methodId, calcStatus, keyword);
        wrapper.orderByDesc("id");
        return Result.success(batchService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/batches/{id}")
    public Result<CourseScoreBatch> batch(@PathVariable Long id) {
        return Result.success(batchService.getById(id));
    }

    @PostMapping("/batches")
    public Result<CourseScoreBatch> createBatch(@RequestBody CourseScoreBatch batch) {
        validateBatchNoUnique(batch.getBatchNo(), null);
        if (batch.getCalcStatus() == null || batch.getCalcStatus().isBlank()) {
            batch.setCalcStatus("PENDING");
        }
        if (batch.getLockedFlag() == null) {
            batch.setLockedFlag(0);
        }
        EntityAuditSupport.touchCreate(batch);
        batchService.save(batch);
        return Result.success(batch);
    }

    @PutMapping("/batches/{id}")
    public Result<CourseScoreBatch> updateBatch(@PathVariable Long id, @RequestBody CourseScoreBatch batch) {
        CourseScoreBatch current = batchService.getById(id);
        if (current == null) {
            return Result.error("成绩批次不存在");
        }
        if (Integer.valueOf(1).equals(current.getLockedFlag())) {
            return Result.error("成绩批次已锁定");
        }
        validateBatchNoUnique(defaultIfBlank(batch.getBatchNo(), current.getBatchNo()), current.getId());
        BeanUtil.copyProperties(batch, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted", "calculatedAt", "importedAt"));
        EntityAuditSupport.touchUpdate(current);
        batchService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/batches/{id}")
    @Transactional
    public Result<Void> deleteBatch(@PathVariable Long id) {
        CourseScoreBatch current = batchService.getById(id);
        if (current == null) {
            return Result.error("成绩批次不存在");
        }
        if (Integer.valueOf(1).equals(current.getLockedFlag())) {
            return Result.error("成绩批次已锁定");
        }
        EntityAuditSupport.touchDelete(current);
        batchService.updateById(current);
        markDeleted(detailService.list(new QueryWrapper<CourseScoreDetail>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("batch_id", id)));
        return Result.success();
    }

    @PostMapping("/batches/{id}/recalculate")
    @Transactional
    public Result<CourseScoreBatch> recalculate(@PathVariable Long id) {
        CourseScoreBatch current = batchService.getById(id);
        if (current == null) {
            return Result.error("成绩批次不存在");
        }
        if (Integer.valueOf(1).equals(current.getLockedFlag())) {
            return Result.error("成绩已锁定，无法重算");
        }
        List<CourseScoreDetail> details = detailService.list(new QueryWrapper<CourseScoreDetail>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("batch_id", id));
        for (CourseScoreDetail detail : details) {
            if (detail.getRawScore() != null) {
                if (detail.getWeightedScore() == null) {
                    detail.setWeightedScore(detail.getRawScore());
                }
                if (detail.getTotalScore() == null) {
                    detail.setTotalScore(detail.getWeightedScore());
                }
            }
            EntityAuditSupport.touchUpdate(detail);
        }
        if (!details.isEmpty()) {
            detailService.updateBatchById(details);
        }
        current.setCalcStatus("DONE");
        current.setCalculatedAt(LocalDateTime.now());
        EntityAuditSupport.touchUpdate(current);
        batchService.updateById(current);
        return Result.success(current);
    }

    @PostMapping("/batches/{id}/lock")
    @Transactional
    public Result<CourseScoreBatch> lockBatch(@PathVariable Long id) {
        CourseScoreBatch current = batchService.getById(id);
        if (current == null) {
            return Result.error("成绩批次不存在");
        }
        List<CourseScoreDetail> details = detailService.list(new QueryWrapper<CourseScoreDetail>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("batch_id", id));
        for (CourseScoreDetail detail : details) {
            detail.setLockedFlag(1);
            if (detail.getSubmitStatus() == null || detail.getSubmitStatus().isBlank()) {
                detail.setSubmitStatus("SUBMITTED");
            }
            EntityAuditSupport.touchUpdate(detail);
        }
        if (!details.isEmpty()) {
            detailService.updateBatchById(details);
        }
        current.setLockedFlag(1);
        current.setCalcStatus("LOCKED");
        EntityAuditSupport.touchUpdate(current);
        batchService.updateById(current);
        return Result.success(current);
    }

    @GetMapping("/details")
    public Result<Page<CourseScoreDetail>> details(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   @RequestParam(required = false) Long batchId,
                                                   @RequestParam(required = false) Long studentId,
                                                   @RequestParam(required = false) String submitStatus,
                                                   @RequestParam(required = false) String keyword) {
        QueryWrapper<CourseScoreDetail> wrapper = buildDetailWrapper(batchId, studentId, submitStatus, keyword);
        wrapper.orderByDesc("id");
        return Result.success(detailService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/details/export")
    public ResponseEntity<ByteArrayResource> exportDetails(@RequestParam(required = false) Long batchId,
                                                           @RequestParam(required = false) Long studentId,
                                                           @RequestParam(required = false) String submitStatus,
                                                           @RequestParam(required = false) String keyword) {
        List<CourseScoreDetail> records = detailService.list(buildDetailWrapper(batchId, studentId, submitStatus, keyword).orderByDesc("id"));
        List<List<?>> rows = new ArrayList<>();
        for (CourseScoreDetail record : records) {
            rows.add(List.of(
                    record.getId(),
                    record.getBatchId(),
                    record.getStudentId(),
                    nullable(record.getRawScore()),
                    nullable(record.getWeightedScore()),
                    nullable(record.getTotalScore()),
                    nullable(record.getSourceType()),
                    nullable(record.getSourceRefId()),
                    nullable(record.getSubmitStatus()),
                    nullable(record.getLockedFlag()),
                    nullable(record.getRemark())
            ));
        }
        return CsvExportSupport.csv("course-score-details.csv",
                List.of("id", "batchId", "studentId", "rawScore", "weightedScore", "totalScore",
                        "sourceType", "sourceRefId", "submitStatus", "lockedFlag", "remark"),
                rows);
    }

    @PostMapping("/details")
    public Result<CourseScoreDetail> createDetail(@RequestBody CourseScoreDetail detail) {
        requireUnlockedBatch(detail.getBatchId());
        validateDetailUnique(detail.getBatchId(), detail.getStudentId(), null);
        validateScore(detail.getRawScore());
        if (detail.getRawScore() == null) {
            detail.setRawScore(BigDecimal.ZERO);
        }
        normalizeScores(detail);
        if (detail.getSubmitStatus() == null || detail.getSubmitStatus().isBlank()) {
            detail.setSubmitStatus("DRAFT");
        }
        if (detail.getLockedFlag() == null) {
            detail.setLockedFlag(0);
        }
        EntityAuditSupport.touchCreate(detail);
        detailService.save(detail);
        return Result.success(detail);
    }

    @PutMapping("/details/{id}")
    public Result<CourseScoreDetail> updateDetail(@PathVariable Long id, @RequestBody CourseScoreDetail detail) {
        CourseScoreDetail current = detailService.getById(id);
        if (current == null) {
            return Result.error("成绩明细不存在");
        }
        if (Integer.valueOf(1).equals(current.getLockedFlag())) {
            return Result.error("成绩已锁定");
        }
        Long nextBatchId = detail.getBatchId() != null ? detail.getBatchId() : current.getBatchId();
        Long nextStudentId = detail.getStudentId() != null ? detail.getStudentId() : current.getStudentId();
        requireUnlockedBatch(nextBatchId);
        validateDetailUnique(nextBatchId, nextStudentId, current.getId());
        validateScore(detail.getRawScore());
        BeanUtil.copyProperties(detail, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted"));
        normalizeScores(current);
        EntityAuditSupport.touchUpdate(current);
        detailService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/details/{id}")
    public Result<Void> deleteDetail(@PathVariable Long id) {
        CourseScoreDetail current = detailService.getById(id);
        if (current == null) {
            return Result.error("成绩明细不存在");
        }
        if (Integer.valueOf(1).equals(current.getLockedFlag())) {
            return Result.error("成绩已锁定");
        }
        requireUnlockedBatch(current.getBatchId());
        EntityAuditSupport.touchDelete(current);
        detailService.updateById(current);
        return Result.success();
    }

    @PostMapping("/details/{id}/submit")
    public Result<CourseScoreDetail> submitDetail(@PathVariable Long id) {
        CourseScoreDetail current = detailService.getById(id);
        if (current == null) {
            return Result.error("成绩明细不存在");
        }
        requireUnlockedBatch(current.getBatchId());
        current.setSubmitStatus("SUBMITTED");
        current.setLockedFlag(1);
        EntityAuditSupport.touchUpdate(current);
        detailService.updateById(current);
        return Result.success(current);
    }

    @PostMapping("/details/import")
    @Transactional
    public Result<List<CourseScoreDetail>> importDetails(@RequestBody List<CourseScoreDetail> details) {
        Set<String> keys = new HashSet<>();
        for (CourseScoreDetail detail : details) {
            requireUnlockedBatch(detail.getBatchId());
            validateDetailUnique(detail.getBatchId(), detail.getStudentId(), null);
            String key = detail.getBatchId() + "_" + detail.getStudentId();
            if (!keys.add(key)) {
                throw new IllegalArgumentException("导入数据存在重复的批次和学生组合: " + key);
            }
            validateScore(detail.getRawScore());
            if (detail.getRawScore() == null) {
                detail.setRawScore(BigDecimal.ZERO);
            }
            normalizeScores(detail);
            if (detail.getSubmitStatus() == null || detail.getSubmitStatus().isBlank()) {
                detail.setSubmitStatus("DRAFT");
            }
            if (detail.getLockedFlag() == null) {
                detail.setLockedFlag(0);
            }
            EntityAuditSupport.touchCreate(detail);
        }
        detailService.saveBatch(details);
        return Result.success(details);
    }

    private QueryWrapper<CourseScoreBatch> buildBatchWrapper(Long taskId, Long objectiveId, Long methodId,
                                                             String calcStatus, String keyword) {
        QueryWrapper<CourseScoreBatch> wrapper = activeWrapper();
        if (taskId != null) {
            wrapper.eq("task_id", taskId);
        }
        if (objectiveId != null) {
            wrapper.eq("objective_id", objectiveId);
        }
        if (methodId != null) {
            wrapper.eq("method_id", methodId);
        }
        if (calcStatus != null && !calcStatus.isBlank()) {
            wrapper.eq("calc_status", calcStatus);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like("batch_no", keyword).or().like("remark", keyword));
        }
        return wrapper;
    }

    private QueryWrapper<CourseScoreDetail> buildDetailWrapper(Long batchId, Long studentId,
                                                               String submitStatus, String keyword) {
        QueryWrapper<CourseScoreDetail> wrapper = activeWrapper();
        if (batchId != null) {
            wrapper.eq("batch_id", batchId);
        }
        if (studentId != null) {
            wrapper.eq("student_id", studentId);
        }
        if (submitStatus != null && !submitStatus.isBlank()) {
            wrapper.eq("submit_status", submitStatus);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like("source_type", keyword).or().like("remark", keyword));
        }
        return wrapper;
    }

    private void validateBatchNoUnique(String batchNo, Long currentId) {
        if (batchNo == null || batchNo.isBlank()) {
            throw new IllegalArgumentException("成绩批次号不能为空");
        }
        long count = batchService.count(this.<CourseScoreBatch>activeWrapper()
                .eq("batch_no", batchNo)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("成绩批次号已存在");
        }
    }

    private void validateDetailUnique(Long batchId, Long studentId, Long currentId) {
        if (batchId == null || studentId == null) {
            throw new IllegalArgumentException("成绩明细必须关联批次和学生");
        }
        long count = detailService.count(this.<CourseScoreDetail>activeWrapper()
                .eq("batch_id", batchId)
                .eq("student_id", studentId)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("该批次下学生成绩已存在");
        }
    }

    private CourseScoreBatch requireUnlockedBatch(Long batchId) {
        if (batchId == null) {
            throw new IllegalArgumentException("成绩批次不能为空");
        }
        CourseScoreBatch batch = batchService.getById(batchId);
        if (batch == null || Integer.valueOf(1).equals(batch.getIsDeleted())) {
            throw new IllegalArgumentException("成绩批次不存在");
        }
        if (Integer.valueOf(1).equals(batch.getLockedFlag())) {
            throw new IllegalArgumentException("成绩批次已锁定");
        }
        return batch;
    }

    private void validateScore(BigDecimal rawScore) {
        if (rawScore == null) {
            return;
        }
        if (rawScore.compareTo(BigDecimal.ZERO) < 0 || rawScore.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("成绩必须在 0 到 100 之间");
        }
    }

    private void normalizeScores(CourseScoreDetail detail) {
        if (detail.getRawScore() != null && detail.getWeightedScore() == null) {
            detail.setWeightedScore(detail.getRawScore());
        }
        if (detail.getWeightedScore() != null && detail.getTotalScore() == null) {
            detail.setTotalScore(detail.getWeightedScore());
        }
    }

    private void initializeTaskWorkspace(TeachingTask task) {
        List<EduCourseObjective> objectives = listActiveObjectives(task.getCourseId());
        if (objectives.isEmpty()) {
            throw new IllegalStateException("No course objectives configured for the selected task course");
        }

        List<EduCourseAssessmentMethod> methods = listActiveMethods(task.getCourseId());
        if (methods.isEmpty()) {
            throw new IllegalStateException("No assessment methods configured for the selected task course");
        }

        List<EduStudent> students = listActiveStudents(task.getClassId());
        if (students.isEmpty()) {
            throw new IllegalStateException("No students found for the selected task class");
        }

        List<CourseScoreBatch> currentBatches = listActiveBatches(task.getId());
        Map<String, CourseScoreBatch> batchMap = currentBatches.stream()
                .collect(Collectors.toMap(
                        item -> buildBatchKey(item.getObjectiveId(), item.getMethodId()),
                        item -> item,
                        (left, right) -> left,
                        LinkedHashMap::new));

        List<CourseScoreBatch> newBatches = new ArrayList<>();
        for (EduCourseObjective objective : objectives) {
            for (EduCourseAssessmentMethod method : methods) {
                String key = buildBatchKey(objective.getId(), method.getId());
                if (batchMap.containsKey(key)) {
                    continue;
                }
                CourseScoreBatch batch = new CourseScoreBatch();
                batch.setBatchNo(generateBatchNo(task.getId(), objective.getId(), method.getId()));
                batch.setTaskId(task.getId());
                batch.setObjectiveId(objective.getId());
                batch.setMethodId(method.getId());
                batch.setCalcStatus("PENDING");
                batch.setLockedFlag(0);
                batch.setImportedAt(LocalDateTime.now());
                batch.setRemark("Initialized from teaching task");
                EntityAuditSupport.touchCreate(batch);
                newBatches.add(batch);
            }
        }
        if (!newBatches.isEmpty()) {
            batchService.saveBatch(newBatches);
        }

        List<CourseScoreBatch> allBatches = listActiveBatches(task.getId());
        List<Long> batchIds = allBatches.stream().map(CourseScoreBatch::getId).filter(Objects::nonNull).toList();
        Map<String, CourseScoreDetail> detailMap = new HashMap<>();
        if (!batchIds.isEmpty()) {
            detailService.list(new LambdaQueryWrapper<CourseScoreDetail>()
                            .eq(CourseScoreDetail::getIsDeleted, 0)
                            .in(CourseScoreDetail::getBatchId, batchIds))
                    .forEach(item -> detailMap.put(item.getBatchId() + "_" + item.getStudentId(), item));
        }

        List<CourseScoreDetail> newDetails = new ArrayList<>();
        for (CourseScoreBatch batch : allBatches) {
            for (EduStudent student : students) {
                String key = batch.getId() + "_" + student.getId();
                if (detailMap.containsKey(key)) {
                    continue;
                }
                CourseScoreDetail detail = new CourseScoreDetail();
                detail.setBatchId(batch.getId());
                detail.setStudentId(student.getId());
                detail.setRawScore(BigDecimal.ZERO);
                detail.setWeightedScore(BigDecimal.ZERO);
                detail.setTotalScore(BigDecimal.ZERO);
                detail.setSourceType("MANUAL");
                detail.setSubmitStatus("DRAFT");
                detail.setLockedFlag(0);
                EntityAuditSupport.touchCreate(detail);
                newDetails.add(detail);
            }
        }
        if (!newDetails.isEmpty()) {
            detailService.saveBatch(newDetails);
        }
    }

    private ScoreTaskWorkspaceVO buildTaskWorkspace(TeachingTask task) {
        EduCourse course = task.getCourseId() == null ? null : courseService.getById(task.getCourseId());
        EduSemester semester = task.getSemesterId() == null ? null : semesterService.getById(task.getSemesterId());
        OrgClass orgClass = task.getClassId() == null ? null : orgClassService.getById(task.getClassId());

        List<EduCourseObjective> objectives = listActiveObjectives(task.getCourseId());
        List<EduCourseAssessmentMethod> methods = listActiveMethods(task.getCourseId());
        List<EduStudent> students = listActiveStudents(task.getClassId());
        List<CourseScoreBatch> batches = listActiveBatches(task.getId());

        List<Long> batchIds = batches.stream().map(CourseScoreBatch::getId).filter(Objects::nonNull).toList();
        List<CourseScoreDetail> details = batchIds.isEmpty()
                ? List.of()
                : detailService.list(new LambdaQueryWrapper<CourseScoreDetail>()
                .eq(CourseScoreDetail::getIsDeleted, 0)
                .in(CourseScoreDetail::getBatchId, batchIds)
                .orderByAsc(CourseScoreDetail::getBatchId)
                .orderByAsc(CourseScoreDetail::getStudentId)
                .orderByAsc(CourseScoreDetail::getId));

        Map<Long, EduCourseObjective> objectiveMap = objectives.stream()
                .collect(Collectors.toMap(EduCourseObjective::getId, item -> item, (left, right) -> left));
        Map<Long, EduCourseAssessmentMethod> methodMap = methods.stream()
                .collect(Collectors.toMap(EduCourseAssessmentMethod::getId, item -> item, (left, right) -> left));
        Map<Long, EduStudent> studentMap = students.stream()
                .collect(Collectors.toMap(EduStudent::getId, item -> item, (left, right) -> left));

        List<Long> userIds = students.stream()
                .map(EduStudent::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, SysUser> userMap = userIds.isEmpty()
                ? Map.of()
                : sysUserService.listByIds(userIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(SysUser::getId, item -> item, (left, right) -> left));

        Map<Long, List<CourseScoreDetail>> detailsByBatch = details.stream()
                .collect(Collectors.groupingBy(CourseScoreDetail::getBatchId, LinkedHashMap::new, Collectors.toList()));

        ScoreTaskWorkspaceVO workspace = new ScoreTaskWorkspaceVO();
        workspace.setTaskId(task.getId());
        workspace.setTaskCode(task.getTaskCode());
        workspace.setSemesterId(task.getSemesterId());
        workspace.setSemesterName(semester == null ? null : defaultIfBlank(semester.getSemesterName(), semester.getSemesterCode()));
        workspace.setCourseId(task.getCourseId());
        workspace.setCourseName(course == null ? null : defaultIfBlank(course.getCourseName(), course.getCourseCode()));
        workspace.setClassId(task.getClassId());
        workspace.setClassName(orgClass == null ? null : defaultIfBlank(orgClass.getClassName(), orgClass.getClassCode()));
        workspace.setTeacherId(task.getTeacherId());
        workspace.setObjectiveCount(objectives.size());
        workspace.setMethodCount(methods.size());
        workspace.setStudentCount(students.size());
        workspace.setBatchCount(batches.size());
        workspace.setObjectives(objectives.stream()
                .map(item -> new LookupOption(
                        item.getId(),
                        joinLabel(item.getObjectiveCode(), item.getObjectiveName()),
                        item.getCourseId()))
                .toList());
        workspace.setMethods(methods.stream()
                .map(item -> new LookupOption(
                        item.getId(),
                        joinLabel(item.getMethodCode(), item.getMethodName()),
                        item.getCourseId()))
                .toList());
        workspace.setStudents(students.stream()
                .sorted(Comparator.comparing(item -> defaultIfBlank(item.getStudentNo(), String.valueOf(item.getId()))))
                .map(item -> toStudentVO(item, userMap, workspace.getClassName()))
                .toList());
        workspace.setBatches(batches.stream()
                .map(batch -> toBatchVO(batch, objectiveMap, methodMap, studentMap, userMap,
                        detailsByBatch.getOrDefault(batch.getId(), List.of())))
                .sorted(Comparator.comparing((ScoreTaskBatchVO item) -> defaultIfBlank(item.getObjectiveCode(), ""))
                        .thenComparing(item -> defaultIfBlank(item.getMethodCode(), ""))
                        .thenComparing(item -> defaultIfBlank(item.getBatchNo(), "")))
                .toList());
        return workspace;
    }

    private ScoreTaskStudentVO toStudentVO(EduStudent student, Map<Long, SysUser> userMap, String className) {
        ScoreTaskStudentVO vo = new ScoreTaskStudentVO();
        vo.setStudentId(student.getId());
        vo.setStudentNo(student.getStudentNo());
        vo.setStudentName(resolveStudentName(student, userMap));
        vo.setClassId(student.getClassId());
        vo.setClassName(className);
        return vo;
    }

    private ScoreTaskBatchVO toBatchVO(CourseScoreBatch batch,
                                       Map<Long, EduCourseObjective> objectiveMap,
                                       Map<Long, EduCourseAssessmentMethod> methodMap,
                                       Map<Long, EduStudent> studentMap,
                                       Map<Long, SysUser> userMap,
                                       List<CourseScoreDetail> details) {
        EduCourseObjective objective = batch.getObjectiveId() == null ? null : objectiveMap.get(batch.getObjectiveId());
        EduCourseAssessmentMethod method = batch.getMethodId() == null ? null : methodMap.get(batch.getMethodId());

        ScoreTaskBatchVO vo = new ScoreTaskBatchVO();
        vo.setId(batch.getId());
        vo.setBatchNo(batch.getBatchNo());
        vo.setTaskId(batch.getTaskId());
        vo.setObjectiveId(batch.getObjectiveId());
        vo.setObjectiveCode(objective == null ? null : objective.getObjectiveCode());
        vo.setObjectiveName(objective == null ? null : objective.getObjectiveName());
        vo.setMethodId(batch.getMethodId());
        vo.setMethodCode(method == null ? null : method.getMethodCode());
        vo.setMethodName(method == null ? null : method.getMethodName());
        vo.setCalcStatus(batch.getCalcStatus());
        vo.setLockedFlag(batch.getLockedFlag());
        vo.setRemark(batch.getRemark());
        vo.setCalculatedAt(batch.getCalculatedAt());
        vo.setDetailCount(details.size());
        vo.setSubmittedCount((int) details.stream()
                .filter(item -> "SUBMITTED".equals(item.getSubmitStatus()) || Integer.valueOf(1).equals(item.getLockedFlag()))
                .count());
        vo.setDetails(details.stream()
                .map(item -> toDetailVO(item, studentMap, userMap))
                .sorted(Comparator.comparing(detail -> defaultIfBlank(detail.getStudentNo(), "")))
                .toList());
        return vo;
    }

    private ScoreTaskDetailVO toDetailVO(CourseScoreDetail detail,
                                         Map<Long, EduStudent> studentMap,
                                         Map<Long, SysUser> userMap) {
        EduStudent student = detail.getStudentId() == null ? null : studentMap.get(detail.getStudentId());

        ScoreTaskDetailVO vo = new ScoreTaskDetailVO();
        vo.setId(detail.getId());
        vo.setBatchId(detail.getBatchId());
        vo.setStudentId(detail.getStudentId());
        vo.setStudentNo(student == null ? null : student.getStudentNo());
        vo.setStudentName(student == null ? null : resolveStudentName(student, userMap));
        vo.setRawScore(detail.getRawScore());
        vo.setWeightedScore(detail.getWeightedScore());
        vo.setTotalScore(detail.getTotalScore());
        vo.setSubmitStatus(detail.getSubmitStatus());
        vo.setLockedFlag(detail.getLockedFlag());
        vo.setRemark(detail.getRemark());
        return vo;
    }

    private String resolveStudentName(EduStudent student, Map<Long, SysUser> userMap) {
        if (student == null || student.getUserId() == null) {
            return null;
        }
        SysUser user = userMap.get(student.getUserId());
        if (user == null) {
            return null;
        }
        return defaultIfBlank(user.getRealName(), user.getUsername());
    }

    private TeachingTask requireTask(Long taskId) {
        if (taskId == null) {
            throw new IllegalArgumentException("Task id cannot be null");
        }
        TeachingTask task = teachingTaskService.getById(taskId);
        if (task == null || Integer.valueOf(1).equals(task.getIsDeleted())) {
            throw new IllegalArgumentException("Task not found");
        }
        return task;
    }

    private List<EduCourseObjective> listActiveObjectives(Long courseId) {
        return courseObjectiveService.list(new LambdaQueryWrapper<EduCourseObjective>()
                .eq(EduCourseObjective::getIsDeleted, 0)
                .eq(EduCourseObjective::getCourseId, courseId)
                .orderByAsc(EduCourseObjective::getSortNo)
                .orderByAsc(EduCourseObjective::getId));
    }

    private List<EduCourseAssessmentMethod> listActiveMethods(Long courseId) {
        return assessmentMethodService.list(new LambdaQueryWrapper<EduCourseAssessmentMethod>()
                .eq(EduCourseAssessmentMethod::getIsDeleted, 0)
                .eq(EduCourseAssessmentMethod::getCourseId, courseId)
                .orderByAsc(EduCourseAssessmentMethod::getId));
    }

    private List<EduStudent> listActiveStudents(Long classId) {
        return studentService.list(new LambdaQueryWrapper<EduStudent>()
                .eq(EduStudent::getIsDeleted, 0)
                .eq(EduStudent::getClassId, classId)
                .orderByAsc(EduStudent::getStudentNo)
                .orderByAsc(EduStudent::getId));
    }

    private List<CourseScoreBatch> listActiveBatches(Long taskId) {
        return batchService.list(new LambdaQueryWrapper<CourseScoreBatch>()
                .eq(CourseScoreBatch::getIsDeleted, 0)
                .eq(CourseScoreBatch::getTaskId, taskId)
                .orderByAsc(CourseScoreBatch::getObjectiveId)
                .orderByAsc(CourseScoreBatch::getMethodId)
                .orderByAsc(CourseScoreBatch::getId));
    }

    private String buildBatchKey(Long objectiveId, Long methodId) {
        return objectiveId + "_" + methodId;
    }

    private String generateBatchNo(Long taskId, Long objectiveId, Long methodId) {
        return "TASK-" + taskId + "-OBJ-" + objectiveId + "-METHOD-" + methodId;
    }

    private String joinLabel(String code, String name) {
        if (code != null && !code.isBlank() && name != null && !name.isBlank()) {
            return code + " - " + name;
        }
        return defaultIfBlank(name, code);
    }

    private <T> QueryWrapper<T> activeWrapper() {
        return new QueryWrapper<T>().and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"));
    }

    private void markDeleted(List<CourseScoreDetail> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (CourseScoreDetail record : records) {
            EntityAuditSupport.touchDelete(record);
        }
        detailService.updateBatchById(records);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value != null && !value.isBlank() ? value : defaultValue;
    }

    private Object nullable(Object value) {
        return Objects.requireNonNullElse(value, "");
    }
}
