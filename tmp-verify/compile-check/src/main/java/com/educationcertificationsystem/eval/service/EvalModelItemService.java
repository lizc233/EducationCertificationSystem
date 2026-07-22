package com.educationcertificationsystem.eval.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.educationcertificationsystem.model.entity.EvalModelItem;
import java.util.List;

/**
* @author Lizc233
* @description 针对表【eval_model_item(模型明细项表)】的数据库操作Service
* @createDate 2026-07-16 14:29:33
*/
public interface EvalModelItemService extends IService<EvalModelItem> {

    List<EvalModelItem> listActiveByModelId(Long modelId);

    void replaceModelItems(Long modelId, List<EvalModelItem> items);

}
