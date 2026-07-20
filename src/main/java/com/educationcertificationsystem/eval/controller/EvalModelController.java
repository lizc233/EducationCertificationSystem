package com.educationcertificationsystem.eval.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.eval.service.EvalModelService;
import com.educationcertificationsystem.model.dto.eval.EvalModelSaveRequest;
import com.educationcertificationsystem.model.entity.EvalModel;
import com.educationcertificationsystem.model.vo.eval.EvalModelDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalModelPageItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Evaluation Model Config")
@RequestMapping("/api/eval/models")
public class EvalModelController {

    private final EvalModelService evalModelService;

    @GetMapping
    @Operation(summary = "Page models")
    public Result<Page<EvalModelPageItemVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String modelType,
            @RequestParam(required = false) String scopeType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer enabled,
            @RequestParam(required = false) String keyword) {
        return Result.success(evalModelService.pageByCondition(
                pageNum, pageSize, modelType, scopeType, status, enabled, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Model detail")
    public Result<EvalModelDetailVO> detail(@PathVariable Long id) {
        EvalModelDetailVO detail = evalModelService.getDetail(id);
        if (detail == null) {
            return Result.error("Model not found");
        }
        return Result.success(detail);
    }

    @PostMapping
    @Operation(summary = "Create model")
    public Result<EvalModelDetailVO> create(@RequestBody EvalModelSaveRequest request) {
        return Result.success(evalModelService.createModel(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update model")
    public Result<EvalModelDetailVO> update(@PathVariable Long id, @RequestBody EvalModelSaveRequest request) {
        return Result.success(evalModelService.updateModel(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete model")
    public Result<String> delete(@PathVariable Long id) {
        evalModelService.deleteModel(id);
        return Result.success("Deleted successfully");
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "Enable model")
    public Result<EvalModel> enable(@PathVariable Long id) {
        return Result.success(evalModelService.updateEnabled(id, 1));
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "Disable model")
    public Result<EvalModel> disable(@PathVariable Long id) {
        return Result.success(evalModelService.updateEnabled(id, 0));
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Update model status")
    public Result<EvalModel> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return Result.success(evalModelService.updateStatus(id, status));
    }
}
