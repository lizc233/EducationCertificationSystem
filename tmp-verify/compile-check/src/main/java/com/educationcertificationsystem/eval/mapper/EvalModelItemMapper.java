package com.educationcertificationsystem.eval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.educationcertificationsystem.model.entity.EvalModelItem;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
* @author Lizc233
* @description 针对表【eval_model_item(模型明细项表)】的数据库操作Mapper
* @createDate 2026-07-16 14:29:33
* @Entity com.educationcertificationsystem.model.entity.EvalModelItem
*/
public interface EvalModelItemMapper extends BaseMapper<EvalModelItem> {

    List<EvalModelItem> selectActiveByModelId(@Param("modelId") Long modelId);

    int deleteByModelId(@Param("modelId") Long modelId);

}




