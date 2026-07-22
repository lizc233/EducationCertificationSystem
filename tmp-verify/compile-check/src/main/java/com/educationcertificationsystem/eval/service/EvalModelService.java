package com.educationcertificationsystem.eval.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.model.entity.EvalModel;
import com.baomidou.mybatisplus.extension.service.IService;
import com.educationcertificationsystem.model.dto.eval.EvalModelSaveRequest;
import com.educationcertificationsystem.model.vo.eval.EvalModelDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalModelPageItemVO;

/**
* @author Lizc233
* @description 针对表【eval_model(达成度评价模型表)】的数据库操作Service
* @createDate 2026-07-16 14:29:33
*/
public interface EvalModelService extends IService<EvalModel> {

    EvalModel getActiveById(Long id);

    Page<EvalModelPageItemVO> pageByCondition(long pageNum,
                                              long pageSize,
                                              String modelType,
                                              String scopeType,
                                              String status,
                                              Integer enabled,
                                              String keyword);

    EvalModelDetailVO getDetail(Long id);

    EvalModelDetailVO createModel(EvalModelSaveRequest request);

    EvalModelDetailVO updateModel(Long id, EvalModelSaveRequest request);

    void deleteModel(Long id);

    EvalModel updateEnabled(Long id, Integer enabled);

    EvalModel updateStatus(Long id, String status);

}
