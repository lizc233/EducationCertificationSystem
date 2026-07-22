package com.educationcertificationsystem.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.educationcertificationsystem.ai.service.AiKnowledgeChunkService;
import com.educationcertificationsystem.ai.service.AiKnowledgeDocumentService;
import com.educationcertificationsystem.ai.service.AiKnowledgeIndexService;
import com.educationcertificationsystem.improve.service.ImprovePlanActionService;
import com.educationcertificationsystem.improve.service.ImprovePlanService;
import com.educationcertificationsystem.model.dto.ai.AiKnowledgeRebuildRequest;
import com.educationcertificationsystem.model.entity.AiKnowledgeChunk;
import com.educationcertificationsystem.model.entity.AiKnowledgeDocument;
import com.educationcertificationsystem.model.entity.ImprovePlan;
import com.educationcertificationsystem.model.entity.ImprovePlanAction;
import com.educationcertificationsystem.model.entity.ReportChapter;
import com.educationcertificationsystem.model.vo.ai.AiKnowledgeRebuildVO;
import com.educationcertificationsystem.report.service.ReportChapterService;
import com.educationcertificationsystem.support.EntityAuditSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiKnowledgeIndexServiceImpl implements AiKnowledgeIndexService {

    private static final String STATUS_SUCCESS = "SUCCESS";

    private final AiKnowledgeDocumentService aiKnowledgeDocumentService;
    private final AiKnowledgeChunkService aiKnowledgeChunkService;
    private final ReportChapterService reportChapterService;
    private final ImprovePlanService improvePlanService;
    private final ImprovePlanActionService improvePlanActionService;
    private final QdrantVectorStoreService qdrantVectorStoreService;
    private final LocalHashEmbeddingService localHashEmbeddingService;
    private final ObjectMapper objectMapper;

    @Override
    public AiKnowledgeRebuildVO rebuild(AiKnowledgeRebuildRequest request) {
        String rebuildType = request == null || !StringUtils.hasText(request.getRebuildType())
                ? "ALL"
                : request.getRebuildType().trim().toUpperCase();
        if ("REPORT_PROJECT".equals(rebuildType)) {
            if (request.getProjectId() == null) {
                throw new IllegalArgumentException("Project id is required");
            }
            rebuildReportProjectKnowledge(request.getProjectId());
        } else if ("IMPROVE_PLAN".equals(rebuildType)) {
            if (request.getPlanId() == null) {
                throw new IllegalArgumentException("Plan id is required");
            }
            rebuildImprovePlanKnowledge(request.getPlanId());
        } else {
            rebuildAll();
        }
        AiKnowledgeRebuildVO vo = new AiKnowledgeRebuildVO();
        vo.setRebuildType(rebuildType);
        vo.setDocumentCount((int) aiKnowledgeDocumentService.count(new LambdaQueryWrapper<AiKnowledgeDocument>()
                .eq(AiKnowledgeDocument::getIsDeleted, 0)));
        vo.setChunkCount((int) aiKnowledgeChunkService.count(new LambdaQueryWrapper<AiKnowledgeChunk>()
                .eq(AiKnowledgeChunk::getIsDeleted, 0)));
        return vo;
    }

    @Override
    public void rebuildReportProjectKnowledge(Long projectId) {
        List<ReportChapter> chapters = reportChapterService.list(new LambdaQueryWrapper<ReportChapter>()
                .eq(ReportChapter::getProjectId, projectId)
                .eq(ReportChapter::getIsDeleted, 0)
                .orderByAsc(ReportChapter::getSortNo)
                .orderByAsc(ReportChapter::getId));
        for (ReportChapter chapter : chapters) {
            upsertReportChapterDocument(chapter);
        }
    }

    @Override
    public void rebuildImprovePlanKnowledge(Long planId) {
        ImprovePlan plan = improvePlanService.getById(planId);
        if (plan == null || (plan.getIsDeleted() != null && plan.getIsDeleted() != 0)) {
            return;
        }
        upsertImprovePlanDocument(plan);
    }

    private void rebuildAll() {
        List<ReportChapter> chapters = reportChapterService.list(new LambdaQueryWrapper<ReportChapter>()
                .eq(ReportChapter::getIsDeleted, 0)
                .orderByAsc(ReportChapter::getProjectId)
                .orderByAsc(ReportChapter::getSortNo)
                .orderByAsc(ReportChapter::getId));
        for (ReportChapter chapter : chapters) {
            upsertReportChapterDocument(chapter);
        }
        List<ImprovePlan> plans = improvePlanService.list(new LambdaQueryWrapper<ImprovePlan>()
                .eq(ImprovePlan::getIsDeleted, 0)
                .orderByDesc(ImprovePlan::getUpdatedAt)
                .orderByDesc(ImprovePlan::getId));
        for (ImprovePlan plan : plans) {
            upsertImprovePlanDocument(plan);
        }
    }

    private void upsertReportChapterDocument(ReportChapter chapter) {
        String content = buildReportChapterContent(chapter);
        String checksum = sha256(content);
        AiKnowledgeDocument document = getOrCreateDocument("REPORT_CHAPTER", chapter.getId());
        if (Objects.equals(document.getContentChecksum(), checksum) && STATUS_SUCCESS.equals(document.getIndexStatus())) {
            return;
        }
        document.setTitle(chapter.getChapterCode() + " " + chapter.getChapterTitle());
        document.setSummary(shortSummary(content));
        document.setBizScope("REPORT_PROJECT:" + chapter.getProjectId());
        document.setContentChecksum(checksum);
        document.setIndexStatus(STATUS_SUCCESS);
        document.setLastIndexedAt(LocalDateTime.now());
        document.setVersionNo(document.getVersionNo() == null ? 1 : document.getVersionNo() + 1);
        saveDocument(document);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("bizType", "REPORT_CHAPTER");
        metadata.put("bizId", chapter.getId());
        metadata.put("projectId", chapter.getProjectId());
        metadata.put("sourceType", chapter.getSourceType());
        metadata.put("sourceId", chapter.getSourceRefId());
        metadata.put("title", document.getTitle());
        reindexChunks(document, content, metadata);
    }

    private void upsertImprovePlanDocument(ImprovePlan plan) {
        String content = buildImprovePlanContent(plan);
        String checksum = sha256(content);
        AiKnowledgeDocument document = getOrCreateDocument("IMPROVE_PLAN", plan.getId());
        if (Objects.equals(document.getContentChecksum(), checksum) && STATUS_SUCCESS.equals(document.getIndexStatus())) {
            return;
        }
        document.setTitle(plan.getPlanCode() + " " + plan.getPlanName());
        document.setSummary(shortSummary(content));
        document.setBizScope("IMPROVE_SOURCE:" + defaultText(plan.getSourceType()) + ":" + plan.getSourceId());
        document.setContentChecksum(checksum);
        document.setIndexStatus(STATUS_SUCCESS);
        document.setLastIndexedAt(LocalDateTime.now());
        document.setVersionNo(document.getVersionNo() == null ? 1 : document.getVersionNo() + 1);
        saveDocument(document);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("bizType", "IMPROVE_PLAN");
        metadata.put("bizId", plan.getId());
        metadata.put("sourceType", plan.getSourceType());
        metadata.put("sourceId", plan.getSourceId());
        metadata.put("targetType", plan.getTargetType());
        metadata.put("targetId", plan.getTargetId());
        metadata.put("title", document.getTitle());
        reindexChunks(document, content, metadata);
    }

    private void reindexChunks(AiKnowledgeDocument document, String content, Map<String, Object> metadata) {
        List<AiKnowledgeChunk> existingChunks = aiKnowledgeChunkService.list(new LambdaQueryWrapper<AiKnowledgeChunk>()
                .eq(AiKnowledgeChunk::getDocumentId, document.getId())
                .eq(AiKnowledgeChunk::getIsDeleted, 0)
                .orderByAsc(AiKnowledgeChunk::getChunkNo));
        List<String> oldPointIds = existingChunks.stream()
                .map(AiKnowledgeChunk::getQdrantPointId)
                .filter(StringUtils::hasText)
                .toList();
        if (!oldPointIds.isEmpty()) {
            qdrantVectorStoreService.deletePoints(oldPointIds);
        }
        if (!existingChunks.isEmpty()) {
            existingChunks.forEach(EntityAuditSupport::touchDelete);
            aiKnowledgeChunkService.updateBatchById(existingChunks);
        }

        List<String> chunks = splitIntoChunks(content);
        qdrantVectorStoreService.ensureCollection(localHashEmbeddingService.embed(content).size());
        int chunkNo = 1;
        for (String chunkText : chunks) {
            AiKnowledgeChunk chunk = new AiKnowledgeChunk();
            chunk.setDocumentId(document.getId());
            chunk.setChunkNo(chunkNo);
            chunk.setChunkText(chunkText);
            chunk.setMetadataJson(writeJson(metadata));
            chunk.setQdrantPointId("k-" + document.getId() + "-" + chunkNo);
            chunk.setEmbeddingModel("LOCAL_HASH");
            chunk.setIndexStatus(STATUS_SUCCESS);
            chunk.setLastIndexedAt(LocalDateTime.now());
            EntityAuditSupport.touchCreate(chunk);
            aiKnowledgeChunkService.save(chunk);

            Map<String, Object> payload = new LinkedHashMap<>(metadata);
            payload.put("chunkId", chunk.getId());
            payload.put("chunkNo", chunkNo);
            payload.put("documentId", document.getId());
            payload.put("snippet", shortSummary(chunkText));
            qdrantVectorStoreService.upsertPoint(
                    chunk.getQdrantPointId(),
                    localHashEmbeddingService.embed(chunkText),
                    payload);
            chunkNo++;
        }
    }

    private AiKnowledgeDocument getOrCreateDocument(String sourceType, Long sourceId) {
        AiKnowledgeDocument document = aiKnowledgeDocumentService.getOne(new LambdaQueryWrapper<AiKnowledgeDocument>()
                .eq(AiKnowledgeDocument::getSourceType, sourceType)
                .eq(AiKnowledgeDocument::getSourceId, sourceId)
                .eq(AiKnowledgeDocument::getIsDeleted, 0), false);
        if (document != null) {
            return document;
        }
        document = new AiKnowledgeDocument();
        document.setSourceType(sourceType);
        document.setSourceId(sourceId);
        document.setVersionNo(0);
        EntityAuditSupport.touchCreate(document);
        return document;
    }

    private void saveDocument(AiKnowledgeDocument document) {
        if (document.getId() == null) {
            aiKnowledgeDocumentService.save(document);
        } else {
            EntityAuditSupport.touchUpdate(document);
            aiKnowledgeDocumentService.updateById(document);
        }
    }

    private String buildReportChapterContent(ReportChapter chapter) {
        StringBuilder builder = new StringBuilder();
        builder.append("Chapter: ").append(chapter.getChapterCode()).append(' ').append(chapter.getChapterTitle()).append('\n');
        if (StringUtils.hasText(chapter.getSourceType())) {
            builder.append("Source type: ").append(chapter.getSourceType()).append('\n');
        }
        if (chapter.getSourceRefId() != null) {
            builder.append("Source id: ").append(chapter.getSourceRefId()).append('\n');
        }
        if (StringUtils.hasText(chapter.getContentText())) {
            builder.append("Content:\n").append(chapter.getContentText()).append('\n');
        }
        if (StringUtils.hasText(chapter.getRemark())) {
            builder.append("Remark: ").append(chapter.getRemark()).append('\n');
        }
        return builder.toString().trim();
    }

    private String buildImprovePlanContent(ImprovePlan plan) {
        StringBuilder builder = new StringBuilder();
        builder.append("Plan: ").append(plan.getPlanCode()).append(' ').append(plan.getPlanName()).append('\n');
        builder.append("Source: ").append(defaultText(plan.getSourceType())).append('#').append(plan.getSourceId()).append('\n');
        builder.append("Target: ").append(defaultText(plan.getTargetType())).append('#').append(plan.getTargetId()).append('\n');
        builder.append("Status: ").append(defaultText(plan.getStatus())).append('\n');
        if (StringUtils.hasText(plan.getRemark())) {
            builder.append("Remark: ").append(plan.getRemark()).append('\n');
        }
        List<ImprovePlanAction> actions = improvePlanActionService.list(new LambdaQueryWrapper<ImprovePlanAction>()
                .eq(ImprovePlanAction::getPlanId, plan.getId())
                .eq(ImprovePlanAction::getIsDeleted, 0)
                .orderByAsc(ImprovePlanAction::getSortNo)
                .orderByAsc(ImprovePlanAction::getId));
        for (ImprovePlanAction action : actions) {
            builder.append("Action ").append(action.getSortNo()).append(": ")
                    .append(action.getActionTitle()).append('\n')
                    .append(defaultText(action.getActionDesc())).append('\n');
        }
        return builder.toString().trim();
    }

    private List<String> splitIntoChunks(String content) {
        String normalized = StringUtils.hasText(content) ? content : "Empty content";
        String[] parts = normalized.split("\\r?\\n\\s*\\r?\\n");
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String part : parts) {
            String block = StringUtils.hasText(part) ? part.trim() : "";
            if (!StringUtils.hasText(block)) {
                continue;
            }
            if (current.length() + block.length() > 500 && current.length() > 0) {
                chunks.add(current.toString().trim());
                current.setLength(0);
            }
            current.append(block).append('\n');
        }
        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }
        if (chunks.isEmpty()) {
            chunks.add(normalized);
        }
        return chunks;
    }

    private String shortSummary(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 160 ? normalized : normalized.substring(0, 160);
    }

    private String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Calculate checksum failed", ex);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Serialize metadata failed", ex);
        }
    }

    private String defaultText(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }
}
