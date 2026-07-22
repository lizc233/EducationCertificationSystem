package com.educationcertificationsystem.eval.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.model.entity.EvalResultDetail;
import com.educationcertificationsystem.eval.service.EvalResultDetailService;
import com.educationcertificationsystem.eval.mapper.EvalResultDetailMapper;
import com.educationcertificationsystem.support.EntityAuditSupport;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author Lizc233
* @description 针对表【eval_result_detail(达成度明细表)】的数据库操作Service实现
* @createDate 2026-07-16 14:29:33
*/
@Service
public class EvalResultDetailServiceImpl extends ServiceImpl<EvalResultDetailMapper, EvalResultDetail>
    implements EvalResultDetailService{

    @Override
    public List<EvalResultDetail> listByResult(String resultType, Long resultId) {
        return baseMapper.selectByResult(resultType, resultId);
    }

    @Override
    @Transactional
    public void replaceDetails(String resultType, Long resultId, List<EvalResultDetail> details) {
        baseMapper.deleteByResult(resultType, resultId);
        if (details == null || details.isEmpty()) {
            return;
        }
        for (EvalResultDetail detail : details) {
            detail.setResultType(resultType);
            detail.setResultId(resultId);
            EntityAuditSupport.touchCreate(detail);
        }
        saveBatch(details);
    }
}




