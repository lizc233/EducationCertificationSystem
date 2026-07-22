package com.educationcertificationsystem.eval.mapper;

import com.educationcertificationsystem.model.entity.EvalRecalcJob;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author Lizc233
* @description 针对表【eval_recalc_job(重算任务表)】的数据库操作Mapper
* @createDate 2026-07-16 14:29:33
* @Entity com.educationcertificationsystem.model.entity.EvalRecalcJob
*/
public interface EvalRecalcJobMapper extends BaseMapper<EvalRecalcJob> {

    EvalRecalcJob selectActiveById(@Param("id") Long id);

}




