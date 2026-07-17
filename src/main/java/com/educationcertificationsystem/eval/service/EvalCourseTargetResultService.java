package com.educationcertificationsystem.eval.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.model.entity.EvalCourseTargetResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.educationcertificationsystem.model.dto.eval.EvalCourseTargetCalculateRequest;
import com.educationcertificationsystem.model.vo.eval.EvalCourseTargetResultDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalCourseTargetResultPageVO;
import java.util.List;

/**
* @author Lizc233
* @description 针对表【eval_course_target_result(课程目标达成结果表)】的数据库操作Service
* @createDate 2026-07-16 14:29:33
*/
public interface EvalCourseTargetResultService extends IService<EvalCourseTargetResult> {

    EvalCourseTargetResult getActiveById(Long id);

    Page<EvalCourseTargetResultPageVO> pageByCondition(long pageNum,
                                                       long pageSize,
                                                       Long taskId,
                                                       Long semesterId,
                                                       Long classId,
                                                       Long objectiveId,
                                                       Long modelId,
                                                       Integer lockedFlag,
                                                       String keyword);

    EvalCourseTargetResultDetailVO getDetail(Long id);

    List<EvalCourseTargetResult> calculate(EvalCourseTargetCalculateRequest request);

    EvalCourseTargetResult recalculate(Long resultId, String remark);

    EvalCourseTargetResult confirm(Long resultId);

}
