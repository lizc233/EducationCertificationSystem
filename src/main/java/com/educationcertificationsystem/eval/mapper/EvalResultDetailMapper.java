package com.educationcertificationsystem.eval.mapper;

import com.educationcertificationsystem.model.entity.EvalResultDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
* @author Lizc233
* @description 针对表【eval_result_detail(达成度明细表)】的数据库操作Mapper
* @createDate 2026-07-16 14:29:33
* @Entity com.educationcertificationsystem.model.entity.EvalResultDetail
*/
public interface EvalResultDetailMapper extends BaseMapper<EvalResultDetail> {

    List<EvalResultDetail> selectByResult(@Param("resultType") String resultType,
                                          @Param("resultId") Long resultId);

    int deleteByResult(@Param("resultType") String resultType,
                       @Param("resultId") Long resultId);

}




