package com.educationcertificationsystem.survey.mapper;

import com.educationcertificationsystem.model.vo.survey.SurveyQuestionnairePageVO;
import com.educationcertificationsystem.model.entity.SurveyQuestionnaire;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
* @author Lizc233
* @description 针对表【survey_questionnaire(问卷主表)】的数据库操作Mapper
* @createDate 2026-07-16 14:29:35
* @Entity com.educationcertificationsystem.model.entity.SurveyQuestionnaire
*/
public interface SurveyQuestionnaireMapper extends BaseMapper<SurveyQuestionnaire> {

    long countByCondition(@Param("publishStatus") String publishStatus,
                          @Param("questionnaireType") String questionnaireType,
                          @Param("targetObjectType") String targetObjectType,
                          @Param("keyword") String keyword);

    List<SurveyQuestionnairePageVO> selectPageByCondition(@Param("offset") long offset,
                                                          @Param("size") long size,
                                                          @Param("publishStatus") String publishStatus,
                                                          @Param("questionnaireType") String questionnaireType,
                                                          @Param("targetObjectType") String targetObjectType,
                                                          @Param("keyword") String keyword);
}




