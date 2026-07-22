package com.educationcertificationsystem.eval.mapper;

import com.educationcertificationsystem.model.entity.EvalCourseTargetResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.educationcertificationsystem.model.vo.eval.EvalCourseTargetResultPageVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
* @author Lizc233
* @description 针对表【eval_course_target_result(课程目标达成结果表)】的数据库操作Mapper
* @createDate 2026-07-16 14:29:33
* @Entity com.educationcertificationsystem.model.entity.EvalCourseTargetResult
*/
public interface EvalCourseTargetResultMapper extends BaseMapper<EvalCourseTargetResult> {

    EvalCourseTargetResult selectActiveById(@Param("id") Long id);

    long countByCondition(@Param("taskId") Long taskId,
                          @Param("semesterId") Long semesterId,
                          @Param("courseId") Long courseId,
                          @Param("classId") Long classId,
                          @Param("objectiveId") Long objectiveId,
                          @Param("modelId") Long modelId,
                          @Param("lockedFlag") Integer lockedFlag,
                          @Param("keyword") String keyword);

    List<EvalCourseTargetResultPageVO> selectPageByCondition(@Param("offset") long offset,
                                                             @Param("size") long size,
                                                             @Param("taskId") Long taskId,
                                                             @Param("semesterId") Long semesterId,
                                                             @Param("courseId") Long courseId,
                                                             @Param("classId") Long classId,
                                                             @Param("objectiveId") Long objectiveId,
                                                             @Param("modelId") Long modelId,
                                                             @Param("lockedFlag") Integer lockedFlag,
                                                             @Param("keyword") String keyword);

    EvalCourseTargetResult selectByUnique(@Param("taskId") Long taskId,
                                          @Param("objectiveId") Long objectiveId,
                                          @Param("modelId") Long modelId);

}




