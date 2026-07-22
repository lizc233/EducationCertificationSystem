package com.educationcertificationsystem.eval.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.eval.service.EvalCourseTargetResultService;
import com.educationcertificationsystem.model.dto.eval.EvalCourseTargetCalculateRequest;
import com.educationcertificationsystem.model.entity.EvalCourseTargetResult;
import com.educationcertificationsystem.model.vo.eval.EvalCourseTargetResultDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalCourseTargetResultPageVO;
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
@Tag(name = "Course Target Evaluation")
@RequestMapping("/api/eval/course-target-results")
public class EvalCourseTargetResultController {

    private final EvalCourseTargetResultService evalCourseTargetResultService;

    @GetMapping
    @Operation(summary = "Page course target results")
    public Result<Page<EvalCourseTargetResultPageVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long objectiveId,
            @RequestParam(required = false) Long modelId,
            @RequestParam(required = false) Integer lockedFlag,
            @RequestParam(required = false) String keyword) {
        return Result.success(evalCourseTargetResultService.pageByCondition(
                pageNum, pageSize, taskId, semesterId, courseId, classId, objectiveId, modelId, lockedFlag, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Course target result detail")
    public Result<EvalCourseTargetResultDetailVO> detail(@PathVariable Long id) {
        EvalCourseTargetResultDetailVO detail = evalCourseTargetResultService.getDetail(id);
        if (detail == null) {
            return Result.error("Result not found");
        }
        return Result.success(detail);
    }

    @PostMapping("/calculate")
    @Operation(summary = "Calculate course target results")
    public Result<List<EvalCourseTargetResult>> calculate(@RequestBody EvalCourseTargetCalculateRequest request) {
        return Result.success(evalCourseTargetResultService.calculate(request));
    }

    @PostMapping("/{id}/recalculate")
    @Operation(summary = "Recalculate course target result")
    public Result<EvalCourseTargetResult> recalculate(@PathVariable Long id,
                                                      @RequestParam(required = false) String remark) {
        return Result.success(evalCourseTargetResultService.recalculate(id, remark));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm course target result")
    public Result<EvalCourseTargetResult> confirm(@PathVariable Long id) {
        return Result.success(evalCourseTargetResultService.confirm(id));
    }
}
