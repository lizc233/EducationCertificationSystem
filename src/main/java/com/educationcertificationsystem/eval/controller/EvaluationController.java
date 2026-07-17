package com.educationcertificationsystem.eval.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.course.service.CourseScoreBatchService;
import com.educationcertificationsystem.course.service.CourseScoreDetailService;
import com.educationcertificationsystem.model.entity.CourseScoreBatch;
import com.educationcertificationsystem.model.entity.CourseScoreDetail;
import com.educationcertificationsystem.support.CsvExportSupport;
import com.educationcertificationsystem.support.EntityAuditSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class EvaluationController {

    private final CourseScoreBatchService batchService;
    private final CourseScoreDetailService detailService;

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
