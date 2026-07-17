package com.educationcertificationsystem.survey.mapper;

import com.educationcertificationsystem.model.vo.survey.SurveyResponsePageVO;
import com.educationcertificationsystem.model.entity.SurveyResponse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
* @author Lizc233
* @description 针对表【survey_response(问卷作答表)】的数据库操作Mapper
* @createDate 2026-07-16 14:29:35
* @Entity com.educationcertificationsystem.model.entity.SurveyResponse
*/
public interface SurveyResponseMapper extends BaseMapper<SurveyResponse> {

    long countByCondition(@Param("questionnaireId") Long questionnaireId,
                          @Param("respondentType") String respondentType,
                          @Param("submitStatus") String submitStatus,
                          @Param("keyword") String keyword);

    List<SurveyResponsePageVO> selectPageByCondition(@Param("offset") long offset,
                                                     @Param("size") long size,
                                                     @Param("questionnaireId") Long questionnaireId,
                                                     @Param("respondentType") String respondentType,
                                                     @Param("submitStatus") String submitStatus,
                                                     @Param("keyword") String keyword);
}




