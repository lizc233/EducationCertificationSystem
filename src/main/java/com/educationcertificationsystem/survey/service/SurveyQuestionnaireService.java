package com.educationcertificationsystem.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.model.dto.survey.SurveyDispatchRequest;
import com.educationcertificationsystem.model.dto.survey.SurveyPublishEvent;
import com.educationcertificationsystem.model.dto.survey.SurveyQuestionnaireSaveRequest;
import com.educationcertificationsystem.model.entity.SurveyQuestionnaire;
import com.educationcertificationsystem.model.vo.survey.SurveyPublishTaskVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionnaireDetailVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionnairePageVO;

/**
* @author Lizc233
* @description 针对表【survey_questionnaire(问卷主表)】的数据库操作Service
* @createDate 2026-07-16 14:29:35
*/
public interface SurveyQuestionnaireService extends IService<SurveyQuestionnaire> {

    Page<SurveyQuestionnairePageVO> pageByCondition(long pageNum,
                                                    long pageSize,
                                                    String publishStatus,
                                                    String questionnaireType,
                                                    String targetObjectType,
                                                    String keyword);

    SurveyQuestionnaireDetailVO getDetail(Long id);

    SurveyQuestionnaireDetailVO preview(Long id);

    SurveyQuestionnaireDetailVO createQuestionnaire(SurveyQuestionnaireSaveRequest request);

    SurveyQuestionnaireDetailVO updateQuestionnaire(Long id, SurveyQuestionnaireSaveRequest request);

    void deleteQuestionnaire(Long id);

    SurveyQuestionnaire publish(Long id, SurveyDispatchRequest request);

    SurveyQuestionnaire retryPublish(Long id, SurveyDispatchRequest request);

    SurveyQuestionnaire revoke(Long id, String remark);

    SurveyQuestionnaire end(Long id, String remark);

    SurveyQuestionnaire sendDeadlineReminder(Long id, SurveyDispatchRequest request);

    Page<SurveyPublishTaskVO> pagePublishTasks(Long questionnaireId, long pageNum, long pageSize);

    void handlePublishEvent(SurveyPublishEvent event);
}
