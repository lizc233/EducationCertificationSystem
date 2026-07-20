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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @Operation(summary = "Calculate graduation requirement results")
    public Result<List<EvalGraduationRequirementResult>> calculate(
            @RequestBody EvalGraduationRequirementCalculateRequest request) {
        return Result.success(evalGraduationRequirementResultService.calculate(request));
    }

    @PostMapping("/{id}/recalculate")
    @Operation(summary = "Recalculate graduation requirement result")
    public Result<EvalGraduationRequirementResult> recalculate(@PathVariable Long id,
                                                               @RequestParam(required = false) String remark) {
        return Result.success(evalGraduationRequirementResultService.recalculate(id, remark));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm graduation requirement result")
    public Result<EvalGraduationRequirementResult> confirm(@PathVariable Long id) {
        return Result.success(evalGraduationRequirementResultService.confirm(id));
    }

    @PostMapping("/{id}/close-warning")
    @Operation(summary = "Close warning")
    public Result<EvalGraduationRequirementResult> closeWarning(@PathVariable Long id,
                                                                @RequestParam(required = false) String remark) {
        return Result.success(evalGraduationRequirementResultService.closeWarning(id, remark));
    }

    @PostMapping("/notify-warnings")
    @Operation(summary = "Notify warning results")
    public Result<Integer> notifyWarnings(@RequestBody EvalGraduationWarningNotifyRequest request) {
        return Result.success(evalGraduationRequirementResultService.notifyWarnings(request));
    }
}
