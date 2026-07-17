package com.educationcertificationsystem.eval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.educationcertificationsystem.model.entity.EvalModelScope;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
* @author Lizc233
* @description 针对表【eval_model_scope(模型适用范围表)】的数据库操作Mapper
* @createDate 2026-07-16 14:29:33
* @Entity com.educationcertificationsystem.model.entity.EvalModelScope
*/
public interface EvalModelScopeMapper extends BaseMapper<EvalModelScope> {

    List<EvalModelScope> selectActiveByModelId(@Param("modelId") Long modelId);

    int deleteByModelId(@Param("modelId") Long modelId);

}




