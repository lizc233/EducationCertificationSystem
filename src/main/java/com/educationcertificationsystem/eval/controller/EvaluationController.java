package com.educationcertificationsystem.eval.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.model.entity.CourseScoreBatch;
import com.educationcertificationsystem.model.entity.CourseScoreDetail;
import com.educationcertificationsystem.course.service.CourseScoreBatchService;
import com.educationcertificationsystem.course.service.CourseScoreDetailService;
import com.educationcertificationsystem.support.EntityAuditSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
        wrapper.orderByDesc("id");
        return Result.success(batchService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/batches/{id}")
    public Result<CourseScoreBatch> batch(@PathVariable Long id) {
        return Result.success(batchService.getById(id));
    }

    @PostMapping("/batches")
    public Result<CourseScoreBatch> createBatch(@RequestBody CourseScoreBatch batch) {
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
        current.setCalculatedAt(java.time.LocalDateTime.now());
        EntityAuditSupport.touchUpdate(current);
        batchService.updateById(current);
        return Result.success(current);
    }

    @PostMapping("/batches/{id}/lock")
    public Result<CourseScoreBatch> lockBatch(@PathVariable Long id) {
        CourseScoreBatch current = batchService.getById(id);
        if (current == null) {
            return Result.error("成绩批次不存在");
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
        wrapper.orderByDesc("id");
        return Result.success(detailService.page(new Page<>(page, size), wrapper));
    }

    @PostMapping("/details")
    public Result<CourseScoreDetail> createDetail(@RequestBody CourseScoreDetail detail) {
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
        current.setSubmitStatus("SUBMITTED");
        current.setLockedFlag(1);
        EntityAuditSupport.touchUpdate(current);
        detailService.updateById(current);
        return Result.success(current);
    }

    @PostMapping("/details/import")
    @Transactional
    public Result<List<CourseScoreDetail>> importDetails(@RequestBody List<CourseScoreDetail> details) {
        for (CourseScoreDetail detail : details) {
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
}
