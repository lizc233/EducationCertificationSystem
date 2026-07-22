package com.educationcertificationsystem.eval.mapper;

import com.educationcertificationsystem.model.entity.EvalGraduationRequirementResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.educationcertificationsystem.model.vo.eval.EvalGraduationRequirementResultPageVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
* @author Lizc233
* @description 针对表【eval_graduation_requirement_result(毕业要求达成结果表)】的数据库操作Mapper
* @createDate 2026-07-16 14:29:33
* @Entity com.educationcertificationsystem.model.entity.EvalGraduationRequirementResult
*/
public interface EvalGraduationRequirementResultMapper extends BaseMapper<EvalGraduationRequirementResult> {

    EvalGraduationRequirementResult selectActiveById(@Param("id") Long id);

    long countByCondition(@Param("programVersionId") Long programVersionId,
                          @Param("majorId") Long majorId,
                          @Param("requirementId") Long requirementId,
                          @Param("modelId") Long modelId,
                          @Param("warningFlag") Integer warningFlag,
                          @Param("lockFlag") Integer lockFlag,
                          @Param("keyword") String keyword);

    List<EvalGraduationRequirementResultPageVO> selectPageByCondition(@Param("offset") long offset,
                                                                      @Param("size") long size,
                                                                      @Param("programVersionId") Long programVersionId,
                                                                      @Param("majorId") Long majorId,
                                                                      @Param("requirementId") Long requirementId,
                                                                      @Param("modelId") Long modelId,
                                                                      @Param("warningFlag") Integer warningFlag,
                                                                      @Param("lockFlag") Integer lockFlag,
                                                                      @Param("keyword") String keyword);

    EvalGraduationRequirementResult selectByUnique(@Param("programVersionId") Long programVersionId,
                                                   @Param("requirementId") Long requirementId,
                                                   @Param("modelId") Long modelId);

}




