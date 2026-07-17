package com.educationcertificationsystem.improve.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.improve.service.ImprovePlanService;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanActionProgressRequest;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanRecordSaveRequest;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanReminderRequest;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanSaveRequest;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanVerifyRequest;
import com.educationcertificationsystem.model.entity.ImprovePlan;
import com.educationcertificationsystem.model.entity.ImprovePlanAction;
import com.educationcertificationsystem.model.entity.ImprovePlanRecord;
import com.educationcertificationsystem.model.vo.improve.ImprovePlanDetailVO;
import com.educationcertificationsystem.model.vo.improve.ImprovePlanPageVO;
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
@Tag(name = "Improve Plan")
@RequestMapping("/api/improve/plans")
public class ImprovePlanController {

    private final ImprovePlanService improvePlanService;

    @GetMapping
    @Operation(summary = "Page improve plans")
    public Result<Page<ImprovePlanPageVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long ownerUserId,
            @RequestParam(required = false) Long responsibleUserId,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false, defaultValue = "0") Integer overdueOnly,
            @RequestParam(required = false) String keyword) {
        return Result.success(improvePlanService.pageByCondition(
                pageNum, pageSize, status, sourceType, targetType, ownerUserId, responsibleUserId, priority, overdueOnly, keyword));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Page overdue improve plans")
    public Result<Page<ImprovePlanPageVO>> overdue(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long ownerUserId,
            @RequestParam(required = false) Long responsibleUserId,
            @RequestParam(required = false) String keyword) {
        return Result.success(improvePlanService.pageByCondition(
                pageNum, pageSize, null, null, null, ownerUserId, responsibleUserId, null, 1, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Improve plan detail")
    public Result<ImprovePlanDetailVO> detail(@PathVariable Long id) {
        ImprovePlanDetailVO detail = improvePlanService.getDetail(id);
        if (detail == null) {
            return Result.error("Improve plan not found");
        }
        return Result.success(detail);
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Create improve plan")
    public Result<ImprovePlanDetailVO> create(@RequestBody ImprovePlanSaveRequest request) {
        try {
            return Result.success(improvePlanService.createPlan(request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Create improve plan failed", ex);
            return Result.error("Create improve plan failed");
        }
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Update improve plan")
    public Result<ImprovePlanDetailVO> update(@PathVariable Long id, @RequestBody ImprovePlanSaveRequest request) {
        try {
            return Result.success(improvePlanService.updatePlan(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Update improve plan failed, id={}", id, ex);
            return Result.error("Update improve plan failed");
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Delete improve plan")
    public Result<String> delete(@PathVariable Long id) {
        try {
            improvePlanService.deletePlan(id);
            return Result.success("Deleted successfully");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Delete improve plan failed, id={}", id, ex);
            return Result.error("Delete improve plan failed");
        }
    }

    @PostMapping("/{id}/start")
    @Transactional
    @Operation(summary = "Start improve plan")
    public Result<ImprovePlan> start(@PathVariable Long id) {
        try {
            return Result.success(improvePlanService.startPlan(id));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Start improve plan failed, id={}", id, ex);
            return Result.error("Start improve plan failed");
        }
    }

    @PostMapping("/{id}/complete")
    @Transactional
    @Operation(summary = "Complete improve plan")
    public Result<ImprovePlan> complete(@PathVariable Long id) {
        try {
            return Result.success(improvePlanService.completePlan(id));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Complete improve plan failed, id={}", id, ex);
            return Result.error("Complete improve plan failed");
        }
    }

    @PostMapping("/{id}/verify")
    @Transactional
    @Operation(summary = "Verify improve plan")
    public Result<ImprovePlan> verify(@PathVariable Long id, @RequestBody ImprovePlanVerifyRequest request) {
        try {
            return Result.success(improvePlanService.verifyPlan(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Verify improve plan failed, id={}", id, ex);
            return Result.error("Verify improve plan failed");
        }
    }

    @PostMapping("/{id}/remind")
    @Transactional
    @Operation(summary = "Send improve reminder")
    public Result<Integer> remind(@PathVariable Long id, @RequestBody(required = false) ImprovePlanReminderRequest request) {
        try {
            return Result.success(improvePlanService.sendReminder(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Send improve reminder failed, id={}", id, ex);
            return Result.error("Send improve reminder failed");
        }
    }

    @PostMapping("/actions/{actionId}/progress")
    @Transactional
    @Operation(summary = "Update action progress")
    public Result<ImprovePlanAction> updateActionProgress(@PathVariable Long actionId,
                                                          @RequestBody ImprovePlanActionProgressRequest request) {
        try {
            return Result.success(improvePlanService.updateActionProgress(actionId, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Update improve action progress failed, actionId={}", actionId, ex);
            return Result.error("Update improve action progress failed");
        }
    }

    @PostMapping("/actions/{actionId}/records")
    @Transactional
    @Operation(summary = "Add improve record")
    public Result<ImprovePlanRecord> addRecord(@PathVariable Long actionId,
                                               @RequestBody ImprovePlanRecordSaveRequest request) {
        try {
            return Result.success(improvePlanService.addRecord(actionId, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Add improve record failed, actionId={}", actionId, ex);
            return Result.error("Add improve record failed");
        }
    }

    @PutMapping("/records/{recordId}")
    @Transactional
    @Operation(summary = "Update improve record")
    public Result<ImprovePlanRecord> updateRecord(@PathVariable Long recordId,
                                                  @RequestBody ImprovePlanRecordSaveRequest request) {
        try {
            return Result.success(improvePlanService.updateRecord(recordId, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Update improve record failed, recordId={}", recordId, ex);
            return Result.error("Update improve record failed");
        }
    }

    @DeleteMapping("/records/{recordId}")
    @Transactional
    @Operation(summary = "Delete improve record")
    public Result<String> deleteRecord(@PathVariable Long recordId) {
        try {
            improvePlanService.deleteRecord(recordId);
            return Result.success("Deleted successfully");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Delete improve record failed, recordId={}", recordId, ex);
            return Result.error("Delete improve record failed");
        }
    }
}
