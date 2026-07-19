package com.educationcertificationsystem.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.ai.service.AiAssistantService;
import com.educationcertificationsystem.ai.service.AiKnowledgeIndexService;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.model.dto.ai.AiAnalysisRetryRequest;
import com.educationcertificationsystem.model.dto.ai.AiImproveSuggestionConfirmRequest;
import com.educationcertificationsystem.model.dto.ai.AiImproveSuggestionGenerateRequest;
import com.educationcertificationsystem.model.dto.ai.AiKnowledgeRebuildRequest;
import com.educationcertificationsystem.model.dto.ai.AiReportAssistantConfirmRequest;
import com.educationcertificationsystem.model.dto.ai.AiReportAssistantGenerateRequest;
import com.educationcertificationsystem.model.vo.ai.AiAnalysisHistoryVO;
import com.educationcertificationsystem.model.vo.ai.AiAnalysisResultVO;
import com.educationcertificationsystem.model.vo.ai.AiKnowledgeRebuildVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "AI Assistant")
@RequestMapping("/api/ai")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;
    private final AiKnowledgeIndexService aiKnowledgeIndexService;

    @PostMapping("/reports/chapters/{chapterId}/generate")
    @Transactional
    @Operation(summary = "Generate AI report chapter content")
    public Result<AiAnalysisResultVO> generateReport(@PathVariable Long chapterId,
                                                     @RequestBody AiReportAssistantGenerateRequest request) {
        try {
            return Result.success(aiAssistantService.generateReportAssistant(chapterId, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Generate AI report content failed, chapterId={}", chapterId, ex);
            return Result.error("Generate AI report content failed");
        }
    }

    @PostMapping("/reports/chapters/{chapterId}/confirm")
    @Transactional
    @Operation(summary = "Confirm AI report chapter content")
    public Result<AiAnalysisResultVO> confirmReport(@PathVariable Long chapterId,
                                                    @RequestBody AiReportAssistantConfirmRequest request) {
        try {
            return Result.success(aiAssistantService.confirmReportAssistant(chapterId, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Confirm AI report content failed, chapterId={}", chapterId, ex);
            return Result.error("Confirm AI report content failed");
        }
    }

    @PostMapping("/improve-suggestions/generate")
    @Transactional
    @Operation(summary = "Generate AI improve suggestion")
    public Result<AiAnalysisResultVO> generateImprove(@RequestBody AiImproveSuggestionGenerateRequest request) {
        try {
            return Result.success(aiAssistantService.generateImproveSuggestion(request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Generate AI improve suggestion failed", ex);
            return Result.error("Generate AI improve suggestion failed");
        }
    }

    @PostMapping("/improve-suggestions/confirm")
    @Transactional
    @Operation(summary = "Confirm AI improve suggestion")
    public Result<AiAnalysisResultVO> confirmImprove(@RequestBody AiImproveSuggestionConfirmRequest request) {
        try {
            return Result.success(aiAssistantService.confirmImproveSuggestion(request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Confirm AI improve suggestion failed", ex);
            return Result.error("Confirm AI improve suggestion failed");
        }
    }

    @PostMapping("/requests/{requestId}/retry")
    @Transactional
    @Operation(summary = "Retry AI request")
    public Result<AiAnalysisResultVO> retry(@PathVariable Long requestId,
                                            @RequestBody(required = false) AiAnalysisRetryRequest request) {
        try {
            return Result.success(aiAssistantService.retry(requestId, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Retry AI request failed, requestId={}", requestId, ex);
            return Result.error("Retry AI request failed");
        }
    }

    @GetMapping("/requests")
    @Operation(summary = "Page AI request history")
    public Result<Page<AiAnalysisHistoryVO>> history(@RequestParam(defaultValue = "1") long pageNum,
                                                     @RequestParam(defaultValue = "10") long pageSize,
                                                     @RequestParam(required = false) String scenarioType,
                                                     @RequestParam(required = false) String sourceType,
                                                     @RequestParam(required = false) Long requesterUserId,
                                                     @RequestParam(required = false) String requestStatus) {
        return Result.success(aiAssistantService.pageHistory(
                pageNum, pageSize, scenarioType, sourceType, requesterUserId, requestStatus));
    }

    @GetMapping("/requests/{requestId}")
    @Operation(summary = "AI request detail")
    public Result<AiAnalysisResultVO> detail(@PathVariable Long requestId) {
        try {
            return Result.success(aiAssistantService.getDetail(requestId));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Get AI request detail failed, requestId={}", requestId, ex);
            return Result.error("Get AI request detail failed");
        }
    }

    @PostMapping("/knowledge/rebuild")
    @Transactional
    @Operation(summary = "Rebuild AI knowledge index")
    public Result<AiKnowledgeRebuildVO> rebuild(@RequestBody(required = false) AiKnowledgeRebuildRequest request) {
        try {
            return Result.success(aiKnowledgeIndexService.rebuild(request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Rebuild AI knowledge failed", ex);
            return Result.error("Rebuild AI knowledge failed");
        }
    }
}
