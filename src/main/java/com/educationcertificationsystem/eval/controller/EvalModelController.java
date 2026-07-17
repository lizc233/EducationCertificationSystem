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
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
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
    @Transactional
    @Operation(summary = "Create model")
    public Result<EvalModelDetailVO> create(@RequestBody EvalModelSaveRequest request) {
        try {
            return Result.success(evalModelService.createModel(request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Create evaluation model failed", ex);
            return Result.error("Create evaluation model failed");
        }
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Update model")
    public Result<EvalModelDetailVO> update(@PathVariable Long id, @RequestBody EvalModelSaveRequest request) {
        try {
            return Result.success(evalModelService.updateModel(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Update evaluation model failed, id={}", id, ex);
            return Result.error("Update evaluation model failed");
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Delete model")
    public Result<String> delete(@PathVariable Long id) {
        try {
            evalModelService.deleteModel(id);
            return Result.success("Deleted successfully");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Delete evaluation model failed, id={}", id, ex);
            return Result.error("Delete evaluation model failed");
        }
    }

    @PostMapping("/{id}/enable")
    @Transactional
    @Operation(summary = "Enable model")
    public Result<EvalModel> enable(@PathVariable Long id) {
        try {
            return Result.success(evalModelService.updateEnabled(id, 1));
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Enable evaluation model failed, id={}", id, ex);
            return Result.error("Enable evaluation model failed");
        }
    }

    @PostMapping("/{id}/disable")
    @Transactional
    @Operation(summary = "Disable model")
    public Result<EvalModel> disable(@PathVariable Long id) {
        try {
            return Result.success(evalModelService.updateEnabled(id, 0));
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Disable evaluation model failed, id={}", id, ex);
            return Result.error("Disable evaluation model failed");
        }
    }

    @PostMapping("/{id}/status")
    @Transactional
    @Operation(summary = "Update model status")
    public Result<EvalModel> updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            return Result.success(evalModelService.updateStatus(id, status));
        } catch (IllegalArgumentException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Update model status failed, id={}", id, ex);
            return Result.error("Update model status failed");
        }
    }
}
