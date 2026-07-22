package com.educationcertificationsystem.course.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.course.mapper.CourseScoreDetailMapper;
import com.educationcertificationsystem.course.service.CourseScoreBatchService;
import com.educationcertificationsystem.course.service.CourseScoreDetailService;
import com.educationcertificationsystem.course.service.EduCourseAssessmentMethodService;
import com.educationcertificationsystem.dto.course.ScoreImportRequest;
import com.educationcertificationsystem.model.entity.CourseScoreBatch;
import com.educationcertificationsystem.model.entity.CourseScoreDetail;
import com.educationcertificationsystem.model.entity.EduCourseAssessmentMethod;
import com.educationcertificationsystem.support.CsvExportSupport;
import com.educationcertificationsystem.support.EntityAuditSupport;
import com.educationcertificationsystem.vo.course.ScoreDetailView;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final CourseScoreBatchService batchService;
    private final CourseScoreDetailService detailService;
    private final CourseScoreDetailMapper detailMapper;
    private final EduCourseAssessmentMethodService assessmentMethodService;

    @GetMapping("/batches")
    public Result<Page<CourseScoreBatch>> batches(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "10") long size,
                                                  @RequestParam(required = false) Long taskId,
                                                  @RequestParam(required = false) Long objectiveId,
                                                  @RequestParam(required = false) Long methodId,
                                                  @RequestParam(required = false) String calcStatus,
                                                  @RequestParam(required = false) String keyword) {
        QueryWrapper<CourseScoreBatch> wrapper = activeBatchWrapper();
        if (taskId != null) {
            wrapper.eq("task_id", taskId);
        }
        if (objectiveId != null) {
            wrapper.eq("objective_id", objectiveId);
        }
        if (methodId != null) {
            wrapper.eq("method_id", methodId);
        }
        if (StringUtils.hasText(calcStatus)) {
            wrapper.eq("calc_status", calcStatus);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("batch_no", keyword).or().like("remark", keyword));
        }
        wrapper.orderByDesc("id");
        return Result.success(batchService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/batches/{id}")
    public Result<CourseScoreBatch> batch(@PathVariable Long id) {
        CourseScoreBatch batch = batchService.getOne(activeBatchWrapper().eq("id", id), false);
        if (batch == null) {
            return Result.error("成绩批次不存在");
        }
        return Result.success(batch);
    }

    @PostMapping("/batches")
    public Result<CourseScoreBatch> createBatch(@RequestBody CourseScoreBatch batch) {
        try {
            validateBatchRequired(batch);
            if (!StringUtils.hasText(batch.getBatchNo())) {
                batch.setBatchNo("BATCH-" + System.currentTimeMillis());
            }
            validateBatchNoUnique(batch.getBatchNo(), null);
            batch.setCalcStatus("PENDING");
            if (batch.getLockedFlag() == null) {
                batch.setLockedFlag(0);
            }
            EntityAuditSupport.touchCreate(batch);
            batchService.save(batch);
            return Result.success(batch);
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PutMapping("/batches/{id}")
    public Result<CourseScoreBatch> updateBatch(@PathVariable Long id, @RequestBody CourseScoreBatch batch) {
        try {
            CourseScoreBatch current = requireEditableBatch(id);
            String batchNo = StringUtils.hasText(batch.getBatchNo()) ? batch.getBatchNo() : current.getBatchNo();
            validateBatchNoUnique(batchNo, current.getId());
            BeanUtil.copyProperties(batch, current, CopyOptions.create().setIgnoreNullValue(true)
                    .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted", "importedAt", "calculatedAt"));
            current.setBatchNo(batchNo);
            EntityAuditSupport.touchUpdate(current);
            batchService.updateById(current);
            return Result.success(current);
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @DeleteMapping("/batches/{id}")
    @Transactional
    public Result<Void> deleteBatch(@PathVariable Long id) {
        try {
            CourseScoreBatch batch = requireEditableBatch(id);
            EntityAuditSupport.touchDelete(batch);
            batchService.updateById(batch);
            List<CourseScoreDetail> details = detailService.list(activeDetailWrapper().eq("batch_id", id));
            markDetailsDeleted(details);
            return Result.success();
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("/batches/{id}/recalculate")
    @Transactional
    public Result<CourseScoreBatch> recalculateBatch(@PathVariable Long id) {
        try {
            CourseScoreBatch batch = requireEditableBatch(id);
            List<CourseScoreDetail> details = detailService.list(activeDetailWrapper().eq("batch_id", id));
            if (details.isEmpty()) {
                return Result.error("该批次暂无成绩明细，无法重算");
            }
            for (CourseScoreDetail detail : details) {
                applyScoreFields(detail, batch.getMethodId());
                EntityAuditSupport.touchUpdate(detail);
            }
            detailService.updateBatchById(details);
            batch.setCalcStatus("DONE");
            batch.setCalculatedAt(LocalDateTime.now());
            EntityAuditSupport.touchUpdate(batch);
            batchService.updateById(batch);
            return Result.success(batch);
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("/batches/{id}/lock")
    @Transactional
    public Result<CourseScoreBatch> lockBatch(@PathVariable Long id) {
        try {
            CourseScoreBatch batch = requireEditableBatch(id);
            List<CourseScoreDetail> details = detailService.list(activeDetailWrapper().eq("batch_id", id));
            for (CourseScoreDetail detail : details) {
                detail.setLockedFlag(1);
                if (!"SUBMITTED".equals(detail.getSubmitStatus())) {
                    detail.setSubmitStatus("SUBMITTED");
                }
                EntityAuditSupport.touchUpdate(detail);
            }
            if (!details.isEmpty()) {
                detailService.updateBatchById(details);
            }
            batch.setLockedFlag(1);
            batch.setCalcStatus("LOCKED");
            EntityAuditSupport.touchUpdate(batch);
            batchService.updateById(batch);
            return Result.success(batch);
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @GetMapping("/details")
    public Result<List<ScoreDetailView>> details(@RequestParam Long batchId) {
        if (batchId == null) {
            return Result.error("请选择成绩批次");
        }
        return Result.success(detailMapper.selectDetailViews(batchId));
    }

    @PostMapping("/details")
    @Transactional
    public Result<CourseScoreDetail> createDetail(@RequestBody CourseScoreDetail detail) {
        try {
            validateDetailRequired(detail);
            CourseScoreBatch batch = requireEditableBatch(detail.getBatchId());
            validateScoreRange(detail.getRawScore());
            validateDetailUnique(detail.getBatchId(), detail.getStudentId(), null);
            applyScoreFields(detail, batch.getMethodId());
            if (!StringUtils.hasText(detail.getSubmitStatus())) {
                detail.setSubmitStatus("DRAFT");
            }
            if (detail.getLockedFlag() == null) {
                detail.setLockedFlag(0);
            }
            EntityAuditSupport.touchCreate(detail);
            detailService.save(detail);
            markBatchPending(batch, false);
            return Result.success(detail);
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PutMapping("/details/{id}")
    @Transactional
    public Result<CourseScoreDetail> updateDetail(@PathVariable Long id, @RequestBody CourseScoreDetail detail) {
        try {
            CourseScoreDetail current = requireEditableDetail(id);
            CourseScoreBatch batch = requireEditableBatch(current.getBatchId());
            BigDecimal nextRaw = detail.getRawScore() != null ? detail.getRawScore() : current.getRawScore();
            validateScoreRange(nextRaw);
            BeanUtil.copyProperties(detail, current, CopyOptions.create().setIgnoreNullValue(true)
                    .setIgnoreProperties("id", "batchId", "studentId", "createdAt", "updatedAt", "isDeleted"));
            applyScoreFields(current, batch.getMethodId());
            EntityAuditSupport.touchUpdate(current);
            detailService.updateById(current);
            markBatchPending(batch, false);
            return Result.success(current);
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @DeleteMapping("/details/{id}")
    @Transactional
    public Result<Void> deleteDetail(@PathVariable Long id) {
        try {
            CourseScoreDetail current = requireEditableDetail(id);
            CourseScoreBatch batch = requireEditableBatch(current.getBatchId());
            EntityAuditSupport.touchDelete(current);
            detailService.updateById(current);
            markBatchPending(batch, false);
            return Result.success();
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("/details/{id}/submit")
    @Transactional
    public Result<CourseScoreDetail> submitDetail(@PathVariable Long id) {
        try {
            CourseScoreDetail current = requireEditableDetail(id);
            requireEditableBatch(current.getBatchId());
            current.setSubmitStatus("SUBMITTED");
            current.setLockedFlag(1);
            EntityAuditSupport.touchUpdate(current);
            detailService.updateById(current);
            return Result.success(current);
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PostMapping("/details/import")
    @Transactional
    public Result<ImportResult> importDetails(@RequestBody ScoreImportRequest request) {
        try {
            if (request == null || request.getBatchId() == null) {
                throw new IllegalArgumentException("请指定成绩批次");
            }
            if (request.getItems() == null || request.getItems().isEmpty()) {
                throw new IllegalArgumentException("导入数据不能为空");
            }
            CourseScoreBatch batch = requireEditableBatch(request.getBatchId());
            int inserted = 0;
            int updated = 0;
            int failed = 0;
            List<String> errors = new ArrayList<>();
            int rowNo = 0;
            for (ScoreImportRequest.Item item : request.getItems()) {
                rowNo++;
                try {
                    if (item.getStudentId() == null) {
                        throw new IllegalArgumentException("学生不能为空");
                    }
                    validateScoreRange(item.getRawScore());
                    CourseScoreDetail existing = detailService.getOne(activeDetailWrapper()
                            .eq("batch_id", request.getBatchId())
                            .eq("student_id", item.getStudentId()), false);
                    if (existing != null) {
                        if (isLocked(existing.getLockedFlag())) {
                            throw new IllegalArgumentException("成绩已锁定");
                        }
                        existing.setRawScore(item.getRawScore());
                        existing.setSourceType(item.getSourceType());
                        existing.setSourceRefId(item.getSourceRefId());
                        existing.setRemark(item.getRemark());
                        applyScoreFields(existing, batch.getMethodId());
                        EntityAuditSupport.touchUpdate(existing);
                        detailService.updateById(existing);
                        updated++;
                    } else {
                        CourseScoreDetail detail = new CourseScoreDetail();
                        detail.setBatchId(request.getBatchId());
                        detail.setStudentId(item.getStudentId());
                        detail.setRawScore(item.getRawScore());
                        detail.setSourceType(item.getSourceType());
                        detail.setSourceRefId(item.getSourceRefId());
                        detail.setRemark(item.getRemark());
                        detail.setSubmitStatus("DRAFT");
                        detail.setLockedFlag(0);
                        applyScoreFields(detail, batch.getMethodId());
                        EntityAuditSupport.touchCreate(detail);
                        detailService.save(detail);
                        inserted++;
                    }
                } catch (IllegalArgumentException ex) {
                    failed++;
                    errors.add("第" + rowNo + "行：" + ex.getMessage());
                }
            }
            markBatchPending(batch, true);
            return Result.success(new ImportResult(inserted, updated, failed, errors));
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @GetMapping("/details/export")
    public ResponseEntity<ByteArrayResource> exportDetails(@RequestParam Long batchId) {
        List<ScoreDetailView> views = detailMapper.selectDetailViews(batchId);
        List<List<?>> rows = new ArrayList<>();
        for (ScoreDetailView view : views) {
            rows.add(List.of(
                    nullable(view.getStudentNo()),
                    nullable(view.getStudentName()),
                    nullable(view.getClassName()),
                    nullable(view.getRawScore()),
                    nullable(view.getWeightedScore()),
                    nullable(view.getTotalScore()),
                    nullable(view.getSubmitStatus()),
                    nullable(view.getLockedFlag()),
                    nullable(view.getRemark())
            ));
        }
        return CsvExportSupport.csv(
                "course-score-details.csv",
                List.of("studentNo", "studentName", "className", "rawScore", "weightedScore",
                        "totalScore", "submitStatus", "lockedFlag", "remark"),
                rows
        );
    }

    private QueryWrapper<CourseScoreBatch> activeBatchWrapper() {
        return new QueryWrapper<CourseScoreBatch>().and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"));
    }

    private QueryWrapper<CourseScoreDetail> activeDetailWrapper() {
        return new QueryWrapper<CourseScoreDetail>().and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"));
    }

    private CourseScoreBatch requireEditableBatch(Long batchId) {
        CourseScoreBatch batch = batchService.getOne(activeBatchWrapper().eq("id", batchId), false);
        if (batch == null) {
            throw new IllegalArgumentException("成绩批次不存在");
        }
        if (isLocked(batch.getLockedFlag())) {
            throw new IllegalArgumentException("成绩批次已锁定，无法修改");
        }
        return batch;
    }

    private CourseScoreDetail requireEditableDetail(Long detailId) {
        CourseScoreDetail detail = detailService.getOne(activeDetailWrapper().eq("id", detailId), false);
        if (detail == null) {
            throw new IllegalArgumentException("成绩明细不存在");
        }
        if (isLocked(detail.getLockedFlag())) {
            throw new IllegalArgumentException("成绩已锁定，无法修改");
        }
        if ("SUBMITTED".equals(detail.getSubmitStatus())) {
            throw new IllegalArgumentException("成绩已提交，无法直接修改");
        }
        return detail;
    }

    private boolean isLocked(Integer lockedFlag) {
        return lockedFlag != null && lockedFlag == 1;
    }

    private void validateBatchRequired(CourseScoreBatch batch) {
        if (batch == null) {
            throw new IllegalArgumentException("成绩批次数据不能为空");
        }
        if (batch.getTaskId() == null) {
            throw new IllegalArgumentException("请选择授课任务");
        }
        if (batch.getObjectiveId() == null) {
            throw new IllegalArgumentException("请选择课程目标");
        }
    }

    private void validateBatchNoUnique(String batchNo, Long currentId) {
        if (!StringUtils.hasText(batchNo)) {
            throw new IllegalArgumentException("批次编号不能为空");
        }
        long count = batchService.count(activeBatchWrapper()
                .eq("batch_no", batchNo)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("批次编号已存在");
        }
    }

    private void validateDetailRequired(CourseScoreDetail detail) {
        if (detail == null) {
            throw new IllegalArgumentException("成绩明细数据不能为空");
        }
        if (detail.getBatchId() == null) {
            throw new IllegalArgumentException("请选择成绩批次");
        }
        if (detail.getStudentId() == null) {
            throw new IllegalArgumentException("请选择学生");
        }
    }

    private void validateDetailUnique(Long batchId, Long studentId, Long currentId) {
        long count = detailService.count(activeDetailWrapper()
                .eq("batch_id", batchId)
                .eq("student_id", studentId)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("该学生在当前批次已存在成绩记录");
        }
    }

    private void validateScoreRange(BigDecimal rawScore) {
        if (rawScore == null) {
            throw new IllegalArgumentException("成绩不能为空");
        }
        if (rawScore.compareTo(BigDecimal.ZERO) < 0 || rawScore.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException("成绩必须在 0 到 100 之间");
        }
    }

    private void applyScoreFields(CourseScoreDetail detail, Long methodId) {
        BigDecimal raw = detail.getRawScore() == null ? BigDecimal.ZERO : detail.getRawScore();
        BigDecimal ratio = resolveMethodRatio(methodId);
        BigDecimal weighted = raw.multiply(ratio).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        detail.setWeightedScore(weighted);
        detail.setTotalScore(weighted);
    }

    private BigDecimal resolveMethodRatio(Long methodId) {
        if (methodId == null) {
            return HUNDRED;
        }
        EduCourseAssessmentMethod method = assessmentMethodService.getById(methodId);
        if (method == null || method.getRatioPercent() == null) {
            return HUNDRED;
        }
        return method.getRatioPercent();
    }

    private void markBatchPending(CourseScoreBatch batch, boolean imported) {
        if (batch == null || isLocked(batch.getLockedFlag())) {
            return;
        }
        batch.setCalcStatus("PENDING");
        batch.setCalculatedAt(null);
        if (imported) {
            batch.setImportedAt(LocalDateTime.now());
        }
        EntityAuditSupport.touchUpdate(batch);
        batchService.updateById(batch);
    }

    private void markDetailsDeleted(List<CourseScoreDetail> details) {
        if (details == null || details.isEmpty()) {
            return;
        }
        for (CourseScoreDetail detail : details) {
            EntityAuditSupport.touchDelete(detail);
        }
        detailService.updateBatchById(details);
    }

    private Object nullable(Object value) {
        return value == null ? "" : value;
    }

    public record ImportResult(int inserted, int updated, int failed, List<String> errors) {
    }
}
