package com.educationcertificationsystem.eval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.educationcertificationsystem.model.entity.EvalModel;
import com.educationcertificationsystem.model.vo.eval.EvalModelPageItemVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
* @author Lizc233
* @description 针对表【eval_model(达成度评价模型表)】的数据库操作Mapper
* @createDate 2026-07-16 14:29:33
* @Entity com.educationcertificationsystem.model.entity.EvalModel
*/
public interface EvalModelMapper extends BaseMapper<EvalModel> {

    EvalModel selectActiveById(@Param("id") Long id);

    long countByCondition(@Param("modelType") String modelType,
                          @Param("scopeType") String scopeType,
                          @Param("status") String status,
                          @Param("enabled") Integer enabled,
                          @Param("keyword") String keyword);

    List<EvalModelPageItemVO> selectPageByCondition(@Param("offset") long offset,
                                                    @Param("size") long size,
                                                    @Param("modelType") String modelType,
                                                    @Param("scopeType") String scopeType,
                                                    @Param("status") String status,
                                                    @Param("enabled") Integer enabled,
                                                    @Param("keyword") String keyword);

    long countByModelCode(@Param("modelCode") String modelCode,
                          @Param("excludeId") Long excludeId);

    long countResultReferences(@Param("modelId") Long modelId);

}




