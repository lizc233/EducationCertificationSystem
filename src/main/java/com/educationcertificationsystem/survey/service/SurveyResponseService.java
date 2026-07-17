package com.educationcertificationsystem.survey.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.model.dto.survey.SurveySubmitRequest;
import com.educationcertificationsystem.model.entity.SurveyResponse;
import com.baomidou.mybatisplus.extension.service.IService;
import com.educationcertificationsystem.model.vo.survey.SurveyFillVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionStatsVO;
import com.educationcertificationsystem.model.vo.survey.SurveyResponseDetailVO;
import com.educationcertificationsystem.model.vo.survey.SurveyResponseOverviewVO;
import com.educationcertificationsystem.model.vo.survey.SurveyResponsePageVO;
import java.util.LinkedHashMap;
import java.util.List;

/**
* @author Lizc233
* @description 针对表【survey_response(问卷作答表)】的数据库操作Service
* @createDate 2026-07-16 14:29:35
*/
public interface SurveyResponseService extends IService<SurveyResponse> {

    SurveyFillVO getFillView(Long questionnaireId, Long respondentUserId);

    SurveyResponse submitResponse(Long questionnaireId, SurveySubmitRequest request);

    Page<SurveyResponsePageVO> pageByCondition(Long questionnaireId,
                                               long pageNum,
                                               long pageSize,
                                               String respondentType,
                                               String keyword);

    SurveyResponseDetailVO getDetail(Long questionnaireId, Long responseId);

    SurveyResponseOverviewVO getOverview(Long questionnaireId);

    List<SurveyQuestionStatsVO> getQuestionStats(Long questionnaireId);

    List<SurveyResponseDetailVO> exportResponses(Long questionnaireId);

    List<LinkedHashMap<String, Object>> buildExportRows(Long questionnaireId, List<SurveyResponseDetailVO> details);
}
