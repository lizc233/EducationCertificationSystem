package com.educationcertificationsystem.eval.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.model.entity.EvalGraduationRequirementResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.educationcertificationsystem.model.dto.eval.EvalGraduationRequirementCalculateRequest;
import com.educationcertificationsystem.model.dto.eval.EvalGraduationWarningNotifyRequest;
import com.educationcertificationsystem.model.vo.eval.EvalGraduationRequirementResultDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalGraduationRequirementResultPageVO;
import java.util.List;

/**
* @author Lizc233
* @description 针对表【eval_graduation_requirement_result(毕业要求达成结果表)】的数据库操作Service
* @createDate 2026-07-16 14:29:33
*/
public interface EvalGraduationRequirementResultService extends IService<EvalGraduationRequirementResult> {

    EvalGraduationRequirementResult getActiveById(Long id);

    Page<EvalGraduationRequirementResultPageVO> pageByCondition(long pageNum,
                                                                long pageSize,
                                                                Long programVersionId,
                                                                Long majorId,
                                                                Long requirementId,
                                                                Long modelId,
                                                                Integer warningFlag,
                                                                Integer lockFlag,
                                                                String keyword);

    EvalGraduationRequirementResultDetailVO getDetail(Long id);

    List<EvalGraduationRequirementResult> calculate(EvalGraduationRequirementCalculateRequest request);

    EvalGraduationRequirementResult recalculate(Long resultId, String remark);

    EvalGraduationRequirementResult confirm(Long resultId);

    EvalGraduationRequirementResult closeWarning(Long resultId, String remark);

    int notifyWarnings(EvalGraduationWarningNotifyRequest request);

}
