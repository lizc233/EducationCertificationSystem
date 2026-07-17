package com.educationcertificationsystem.eval.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.eval.service.EvalGraduationRequirementResultService;
import com.educationcertificationsystem.model.dto.eval.EvalGraduationRequirementCalculateRequest;
import com.educationcertificationsystem.model.dto.eval.EvalGraduationWarningNotifyRequest;
import com.educationcertificationsystem.model.entity.EvalGraduationRequirementResult;
import com.educationcertificationsystem.model.vo.eval.EvalGraduationRequirementResultDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalGraduationRequirementResultPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Graduation Requirement Evaluation")
@RequestMapping("/api/eval/graduation-requirement-results")
public class EvalGraduationRequirementResultController {

    private final EvalGraduationRequirementResultService evalGraduationRequirementResultService;

    @GetMapping
    @Operation(summary = "Page graduation requirement results")
    public Result<Page<EvalGraduationRequirementResultPageVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long programVersionId,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Long requirementId,
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) Integer warningFlag,
            @RequestParam(required = false) Integer lockFlag,
            @RequestParam(required = false) String keyword) {
        return Result.success(evalGraduationRequirementResultService.pageByCondition(
                pageNum, pageSize, programVersionId, majorId, requirementId, modelId, warningFlag, lockFlag, keyword));
    }

    @GetMapping("/warnings")
    @Operation(summary = "Page warning results")
    public Result<Page<EvalGraduationRequirementResultPageVO>> warnings(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long programVersionId,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) String keyword) {
        return Result.success(evalGraduationRequirementResultService.pageByCondition(
                pageNum, pageSize, programVersionId, majorId, null, modelId, 1, null, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Graduation requirement result detail")
    public Result<EvalGraduationRequirementResultDetailVO> detail(@PathVariable Long id) {
        EvalGraduationRequirementResultDetailVO detail = evalGraduationRequirementResultService.getDetail(id);
        if (detail == null) {
            return Result.error("Result not found");
        }
        return Result.success(detail);
    }

    @PostMapping("/calculate")
    @Transactional
    @Operation(summary = "Calculate graduation requirement results")
    public Result<List<EvalGraduationRequirementResult>> calculate(
            @RequestBody EvalGraduationRequirementCalculateRequest request) {
        try {
            return Result.success(evalGraduationRequirementResultService.calculate(request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Calculate graduation requirement results failed", ex);
            return Result.error("Calculate graduation requirement results failed");
        }
    }

    @PostMapping("/{id}/recalculate")
    @Transactional
    @Operation(summary = "Recalculate graduation requirement result")
    public Result<EvalGraduationRequirementResult> recalculate(@PathVariable Long id,
                                                               @RequestParam(required = false) String remark) {
        try {
            return Result.success(evalGraduationRequirementResultService.recalculate(id, remark));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Recalculate graduation requirement result failed, id={}", id, ex);
            return Result.error("Recalculate graduation requirement result failed");
        }
    }

    @PostMapping("/{id}/confirm")
    @Transactional
    @Operation(summary = "Confirm graduation requirement result")
    public Result<EvalGraduationRequirementResult> confirm(@PathVariable Long id) {
        try {
            return Result.success(evalGraduationRequirementResultService.confirm(id));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Confirm graduation requirement result failed, id={}", id, ex);
            return Result.error("Confirm graduation requirement result failed");
        }
    }

    @PostMapping("/{id}/close-warning")
    @Transactional
    @Operation(summary = "Close warning")
    public Result<EvalGraduationRequirementResult> closeWarning(@PathVariable Long id,
                                                                @RequestParam(required = false) String remark) {
        try {
            return Result.success(evalGraduationRequirementResultService.closeWarning(id, remark));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Close warning failed, id={}", id, ex);
            return Result.error("Close warning failed");
        }
    }

    @PostMapping("/notify-warnings")
    @Transactional
    @Operation(summary = "Notify warning results")
    public Result<Integer> notifyWarnings(@RequestBody EvalGraduationWarningNotifyRequest request) {
        try {
            return Result.success(evalGraduationRequirementResultService.notifyWarnings(request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Notify warning results failed", ex);
            return Result.error("Notify warning results failed");
        }
    }
}
