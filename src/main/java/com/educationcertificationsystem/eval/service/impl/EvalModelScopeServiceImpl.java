package com.educationcertificationsystem.eval.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.eval.mapper.EvalModelScopeMapper;
import com.educationcertificationsystem.eval.service.EvalModelScopeService;
import com.educationcertificationsystem.model.entity.EvalModelScope;
import com.educationcertificationsystem.support.EntityAuditSupport;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author Lizc233
* @description 针对表【eval_model_scope(模型适用范围表)】的数据库操作Service实现
* @createDate 2026-07-16 14:29:33
*/
@Service
public class EvalModelScopeServiceImpl extends ServiceImpl<EvalModelScopeMapper, EvalModelScope>
    implements EvalModelScopeService{

    @Override
    public List<EvalModelScope> listActiveByModelId(Long modelId) {
        return baseMapper.selectActiveByModelId(modelId);
    }

    @Override
    @Transactional
    public void replaceModelScopes(Long modelId, List<EvalModelScope> scopes) {
        baseMapper.deleteByModelId(modelId);
        if (scopes == null || scopes.isEmpty()) {
            return;
        }
        for (EvalModelScope scope : scopes) {
            scope.setModelId(modelId);
            EntityAuditSupport.touchCreate(scope);
        }
        saveBatch(scopes);
    }
}




