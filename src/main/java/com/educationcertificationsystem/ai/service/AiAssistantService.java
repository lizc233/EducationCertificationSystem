package com.educationcertificationsystem.ai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.model.dto.ai.AiAnalysisRetryRequest;
import com.educationcertificationsystem.model.dto.ai.AiImproveSuggestionConfirmRequest;
import com.educationcertificationsystem.model.dto.ai.AiImproveSuggestionGenerateRequest;
import com.educationcertificationsystem.model.dto.ai.AiReportAssistantConfirmRequest;
import com.educationcertificationsystem.model.dto.ai.AiReportAssistantGenerateRequest;
import com.educationcertificationsystem.model.vo.ai.AiAnalysisHistoryVO;
import com.educationcertificationsystem.model.vo.ai.AiAnalysisResultVO;

public interface AiAssistantService {

    AiAnalysisResultVO generateReportAssistant(Long chapterId, AiReportAssistantGenerateRequest request);

    AiAnalysisResultVO confirmReportAssistant(Long chapterId, AiReportAssistantConfirmRequest request);

    AiAnalysisResultVO generateImproveSuggestion(AiImproveSuggestionGenerateRequest request);

    AiAnalysisResultVO confirmImproveSuggestion(AiImproveSuggestionConfirmRequest request);

    AiAnalysisResultVO retry(Long requestId, AiAnalysisRetryRequest request);

    Page<AiAnalysisHistoryVO> pageHistory(long pageNum,
                                          long pageSize,
                                          String scenarioType,
                                          String sourceType,
                                          Long requesterUserId,
                                          String requestStatus);

    AiAnalysisResultVO getDetail(Long requestId);
}
