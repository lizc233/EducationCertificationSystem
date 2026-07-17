package com.educationcertificationsystem.eval.service;

import com.educationcertificationsystem.model.entity.EvalResultDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
* @author Lizc233
* @description 针对表【eval_result_detail(达成度明细表)】的数据库操作Service
* @createDate 2026-07-16 14:29:33
*/
public interface EvalResultDetailService extends IService<EvalResultDetail> {

    List<EvalResultDetail> listByResult(String resultType, Long resultId);

    void replaceDetails(String resultType, Long resultId, List<EvalResultDetail> details);

}
