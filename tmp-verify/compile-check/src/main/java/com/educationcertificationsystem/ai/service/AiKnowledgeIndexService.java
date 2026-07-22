package com.educationcertificationsystem.ai.service;

import com.educationcertificationsystem.model.dto.ai.AiKnowledgeRebuildRequest;
import com.educationcertificationsystem.model.vo.ai.AiKnowledgeRebuildVO;

public interface AiKnowledgeIndexService {

    AiKnowledgeRebuildVO rebuild(AiKnowledgeRebuildRequest request);

    void rebuildReportProjectKnowledge(Long projectId);

    void rebuildImprovePlanKnowledge(Long planId);
}
