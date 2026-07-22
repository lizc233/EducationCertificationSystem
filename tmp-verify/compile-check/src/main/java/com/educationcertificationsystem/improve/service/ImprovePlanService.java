package com.educationcertificationsystem.improve.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanActionProgressRequest;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanRecordSaveRequest;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanReminderRequest;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanSaveRequest;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanVerifyRequest;
import com.educationcertificationsystem.model.entity.ImprovePlan;
import com.educationcertificationsystem.model.entity.ImprovePlanAction;
import com.educationcertificationsystem.model.entity.ImprovePlanRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.educationcertificationsystem.model.vo.improve.ImprovePlanDetailVO;
import com.educationcertificationsystem.model.vo.improve.ImprovePlanPageVO;

/**
* @author Lizc233
* @description 针对表【improve_plan(改进计划表)】的数据库操作Service
* @createDate 2026-07-16 14:29:34
*/
public interface ImprovePlanService extends IService<ImprovePlan> {

    Page<ImprovePlanPageVO> pageByCondition(long pageNum,
                                            long pageSize,
                                            String status,
                                            String sourceType,
                                            String targetType,
                                            Long ownerUserId,
                                            Long responsibleUserId,
                                            Integer priority,
                                            Integer overdueOnly,
                                            String keyword);

    ImprovePlanDetailVO getDetail(Long id);

    ImprovePlanDetailVO createPlan(ImprovePlanSaveRequest request);

    ImprovePlanDetailVO updatePlan(Long id, ImprovePlanSaveRequest request);

    void deletePlan(Long id);

    ImprovePlan startPlan(Long id);

    ImprovePlan completePlan(Long id);

    ImprovePlan verifyPlan(Long id, ImprovePlanVerifyRequest request);

    int sendReminder(Long id, ImprovePlanReminderRequest request);

    ImprovePlanAction updateActionProgress(Long actionId, ImprovePlanActionProgressRequest request);

    ImprovePlanRecord addRecord(Long actionId, ImprovePlanRecordSaveRequest request);

    ImprovePlanRecord updateRecord(Long recordId, ImprovePlanRecordSaveRequest request);

    void deleteRecord(Long recordId);
}
