package com.educationcertificationsystem.eval.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.eval.mapper.EvalModelItemMapper;
import com.educationcertificationsystem.eval.service.EvalModelItemService;
import com.educationcertificationsystem.model.entity.EvalModelItem;
import com.educationcertificationsystem.support.EntityAuditSupport;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author Lizc233
* @description 针对表【eval_model_item(模型明细项表)】的数据库操作Service实现
* @createDate 2026-07-16 14:29:33
*/
@Service
public class EvalModelItemServiceImpl extends ServiceImpl<EvalModelItemMapper, EvalModelItem>
    implements EvalModelItemService{

    @Override
    public List<EvalModelItem> listActiveByModelId(Long modelId) {
        return baseMapper.selectActiveByModelId(modelId);
    }

    @Override
    @Transactional
    public void replaceModelItems(Long modelId, List<EvalModelItem> items) {
        baseMapper.deleteByModelId(modelId);
        if (items == null || items.isEmpty()) {
            return;
        }
        for (EvalModelItem item : items) {
            item.setModelId(modelId);
            EntityAuditSupport.touchCreate(item);
        }
        saveBatch(items);
    }
}




