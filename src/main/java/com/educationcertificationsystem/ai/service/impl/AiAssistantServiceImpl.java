package com.educationcertificationsystem.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.ai.config.AiAssistantProperties;
import com.educationcertificationsystem.ai.service.AiAnalysisRequestService;
import com.educationcertificationsystem.ai.service.AiAnalysisResultService;
import com.educationcertificationsystem.ai.service.AiAssistantService;
import com.educationcertificationsystem.ai.service.AiKnowledgeIndexService;
import com.educationcertificationsystem.ai.service.AiPromptTemplateService;
import com.educationcertificationsystem.improve.service.ImprovePlanService;
import com.educationcertificationsystem.model.dto.ai.AiAnalysisRetryRequest;
import com.educationcertificationsystem.model.dto.ai.AiImproveSuggestionConfirmRequest;
import com.educationcertificationsystem.model.dto.ai.AiImproveSuggestionGenerateRequest;
import com.educationcertificationsystem.model.dto.ai.AiKnowledgeRebuildRequest;
import com.educationcertificationsystem.model.dto.ai.AiReportAssistantConfirmRequest;
import com.educationcertificationsystem.model.dto.ai.AiReportAssistantGenerateRequest;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanActionRequest;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanSaveRequest;
import com.educationcertificationsystem.model.dto.report.ReportDraftSaveRequest;
import com.educationcertificationsystem.model.entity.AiAnalysisRequest;
import com.educationcertificationsystem.model.entity.AiAnalysisResult;
import com.educationcertificationsystem.model.entity.AiPromptTemplate;
import com.educationcertificationsystem.model.entity.ReportChapter;
import com.educationcertificationsystem.model.entity.SysUser;
import com.educationcertificationsystem.model.vo.ai.AiAnalysisHistoryVO;
import com.educationcertificationsystem.model.vo.ai.AiAnalysisResultVO;
import com.educationcertificationsystem.model.vo.ai.AiRetrievedChunkVO;
import com.educationcertificationsystem.model.vo.improve.ImprovePlanDetailVO;
import com.educationcertificationsystem.report.service.ReportChapterService;
import com.educationcertificationsystem.report.service.ReportProjectService;
import com.educationcertificationsystem.support.EntityAuditSupport;
import com.educationcertificationsystem.user.service.SysUserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_CONFIRMED = "CONFIRMED";

    private static final String SCENARIO_REPORT_EXPAND = "REPORT_CHAPTER_EXPAND";
    private static final String SCENARIO_REPORT_POLISH = "REPORT_CHAPTER_POLISH";
    private static final String SCENARIO_IMPROVE = "IMPROVE_PLAN_SUGGEST";

    private final AiPromptTemplateService aiPromptTemplateService;
    private final AiAnalysisRequestService aiAnalysisRequestService;
    private final AiAnalysisResultService aiAnalysisResultService;
    private final AiKnowledgeIndexService aiKnowledgeIndexService;
    private final ReportChapterService reportChapterService;
    private final ReportProjectService reportProjectService;
    private final ImprovePlanService improvePlanService;
    private final SysUserService sysUserService;
    private final LocalHashEmbeddingService localHashEmbeddingService;
    private final QdrantVectorStoreService qdrantVectorStoreService;
    private final DeepSeekClientService deepSeekClientService;
    private final AiAssistantProperties aiAssistantProperties;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AiAnalysisResultVO generateReportAssistant(Long chapterId, AiReportAssistantGenerateRequest request) {
        ReportChapter chapter = getRequiredChapter(chapterId);
        validateUserId(request == null ? null : request.getRequesterUserId(), "Requester user is invalid");
        String scenarioType = resolveReportScenario(request == null ? null : request.getOperationType());
        String templateCode = StringUtils.hasText(request == null ? null : request.getTemplateCode())
                ? request.getTemplateCode().trim().toUpperCase(Locale.ROOT)
                : scenarioType;
        AiAnalysisRequest analysisRequest = initRequest(
                scenarioType,
                "REPORT_CHAPTER",
                chapterId,
                getRequiredTemplate(templateCode).getId(),
                request.getRequesterUserId(),
                buildReportMetadata(chapter, request));
        aiAnalysisRequestService.save(analysisRequest);
        executeReportRequest(analysisRequest, chapter, request);
        return getDetail(analysisRequest.getId());
    }

    @Override
    @Transactional
    public AiAnalysisResultVO confirmReportAssistant(Long chapterId, AiReportAssistantConfirmRequest request) {
        ReportChapter chapter = getRequiredChapter(chapterId);
        if (request == null || request.getRequestId() == null) {
            throw new IllegalArgumentException("Request id is required");
        }
        validateUserId(request.getConfirmedBy(), "Confirm user is invalid");
        AiAnalysisRequest analysisRequest = getRequiredRequest(request.getRequestId());
        if (!Objects.equals(analysisRequest.getSourceId(), chapterId)) {
            throw new IllegalArgumentException("AI request does not match chapter");
        }
        AiAnalysisResult result = getRequiredResult(analysisRequest.getId());
        if (!STATUS_SUCCESS.equals(analysisRequest.getRequestStatus())) {
            throw new IllegalStateException("Only successful AI result can be confirmed");
        }
        String applyMode = StringUtils.hasText(request.getApplyMode())
                ? request.getApplyMode().trim().toUpperCase(Locale.ROOT)
                : "REPLACE";
        String content = result.getResultText();
        if (!StringUtils.hasText(content)) {
            throw new IllegalStateException("AI result text is empty");
        }
        if ("APPEND".equals(applyMode) && StringUtils.hasText(chapter.getContentText())) {
            content = chapter.getContentText().trim() + System.lineSeparator() + System.lineSeparator() + content.trim();
        }
        ReportDraftSaveRequest draftRequest = new ReportDraftSaveRequest();
        draftRequest.setEditedBy(request.getConfirmedBy());
        draftRequest.setDraftContent(content);
        draftRequest.setChapterStatus("IN_PROGRESS");
        draftRequest.setProgressPercent(null);
        draftRequest.setComment("AI assistant confirmed");
        draftRequest.setRemark(request.getRemark());
        reportProjectService.saveDraft(chapterId, draftRequest);
        markConfirmed(analysisRequest, result, request.getConfirmedBy(), "REPORT_DRAFT");
        return getDetail(analysisRequest.getId());
    }

    @Override
    @Transactional
    public AiAnalysisResultVO generateImproveSuggestion(AiImproveSuggestionGenerateRequest request) {
        validateImproveGenerateRequest(request);
        String templateCode = StringUtils.hasText(request.getTemplateCode())
                ? request.getTemplateCode().trim().toUpperCase(Locale.ROOT)
                : SCENARIO_IMPROVE;
        AiAnalysisRequest analysisRequest = initRequest(
                SCENARIO_IMPROVE,
                request.getSourceType().trim().toUpperCase(Locale.ROOT),
                request.getSourceId(),
                getRequiredTemplate(templateCode).getId(),
                request.getRequesterUserId(),
                buildImproveMetadata(request));
        aiAnalysisRequestService.save(analysisRequest);
        executeImproveRequest(analysisRequest, request);
        return getDetail(analysisRequest.getId());
    }

    @Override
    @Transactional
    public AiAnalysisResultVO confirmImproveSuggestion(AiImproveSuggestionConfirmRequest request) {
        if (request == null || request.getRequestId() == null) {
            throw new IllegalArgumentException("Request id is required");
        }
        validateUserId(request.getConfirmedBy(), "Confirm user is invalid");
        AiAnalysisRequest analysisRequest = getRequiredRequest(request.getRequestId());
        if (!SCENARIO_IMPROVE.equals(analysisRequest.getScenarioType())) {
            throw new IllegalArgumentException("AI request scenario is invalid");
        }
        AiAnalysisResult result = getRequiredResult(analysisRequest.getId());
        if (!STATUS_SUCCESS.equals(analysisRequest.getRequestStatus())) {
            throw new IllegalStateException("Only successful AI result can be confirmed");
        }
        Map<String, Object> metadata = parseJsonMap(analysisRequest.getRemark());
        JsonNode resultNode = readJsonNode(result.getResultJson());
        ImprovePlanSaveRequest saveRequest = new ImprovePlanSaveRequest();
        saveRequest.setPlanCode(StringUtils.hasText(request.getPlanCode())
                ? request.getPlanCode().trim()
                : "AIIP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        saveRequest.setPlanName(StringUtils.hasText(request.getPlanName())
                ? request.getPlanName().trim()
                : readText(resultNode, "planName", "AI Improve Plan"));
        saveRequest.setSourceType(analysisRequest.getSourceType());
        saveRequest.setSourceId(analysisRequest.getSourceId());
        saveRequest.setTargetType(asString(metadata.get("targetType")));
        saveRequest.setTargetId(asLong(metadata.get("targetId")));
        saveRequest.setOwnerUserId(request.getOwnerUserId() != null
                ? request.getOwnerUserId()
                : fallbackOwnerUserId(metadata, request.getConfirmedBy()));
        saveRequest.setStartDate(request.getStartDate() == null ? LocalDate.now() : request.getStartDate());
        saveRequest.setDueDate(resolveImproveDueDate(request, metadata, resultNode));
        saveRequest.setPriority(requestPriority(metadata, resultNode));
        saveRequest.setRemark(StringUtils.hasText(request.getRemark())
                ? request.getRemark().trim()
                : readText(resultNode, "planSummary", "Generated by AI assistant"));
        saveRequest.setActions(buildActionRequests(resultNode, request, saveRequest));
        ImprovePlanDetailVO created = improvePlanService.createPlan(saveRequest);
        markConfirmed(analysisRequest, result, request.getConfirmedBy(), "IMPROVE_PLAN:" + created.getId());
        return getDetail(analysisRequest.getId());
    }

    @Override
    @Transactional
    public AiAnalysisResultVO retry(Long requestId, AiAnalysisRetryRequest request) {
        AiAnalysisRequest analysisRequest = getRequiredRequest(requestId);
        analysisRequest.setRetryCount((analysisRequest.getRetryCount() == null ? 0 : analysisRequest.getRetryCount()) + 1);
        analysisRequest.setRequestStatus(STATUS_PENDING);
        analysisRequest.setFinishedAt(null);
        EntityAuditSupport.touchUpdate(analysisRequest);
        aiAnalysisRequestService.updateById(analysisRequest);

        if (SCENARIO_REPORT_EXPAND.equals(analysisRequest.getScenarioType())
                || SCENARIO_REPORT_POLISH.equals(analysisRequest.getScenarioType())) {
            ReportChapter chapter = getRequiredChapter(analysisRequest.getSourceId());
            AiReportAssistantGenerateRequest generateRequest = new AiReportAssistantGenerateRequest();
            Map<String, Object> metadata = parseJsonMap(analysisRequest.getRemark());
            generateRequest.setRequesterUserId(analysisRequest.getRequesterUserId());
            generateRequest.setOperationType(asString(metadata.get("operationType")));
            generateRequest.setTopK(asInteger(metadata.get("topK")));
            generateRequest.setUseRagFlag(asInteger(metadata.get("useRagFlag")));
            generateRequest.setTemplateCode(analysisRequest.getScenarioType());
            executeReportRequest(analysisRequest, chapter, generateRequest);
        } else if (SCENARIO_IMPROVE.equals(analysisRequest.getScenarioType())) {
            Map<String, Object> metadata = parseJsonMap(analysisRequest.getRemark());
            AiImproveSuggestionGenerateRequest generateRequest = new AiImproveSuggestionGenerateRequest();
            generateRequest.setRequesterUserId(analysisRequest.getRequesterUserId());
            generateRequest.setOwnerUserId(asLong(metadata.get("ownerUserId")));
            generateRequest.setResponsibleUserId(asLong(metadata.get("responsibleUserId")));
            generateRequest.setSourceType(analysisRequest.getSourceType());
            generateRequest.setSourceId(analysisRequest.getSourceId());
            generateRequest.setTargetType(asString(metadata.get("targetType")));
            generateRequest.setTargetId(asLong(metadata.get("targetId")));
            generateRequest.setPriority(asInteger(metadata.get("priority")));
            generateRequest.setDueDays(asInteger(metadata.get("dueDays")));
            generateRequest.setTopK(asInteger(metadata.get("topK")));
            generateRequest.setTemplateCode(SCENARIO_IMPROVE);
            executeImproveRequest(analysisRequest, generateRequest);
        } else {
            throw new IllegalArgumentException("Unsupported AI request scenario");
        }
        return getDetail(requestId);
    }

    @Override
    public Page<AiAnalysisHistoryVO> pageHistory(long pageNum,
                                                 long pageSize,
                                                 String scenarioType,
                                                 String sourceType,
                                                 Long requesterUserId,
                                                 String requestStatus) {
        long current = Math.max(pageNum, 1L);
        long size = Math.max(pageSize, 1L);
        Page<AiAnalysisRequest> page = aiAnalysisRequestService.page(
                new Page<>(current, size),
                new LambdaQueryWrapper<AiAnalysisRequest>()
                        .eq(StringUtils.hasText(scenarioType), AiAnalysisRequest::getScenarioType, scenarioType)
                        .eq(StringUtils.hasText(sourceType), AiAnalysisRequest::getSourceType, sourceType)
                        .eq(requesterUserId != null, AiAnalysisRequest::getRequesterUserId, requesterUserId)
                        .eq(StringUtils.hasText(requestStatus), AiAnalysisRequest::getRequestStatus, requestStatus)
                        .eq(AiAnalysisRequest::getIsDeleted, 0)
                        .orderByDesc(AiAnalysisRequest::getRequestedAt)
                        .orderByDesc(AiAnalysisRequest::getId));
        List<Long> requestIds = page.getRecords().stream().map(AiAnalysisRequest::getId).toList();
        Map<Long, AiAnalysisResult> resultMap = listResultsByRequestIds(requestIds).stream()
                .collect(Collectors.toMap(AiAnalysisResult::getRequestId, result -> result, (left, right) -> left, LinkedHashMap::new));
        Page<AiAnalysisHistoryVO> resultPage = new Page<>(current, size);
        resultPage.setTotal(page.getTotal());
        resultPage.setRecords(page.getRecords().stream().map(request -> {
            AiAnalysisHistoryVO vo = new AiAnalysisHistoryVO();
            vo.setRequestId(request.getId());
            vo.setRequestNo(request.getRequestNo());
            vo.setScenarioType(request.getScenarioType());
            vo.setSourceType(request.getSourceType());
            vo.setSourceId(request.getSourceId());
            vo.setRequesterUserId(request.getRequesterUserId());
            vo.setRequestStatus(request.getRequestStatus());
            vo.setRetryCount(request.getRetryCount());
            vo.setModelName(request.getModelName());
            vo.setRequestedAt(request.getRequestedAt());
            vo.setFinishedAt(request.getFinishedAt());
            AiAnalysisResult analysisResult = resultMap.get(request.getId());
            if (analysisResult != null) {
                vo.setResultType(analysisResult.getResultType());
                vo.setHumanConfirmedFlag(analysisResult.getHumanConfirmedFlag());
            }
            return vo;
        }).toList());
        return resultPage;
    }

    @Override
    public AiAnalysisResultVO getDetail(Long requestId) {
        AiAnalysisRequest request = getRequiredRequest(requestId);
        AiAnalysisResult result = getRequiredResult(requestId);
        AiAnalysisResultVO vo = new AiAnalysisResultVO();
        vo.setRequestId(request.getId());
        vo.setRequestNo(request.getRequestNo());
        vo.setScenarioType(request.getScenarioType());
        vo.setSourceType(request.getSourceType());
        vo.setSourceId(request.getSourceId());
        vo.setRequesterUserId(request.getRequesterUserId());
        vo.setRequestStatus(request.getRequestStatus());
        vo.setRetryCount(request.getRetryCount());
        vo.setModelName(request.getModelName());
        vo.setRequestedAt(request.getRequestedAt());
        vo.setFinishedAt(request.getFinishedAt());
        vo.setRemark(request.getRemark());
        vo.setResultType(result.getResultType());
        vo.setResultText(result.getResultText());
        vo.setResultJson(result.getResultJson());
        vo.setHumanConfirmedFlag(result.getHumanConfirmedFlag());
        vo.setConfirmedBy(result.getConfirmedBy());
        vo.setConfirmedAt(result.getConfirmedAt());
        vo.setRetrievedChunks(extractRetrievedChunks(result));
        return vo;
    }

    private void executeReportRequest(AiAnalysisRequest request, ReportChapter chapter, AiReportAssistantGenerateRequest generateRequest) {
        try {
            aiKnowledgeIndexService.rebuildReportProjectKnowledge(chapter.getProjectId());
            List<AiRetrievedChunkVO> retrievedChunks = retrieveReportChunks(chapter, generateRequest);
            AiPromptTemplate template = getRequiredTemplate(request.getTemplateId());
            String businessContext = buildReportBusinessContext(chapter, generateRequest);
            String retrievedContext = buildRetrievedContext(retrievedChunks);
            String userPrompt = renderTemplate(template.getUserPrompt(), businessContext, retrievedContext);
            request.setPromptSnapshot(userPrompt);
            request.setModelName("DEEPSEEK:" + deepSeekModel());
            EntityAuditSupport.touchUpdate(request);
            aiAnalysisRequestService.updateById(request);
            String modelText = deepSeekClientService.generate(template.getSystemPrompt(), userPrompt, false);
            Map<String, Object> resultPayload = new LinkedHashMap<>();
            resultPayload.put("retrievedChunks", retrievedChunks);
            saveSuccessResult(request, "REPORT_TEXT", modelText, writeJson(resultPayload));
        } catch (Exception ex) {
            saveFailedResult(request, ex.getMessage());
        }
    }

    private void executeImproveRequest(AiAnalysisRequest request, AiImproveSuggestionGenerateRequest generateRequest) {
        try {
            aiKnowledgeIndexService.rebuild(new AiKnowledgeRebuildRequest());
            List<AiRetrievedChunkVO> retrievedChunks = retrieveImproveChunks(generateRequest);
            AiPromptTemplate template = getRequiredTemplate(request.getTemplateId());
            String businessContext = buildImproveBusinessContext(generateRequest);
            String retrievedContext = buildRetrievedContext(retrievedChunks);
            String userPrompt = renderTemplate(template.getUserPrompt(), businessContext, retrievedContext);
            request.setPromptSnapshot(userPrompt);
            request.setModelName("DEEPSEEK:" + deepSeekModel());
            EntityAuditSupport.touchUpdate(request);
            aiAnalysisRequestService.updateById(request);
            String resultJson = deepSeekClientService.generate(template.getSystemPrompt(), userPrompt, true);
            JsonNode resultNode = readJsonNode(resultJson);
            String summary = readText(resultNode, "planSummary", readText(resultNode, "planName", "AI suggestion generated"));
            Map<String, Object> resultPayload = new LinkedHashMap<>();
            resultPayload.put("retrievedChunks", retrievedChunks);
            resultPayload.put("suggestion", objectMapper.convertValue(resultNode, Map.class));
            saveSuccessResult(request, "IMPROVE_JSON", summary, writeJson(resultPayload), resultJson);
        } catch (Exception ex) {
            saveFailedResult(request, ex.getMessage());
        }
    }

    private void saveSuccessResult(AiAnalysisRequest request, String resultType, String resultText, String payloadJson) {
        saveSuccessResult(request, resultType, resultText, payloadJson, payloadJson);
    }

    private void saveSuccessResult(AiAnalysisRequest request,
                                   String resultType,
                                   String resultText,
                                   String payloadJson,
                                   String resultJson) {
        request.setRequestStatus(STATUS_SUCCESS);
        request.setFinishedAt(LocalDateTime.now());
        EntityAuditSupport.touchUpdate(request);
        aiAnalysisRequestService.updateById(request);
        AiAnalysisResult result = getOrCreateResult(request.getId());
        result.setResultType(resultType);
        result.setResultText(resultText);
        result.setResultJson(resultJson);
        result.setHumanConfirmedFlag(0);
        result.setConfirmedBy(null);
        result.setConfirmedAt(null);
        result.setRemark(payloadJson);
        saveResult(result);
    }

    private void saveFailedResult(AiAnalysisRequest request, String errorMessage) {
        request.setRequestStatus(STATUS_FAILED);
        request.setFinishedAt(LocalDateTime.now());
        EntityAuditSupport.touchUpdate(request);
        aiAnalysisRequestService.updateById(request);
        AiAnalysisResult result = getOrCreateResult(request.getId());
        result.setResultType("ERROR");
        result.setResultText(errorMessage);
        result.setResultJson(writeJson(Map.of()));
        result.setHumanConfirmedFlag(0);
        result.setConfirmedBy(null);
        result.setConfirmedAt(null);
        result.setRemark(errorMessage);
        saveResult(result);
    }

    private void markConfirmed(AiAnalysisRequest request, AiAnalysisResult result, Long confirmedBy, String applyTarget) {
        request.setRequestStatus(STATUS_CONFIRMED);
        request.setFinishedAt(LocalDateTime.now());
        EntityAuditSupport.touchUpdate(request);
        aiAnalysisRequestService.updateById(request);
        result.setHumanConfirmedFlag(1);
        result.setConfirmedBy(confirmedBy);
        result.setConfirmedAt(LocalDateTime.now());
        result.setResultJson(appendApplyTarget(result.getResultJson(), applyTarget));
        saveResult(result);
    }

    private AiAnalysisRequest initRequest(String scenarioType,
                                          String sourceType,
                                          Long sourceId,
                                          Long templateId,
                                          Long requesterUserId,
                                          String metadataJson) {
        AiAnalysisRequest request = new AiAnalysisRequest();
        request.setRequestNo("AIR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        request.setScenarioType(scenarioType);
        request.setSourceType(sourceType);
        request.setSourceId(sourceId);
        request.setTemplateId(templateId);
        request.setRequesterUserId(requesterUserId);
        request.setRequestStatus(STATUS_PENDING);
        request.setRetryCount(0);
        request.setRequestedAt(LocalDateTime.now());
        request.setPromptSnapshot(null);
        request.setRemark(metadataJson);
        EntityAuditSupport.touchCreate(request);
        return request;
    }

    private String buildReportMetadata(ReportChapter chapter, AiReportAssistantGenerateRequest request) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("chapterId", chapter.getId());
        metadata.put("projectId", chapter.getProjectId());
        metadata.put("operationType", resolveReportOperation(request == null ? null : request.getOperationType()));
        metadata.put("useRagFlag", request == null || request.getUseRagFlag() == null ? 1 : request.getUseRagFlag());
        metadata.put("topK", request == null ? null : request.getTopK());
        return writeJson(metadata);
    }

    private String buildImproveMetadata(AiImproveSuggestionGenerateRequest request) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("ownerUserId", request.getOwnerUserId());
        metadata.put("responsibleUserId", request.getResponsibleUserId());
        metadata.put("targetType", request.getTargetType());
        metadata.put("targetId", request.getTargetId());
        metadata.put("priority", request.getPriority());
        metadata.put("dueDays", request.getDueDays());
        metadata.put("topK", request.getTopK());
        return writeJson(metadata);
    }

    private List<AiRetrievedChunkVO> retrieveReportChunks(ReportChapter chapter, AiReportAssistantGenerateRequest request) {
        List<Double> vector = localHashEmbeddingService.embed(buildReportBusinessContext(chapter, request));
        int limit = request != null && request.getTopK() != null ? request.getTopK() : 6;
        List<AiRetrievedChunkVO> merged = new ArrayList<>();
        merged.addAll(toRetrievedChunks(qdrantVectorStoreService.search(
                vector,
                limit,
                Map.of("bizType", "REPORT_CHAPTER", "projectId", chapter.getProjectId()))));
        if (StringUtils.hasText(chapter.getSourceType()) && chapter.getSourceRefId() != null) {
            merged.addAll(toRetrievedChunks(qdrantVectorStoreService.search(
                    vector,
                    limit,
                    Map.of("bizType", "IMPROVE_PLAN", "sourceType", chapter.getSourceType(), "sourceId", chapter.getSourceRefId()))));
        }
        return deduplicateChunks(merged, limit);
    }

    private List<AiRetrievedChunkVO> retrieveImproveChunks(AiImproveSuggestionGenerateRequest request) {
        List<Double> vector = localHashEmbeddingService.embed(buildImproveBusinessContext(request));
        int limit = request.getTopK() == null ? 6 : request.getTopK();
        List<AiRetrievedChunkVO> merged = new ArrayList<>();
        merged.addAll(toRetrievedChunks(qdrantVectorStoreService.search(
                vector,
                limit,
                Map.of("bizType", "IMPROVE_PLAN", "sourceType", request.getSourceType().trim().toUpperCase(Locale.ROOT), "sourceId", request.getSourceId()))));
        if (StringUtils.hasText(request.getTargetType()) && request.getTargetId() != null) {
            merged.addAll(toRetrievedChunks(qdrantVectorStoreService.search(
                    vector,
                    limit,
                    Map.of("bizType", "IMPROVE_PLAN", "targetType", request.getTargetType().trim().toUpperCase(Locale.ROOT), "targetId", request.getTargetId()))));
            merged.addAll(toRetrievedChunks(qdrantVectorStoreService.search(
                    vector,
                    limit,
                    Map.of("bizType", "REPORT_CHAPTER", "sourceType", request.getTargetType().trim().toUpperCase(Locale.ROOT), "sourceId", request.getTargetId()))));
        }
        return deduplicateChunks(merged, limit);
    }

    private List<AiRetrievedChunkVO> toRetrievedChunks(List<QdrantVectorStoreService.SearchResult> searchResults) {
        List<AiRetrievedChunkVO> result = new ArrayList<>();
        for (QdrantVectorStoreService.SearchResult searchResult : searchResults) {
            Map<String, Object> payload = searchResult.getPayload();
            AiRetrievedChunkVO vo = new AiRetrievedChunkVO();
            vo.setChunkId(asLong(payload.get("chunkId")));
            vo.setSourceType(asString(payload.get("bizType")));
            vo.setSourceId(asLong(payload.get("bizId")));
            vo.setTitle(asString(payload.get("title")));
            vo.setSnippet(asString(payload.get("snippet")));
            vo.setScore(searchResult.getScore());
            result.add(vo);
        }
        return result;
    }

    private List<AiRetrievedChunkVO> deduplicateChunks(List<AiRetrievedChunkVO> source, int limit) {
        LinkedHashMap<Long, AiRetrievedChunkVO> deduplicated = new LinkedHashMap<>();
        source.stream()
                .sorted(Comparator.comparing(AiRetrievedChunkVO::getScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .forEach(chunk -> deduplicated.putIfAbsent(chunk.getChunkId(), chunk));
        return deduplicated.values().stream().limit(limit).toList();
    }

    private String buildReportBusinessContext(ReportChapter chapter, AiReportAssistantGenerateRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("Operation: ").append(resolveReportOperation(request == null ? null : request.getOperationType())).append('\n');
        builder.append("Chapter code: ").append(chapter.getChapterCode()).append('\n');
        builder.append("Chapter title: ").append(chapter.getChapterTitle()).append('\n');
        builder.append("Project id: ").append(chapter.getProjectId()).append('\n');
        if (StringUtils.hasText(chapter.getSourceType())) {
            builder.append("Reference source type: ").append(chapter.getSourceType()).append('\n');
        }
        if (chapter.getSourceRefId() != null) {
            builder.append("Reference source id: ").append(chapter.getSourceRefId()).append('\n');
        }
        builder.append("Existing content:\n").append(StringUtils.hasText(chapter.getContentText()) ? chapter.getContentText() : "Empty");
        return builder.toString();
    }

    private String buildImproveBusinessContext(AiImproveSuggestionGenerateRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("Source type: ").append(request.getSourceType().trim().toUpperCase(Locale.ROOT)).append('\n');
        builder.append("Source id: ").append(request.getSourceId()).append('\n');
        if (StringUtils.hasText(request.getTargetType())) {
            builder.append("Target type: ").append(request.getTargetType().trim().toUpperCase(Locale.ROOT)).append('\n');
        }
        if (request.getTargetId() != null) {
            builder.append("Target id: ").append(request.getTargetId()).append('\n');
        }
        builder.append("Priority hint: ").append(request.getPriority() == null ? 1 : request.getPriority()).append('\n');
        builder.append("Due days hint: ").append(request.getDueDays() == null ? 30 : request.getDueDays()).append('\n');
        builder.append("Need concrete, executable improvement suggestions.");
        return builder.toString();
    }

    private String buildRetrievedContext(List<AiRetrievedChunkVO> retrievedChunks) {
        if (retrievedChunks == null || retrievedChunks.isEmpty()) {
            return "No retrieved knowledge.";
        }
        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (AiRetrievedChunkVO chunk : retrievedChunks) {
            builder.append(index).append(". [")
                    .append(defaultText(chunk.getSourceType())).append('#').append(chunk.getSourceId()).append("] ")
                    .append(defaultText(chunk.getTitle())).append('\n')
                    .append(defaultText(chunk.getSnippet())).append("\n\n");
            index++;
        }
        return builder.toString().trim();
    }

    private String renderTemplate(String template, String businessContext, String retrievedContext) {
        return template.replace("{{businessContext}}", businessContext)
                .replace("{{retrievedContext}}", retrievedContext);
    }

    private AiPromptTemplate getRequiredTemplate(String templateCode) {
        AiPromptTemplate template = aiPromptTemplateService.getOne(new LambdaQueryWrapper<AiPromptTemplate>()
                .eq(AiPromptTemplate::getTemplateCode, templateCode)
                .eq(AiPromptTemplate::getEnabled, 1)
                .eq(AiPromptTemplate::getIsDeleted, 0), false);
        if (template == null) {
            throw new IllegalArgumentException("AI prompt template not found: " + templateCode);
        }
        return template;
    }

    private AiPromptTemplate getRequiredTemplate(Long templateId) {
        AiPromptTemplate template = aiPromptTemplateService.getById(templateId);
        if (template == null || (template.getIsDeleted() != null && template.getIsDeleted() != 0)) {
            throw new IllegalArgumentException("AI prompt template not found");
        }
        return template;
    }

    private ReportChapter getRequiredChapter(Long chapterId) {
        ReportChapter chapter = reportChapterService.getOne(new LambdaQueryWrapper<ReportChapter>()
                .eq(ReportChapter::getId, chapterId)
                .eq(ReportChapter::getIsDeleted, 0), false);
        if (chapter == null) {
            throw new IllegalArgumentException("Report chapter not found");
        }
        return chapter;
    }

    private AiAnalysisRequest getRequiredRequest(Long requestId) {
        AiAnalysisRequest request = aiAnalysisRequestService.getOne(new LambdaQueryWrapper<AiAnalysisRequest>()
                .eq(AiAnalysisRequest::getId, requestId)
                .eq(AiAnalysisRequest::getIsDeleted, 0), false);
        if (request == null) {
            throw new IllegalArgumentException("AI analysis request not found");
        }
        return request;
    }

    private AiAnalysisResult getRequiredResult(Long requestId) {
        AiAnalysisResult result = aiAnalysisResultService.getOne(new LambdaQueryWrapper<AiAnalysisResult>()
                .eq(AiAnalysisResult::getRequestId, requestId)
                .eq(AiAnalysisResult::getIsDeleted, 0), false);
        if (result == null) {
            throw new IllegalArgumentException("AI analysis result not found");
        }
        return result;
    }

    private AiAnalysisResult getOrCreateResult(Long requestId) {
        AiAnalysisResult result = aiAnalysisResultService.getOne(new LambdaQueryWrapper<AiAnalysisResult>()
                .eq(AiAnalysisResult::getRequestId, requestId)
                .eq(AiAnalysisResult::getIsDeleted, 0), false);
        if (result != null) {
            EntityAuditSupport.touchUpdate(result);
            return result;
        }
        result = new AiAnalysisResult();
        result.setRequestId(requestId);
        EntityAuditSupport.touchCreate(result);
        return result;
    }

    private void saveResult(AiAnalysisResult result) {
        if (result.getId() == null) {
            aiAnalysisResultService.save(result);
        } else {
            aiAnalysisResultService.updateById(result);
        }
    }

    private List<AiAnalysisResult> listResultsByRequestIds(List<Long> requestIds) {
        if (requestIds == null || requestIds.isEmpty()) {
            return List.of();
        }
        return aiAnalysisResultService.list(new LambdaQueryWrapper<AiAnalysisResult>()
                .in(AiAnalysisResult::getRequestId, requestIds)
                .eq(AiAnalysisResult::getIsDeleted, 0));
    }

    private List<AiRetrievedChunkVO> extractRetrievedChunks(AiAnalysisResult result) {
        JsonNode node = readJsonNode(StringUtils.hasText(result.getRemark()) ? result.getRemark() : result.getResultJson());
        JsonNode chunksNode = node.path("retrievedChunks");
        if (!chunksNode.isArray()) {
            chunksNode = node.path("suggestion").path("retrievedChunks");
        }
        if (!chunksNode.isArray()) {
            return List.of();
        }
        List<AiRetrievedChunkVO> results = new ArrayList<>();
        for (JsonNode chunkNode : chunksNode) {
            AiRetrievedChunkVO vo = new AiRetrievedChunkVO();
            vo.setChunkId(chunkNode.path("chunkId").isNumber() ? chunkNode.path("chunkId").asLong() : null);
            vo.setSourceType(readText(chunkNode, "sourceType", null));
            vo.setSourceId(chunkNode.path("sourceId").isNumber() ? chunkNode.path("sourceId").asLong() : null);
            vo.setTitle(readText(chunkNode, "title", null));
            vo.setSnippet(readText(chunkNode, "snippet", null));
            vo.setScore(chunkNode.path("score").isNumber() ? chunkNode.path("score").asDouble() : null);
            results.add(vo);
        }
        return results;
    }

    private List<ImprovePlanActionRequest> buildActionRequests(JsonNode resultNode,
                                                               AiImproveSuggestionConfirmRequest confirmRequest,
                                                               ImprovePlanSaveRequest saveRequest) {
        JsonNode actionsNode = resultNode.path("actions");
        if (!actionsNode.isArray() || actionsNode.isEmpty()) {
            throw new IllegalStateException("AI improve suggestion actions are empty");
        }
        List<ImprovePlanActionRequest> actionRequests = new ArrayList<>();
        int index = 1;
        for (JsonNode actionNode : actionsNode) {
            ImprovePlanActionRequest actionRequest = new ImprovePlanActionRequest();
            actionRequest.setActionCode("AI-A" + index);
            actionRequest.setActionTitle(readText(actionNode, "actionTitle", "AI Action " + index));
            actionRequest.setActionDesc(readText(actionNode, "actionDesc", "Generated by AI assistant"));
            actionRequest.setResponsibleUserId(confirmRequest.getResponsibleUserId() != null
                    ? confirmRequest.getResponsibleUserId()
                    : saveRequest.getOwnerUserId());
            actionRequest.setStartDate(saveRequest.getStartDate());
            actionRequest.setDueDate(saveRequest.getStartDate().plusDays(Math.max(readInt(actionNode, "dueDaysOffset", 7), 1)));
            actionRequest.setProgressPercent(null);
            actionRequest.setStatus("PENDING");
            actionRequest.setSortNo(index);
            actionRequest.setRemark("Generated by AI assistant");
            actionRequests.add(actionRequest);
            index++;
        }
        return actionRequests;
    }

    private LocalDate resolveImproveDueDate(AiImproveSuggestionConfirmRequest request,
                                            Map<String, Object> metadata,
                                            JsonNode resultNode) {
        if (request.getDueDate() != null) {
            return request.getDueDate();
        }
        int dueDays = readInt(resultNode, "dueDays", asInteger(metadata.get("dueDays")) == null ? 30 : asInteger(metadata.get("dueDays")));
        return (request.getStartDate() == null ? LocalDate.now() : request.getStartDate()).plusDays(Math.max(dueDays, 1));
    }

    private int requestPriority(Map<String, Object> metadata, JsonNode resultNode) {
        Integer metadataPriority = asInteger(metadata.get("priority"));
        return readInt(resultNode, "priority", metadataPriority == null ? 1 : metadataPriority);
    }

    private Long fallbackOwnerUserId(Map<String, Object> metadata, Long confirmedBy) {
        Long ownerUserId = asLong(metadata.get("ownerUserId"));
        if (ownerUserId != null) {
            validateUserId(ownerUserId, "Owner user is invalid");
            return ownerUserId;
        }
        return confirmedBy;
    }

    private void validateImproveGenerateRequest(AiImproveSuggestionGenerateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        validateUserId(request.getRequesterUserId(), "Requester user is invalid");
        validateUserId(request.getOwnerUserId(), "Owner user is invalid");
        if (request.getResponsibleUserId() != null) {
            validateUserId(request.getResponsibleUserId(), "Responsible user is invalid");
        }
        if (!StringUtils.hasText(request.getSourceType()) || request.getSourceId() == null) {
            throw new IllegalArgumentException("Source type and source id are required");
        }
    }

    private void validateUserId(Long userId, String message) {
        SysUser user = userId == null ? null : sysUserService.getById(userId);
        if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() != 0)) {
            throw new IllegalArgumentException(message);
        }
    }

    private String resolveReportScenario(String operationType) {
        return "EXPAND".equals(resolveReportOperation(operationType)) ? SCENARIO_REPORT_EXPAND : SCENARIO_REPORT_POLISH;
    }

    private String resolveReportOperation(String operationType) {
        return "EXPAND".equalsIgnoreCase(operationType) ? "EXPAND" : "POLISH";
    }

    private Map<String, Object> parseJsonMap(String json) {
        if (!StringUtils.hasText(json)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private JsonNode readJsonNode(String json) {
        try {
            return StringUtils.hasText(json) ? objectMapper.readTree(json) : objectMapper.createObjectNode();
        } catch (Exception ex) {
            throw new IllegalStateException("Parse AI result JSON failed", ex);
        }
    }

    private String readText(JsonNode node, String field, String defaultValue) {
        JsonNode valueNode = node.path(field);
        return valueNode.isTextual() ? valueNode.asText() : defaultValue;
    }

    private int readInt(JsonNode node, String field, int defaultValue) {
        JsonNode valueNode = node.path(field);
        return valueNode.isNumber() ? valueNode.asInt() : defaultValue;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Serialize JSON failed", ex);
        }
    }

    private String deepSeekModel() {
        return aiAssistantProperties.getDeepseek() == null ? "UNKNOWN" : aiAssistantProperties.getDeepseek().getModel();
    }

    private String appendApplyTarget(String resultJson, String applyTarget) {
        try {
            JsonNode root = readJsonNode(resultJson);
            Map<String, Object> rootMap = root.isObject()
                    ? objectMapper.convertValue(root, new TypeReference<Map<String, Object>>() {})
                    : new LinkedHashMap<>();
            rootMap.put("appliedTarget", applyTarget);
            return writeJson(rootMap);
        } catch (Exception ex) {
            return writeJson(Map.of("appliedTarget", applyTarget));
        }
    }

    private String defaultText(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            return Long.parseLong(text);
        }
        return null;
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            return Integer.parseInt(text);
        }
        return null;
    }
}
