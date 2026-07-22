package com.educationcertificationsystem.improve.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.course.service.EduCourseService;
import com.educationcertificationsystem.file.service.FileStorageService;
import com.educationcertificationsystem.file.service.SysFileService;
import com.educationcertificationsystem.model.entity.ImprovePlan;
import com.educationcertificationsystem.model.entity.ImprovePlanAction;
import com.educationcertificationsystem.model.entity.ImprovePlanRecord;
import com.educationcertificationsystem.model.entity.SurveyQuestionnaire;
import com.educationcertificationsystem.model.entity.SysFile;
import com.educationcertificationsystem.model.entity.SysUser;
import com.educationcertificationsystem.model.entity.TrGraduationRequirement;
import com.educationcertificationsystem.model.entity.TrProgramTarget;
import com.educationcertificationsystem.improve.service.ImprovePlanService;
import com.educationcertificationsystem.improve.mapper.ImprovePlanMapper;
import com.educationcertificationsystem.improve.service.ImprovePlanActionService;
import com.educationcertificationsystem.improve.service.ImprovePlanRecordService;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanActionProgressRequest;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanActionRequest;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanRecordSaveRequest;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanReminderRequest;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanSaveRequest;
import com.educationcertificationsystem.model.dto.improve.ImprovePlanVerifyRequest;
import com.educationcertificationsystem.model.dto.notice.NoticeSendRequest;
import com.educationcertificationsystem.model.vo.improve.ImprovePlanActionVO;
import com.educationcertificationsystem.model.vo.improve.ImprovePlanDetailVO;
import com.educationcertificationsystem.model.vo.improve.ImprovePlanPageVO;
import com.educationcertificationsystem.model.vo.improve.ImprovePlanRecordVO;
import com.educationcertificationsystem.notice.service.NoticeMessageService;
import com.educationcertificationsystem.program.service.TrGraduationRequirementService;
import com.educationcertificationsystem.program.service.TrProgramTargetService;
import com.educationcertificationsystem.support.EntityAuditSupport;
import com.educationcertificationsystem.survey.service.SurveyQuestionnaireService;
import com.educationcertificationsystem.user.service.SysUserService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
* @author Lizc233
* @description 针对表【improve_plan(改进计划表)】的数据库操作Service实现
* @createDate 2026-07-16 14:29:34
*/
@Service
@RequiredArgsConstructor
public class ImprovePlanServiceImpl extends ServiceImpl<ImprovePlanMapper, ImprovePlan>
    implements ImprovePlanService {

    private static final String PLAN_PENDING = "PENDING";
    private static final String PLAN_IN_PROGRESS = "IN_PROGRESS";
    private static final String PLAN_COMPLETED = "COMPLETED";
    private static final String PLAN_VERIFIED = "VERIFIED";

    private static final String ACTION_PENDING = "PENDING";
    private static final String ACTION_IN_PROGRESS = "IN_PROGRESS";
    private static final String ACTION_COMPLETED = "COMPLETED";
    private static final String ACTION_VERIFIED = "VERIFIED";

    private final ImprovePlanActionService improvePlanActionService;
    private final ImprovePlanRecordService improvePlanRecordService;
    private final SysUserService sysUserService;
    private final SysFileService sysFileService;
    private final FileStorageService fileStorageService;
    private final NoticeMessageService noticeMessageService;
    private final TrProgramTargetService trProgramTargetService;
    private final TrGraduationRequirementService trGraduationRequirementService;
    private final EduCourseService eduCourseService;
    private final SurveyQuestionnaireService surveyQuestionnaireService;

    @Override
    public Page<ImprovePlanPageVO> pageByCondition(long pageNum,
                                                   long pageSize,
                                                   String status,
                                                   String sourceType,
                                                   String targetType,
                                                   Long ownerUserId,
                                                   Long responsibleUserId,
                                                   Integer priority,
                                                   Integer overdueOnly,
                                                   String keyword) {
        long current = Math.max(pageNum, 1L);
        long size = Math.max(pageSize, 1L);
        long offset = (current - 1) * size;
        long total = baseMapper.countByCondition(normalizeEnum(status), normalizeEnum(sourceType), normalizeEnum(targetType),
                ownerUserId, responsibleUserId, priority, overdueOnly, normalizeText(keyword));
        List<ImprovePlanPageVO> records = total == 0
                ? List.of()
                : baseMapper.selectPageByCondition(offset, size, normalizeEnum(status), normalizeEnum(sourceType),
                normalizeEnum(targetType), ownerUserId, responsibleUserId, priority, overdueOnly, normalizeText(keyword));
        Page<ImprovePlanPageVO> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    @Override
    public ImprovePlanDetailVO getDetail(Long id) {
        ImprovePlan plan = getActivePlan(id);
        return plan == null ? null : buildDetail(plan);
    }

    @Override
    @Transactional
    public ImprovePlanDetailVO createPlan(ImprovePlanSaveRequest request) {
        validatePlanRequest(request, null);
        releaseDeletedPlanCodes(request.getPlanCode().trim(), null);
        ImprovePlan plan = new ImprovePlan();
        applyPlanFields(plan, request);
        plan.setStatus(PLAN_PENDING);
        plan.setEffectReview(null);
        plan.setClosedAt(null);
        EntityAuditSupport.touchCreate(plan);
        save(plan);
        replaceActions(plan.getId(), request.getActions());
        syncPlanStatusFromActions(plan.getId(), false);
        return getDetail(plan.getId());
    }

    @Override
    @Transactional
    public ImprovePlanDetailVO updatePlan(Long id, ImprovePlanSaveRequest request) {
        ImprovePlan plan = getRequiredPlan(id);
        ensureEditable(plan);
        validatePlanRequest(request, id);
        releaseDeletedPlanCodes(request.getPlanCode().trim(), id);
        applyPlanFields(plan, request);
        EntityAuditSupport.touchUpdate(plan);
        updateById(plan);
        replaceActions(id, request.getActions());
        syncPlanStatusFromActions(id, false);
        return getDetail(id);
    }

    @Override
    @Transactional
    public void deletePlan(Long id) {
        ImprovePlan plan = getRequiredPlan(id);
        ensureEditable(plan);
        plan.setPlanCode(releaseUniqueCode(plan.getPlanCode(), plan.getId()));
        EntityAuditSupport.touchDelete(plan);
        updateById(plan);
        List<ImprovePlanAction> actions = listActiveActions(id);
        if (!actions.isEmpty()) {
            markRecordsDeleted(actions.stream().map(ImprovePlanAction::getId).toList());
            actions.forEach(action -> {
                action.setActionCode(releaseUniqueCode(action.getActionCode(), action.getId()));
                EntityAuditSupport.touchDelete(action);
            });
            improvePlanActionService.updateBatchById(actions);
        }
    }

    @Override
    @Transactional
    public ImprovePlan startPlan(Long id) {
        ImprovePlan plan = getRequiredPlan(id);
        if (PLAN_VERIFIED.equals(plan.getStatus())) {
            throw new IllegalStateException("Verified plan cannot be restarted");
        }
        plan.setStatus(PLAN_IN_PROGRESS);
        plan.setClosedAt(null);
        EntityAuditSupport.touchUpdate(plan);
        updateById(plan);
        return plan;
    }

    @Override
    @Transactional
    public ImprovePlan completePlan(Long id) {
        ImprovePlan plan = getRequiredPlan(id);
        List<ImprovePlanAction> actions = listActiveActions(id);
        if (actions.isEmpty()) {
            throw new IllegalStateException("Plan without actions cannot be completed");
        }
        boolean allDone = actions.stream().allMatch(this::isActionDone);
        if (!allDone) {
            throw new IllegalStateException("All actions must be completed before plan completion");
        }
        plan.setStatus(PLAN_COMPLETED);
        plan.setClosedAt(LocalDateTime.now());
        EntityAuditSupport.touchUpdate(plan);
        updateById(plan);
        return plan;
    }

    @Override
    @Transactional
    public ImprovePlan verifyPlan(Long id, ImprovePlanVerifyRequest request) {
        ImprovePlan plan = getRequiredPlan(id);
        if (request == null || !StringUtils.hasText(request.getEffectReview())) {
            throw new IllegalArgumentException("Effect review is required");
        }
        List<ImprovePlanAction> actions = listActiveActions(id);
        boolean allDone = actions.stream().allMatch(this::isActionDone);
        if (!allDone) {
            throw new IllegalStateException("All actions must be completed before verification");
        }
        plan.setStatus(PLAN_VERIFIED);
        plan.setEffectReview(request.getEffectReview().trim());
        if (StringUtils.hasText(request.getRemark())) {
            plan.setRemark(request.getRemark().trim());
        }
        plan.setClosedAt(LocalDateTime.now());
        EntityAuditSupport.touchUpdate(plan);
        updateById(plan);
        return plan;
    }

    @Override
    @Transactional
    public int sendReminder(Long id, ImprovePlanReminderRequest request) {
        ImprovePlan plan = getRequiredPlan(id);
        List<ImprovePlanAction> actions = listActiveActions(id).stream()
                .filter(action -> !isActionDone(action))
                .filter(action -> action.getDueDate() != null && !action.getDueDate().isAfter(LocalDate.now().plusDays(3)))
                .toList();
        if (actions.isEmpty()) {
            throw new IllegalStateException("No overdue or upcoming actions need reminder");
        }
        LinkedHashSet<Long> recipientUserIds = new LinkedHashSet<>();
        if (plan.getOwnerUserId() != null) {
            recipientUserIds.add(plan.getOwnerUserId());
        }
        for (ImprovePlanAction action : actions) {
            if (action.getResponsibleUserId() != null) {
                recipientUserIds.add(action.getResponsibleUserId());
            }
        }
        List<Long> validRecipientIds = recipientUserIds.stream()
                .filter(this::isActiveUserId)
                .toList();
        if (validRecipientIds.isEmpty()) {
            throw new IllegalStateException("No valid reminder recipients found");
        }
        NoticeSendRequest noticeRequest = new NoticeSendRequest();
        noticeRequest.setNoticeType("IMPROVE_PLAN_REMINDER");
        noticeRequest.setTitle("Improve plan reminder: " + plan.getPlanName());
        noticeRequest.setContent(buildReminderContent(plan, actions));
        noticeRequest.setSenderUserId(request == null ? null : request.getOperatorUserId());
        noticeRequest.setBizType("IMPROVE_PLAN");
        noticeRequest.setBizId(plan.getId());
        noticeRequest.setChannelType("INTERNAL");
        noticeRequest.setPriorityLevel(1);
        noticeRequest.setSendAt(LocalDateTime.now());
        noticeRequest.setRemark(request == null ? null : normalizeText(request.getRemark()));
        noticeRequest.setRecipientUserIds(validRecipientIds);
        noticeRequest.setOperatorUserId(request == null ? null : request.getOperatorUserId());
        noticeMessageService.sendNotice(noticeRequest);
        return validRecipientIds.size();
    }

    @Override
    @Transactional
    public ImprovePlanAction updateActionProgress(Long actionId, ImprovePlanActionProgressRequest request) {
        ImprovePlanAction action = getRequiredAction(actionId);
        ImprovePlan plan = getRequiredPlan(action.getPlanId());
        if (PLAN_VERIFIED.equals(plan.getStatus())) {
            throw new IllegalStateException("Verified plan cannot update actions");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getProgressPercent() != null) {
            validateProgress(request.getProgressPercent());
            action.setProgressPercent(request.getProgressPercent());
        }
        if (StringUtils.hasText(request.getStatus())) {
            action.setStatus(validateActionStatus(request.getStatus()));
        }
        if (StringUtils.hasText(request.getRemark())) {
            action.setRemark(request.getRemark().trim());
        }
        if (action.getProgressPercent() != null && action.getProgressPercent().compareTo(new BigDecimal("100")) >= 0
                && !ACTION_VERIFIED.equals(action.getStatus())) {
            action.setStatus(ACTION_COMPLETED);
        }
        EntityAuditSupport.touchUpdate(action);
        improvePlanActionService.updateById(action);
        syncPlanStatusFromActions(action.getPlanId(), false);
        return action;
    }

    @Override
    @Transactional
    public ImprovePlanRecord addRecord(Long actionId, ImprovePlanRecordSaveRequest request) {
        ImprovePlanAction action = getRequiredAction(actionId);
        ImprovePlan plan = getRequiredPlan(action.getPlanId());
        if (PLAN_VERIFIED.equals(plan.getStatus())) {
            throw new IllegalStateException("Verified plan cannot add records");
        }
        validateRecordRequest(request);
        ImprovePlanRecord record = new ImprovePlanRecord();
        applyRecordFields(record, request, actionId);
        EntityAuditSupport.touchCreate(record);
        improvePlanRecordService.save(record);
        bindAttachment(record.getAttachmentFileId(), record.getId());
        applyRecordActionUpdate(action, request);
        return record;
    }

    @Override
    @Transactional
    public ImprovePlanRecord updateRecord(Long recordId, ImprovePlanRecordSaveRequest request) {
        ImprovePlanRecord record = getRequiredRecord(recordId);
        ImprovePlanAction action = getRequiredAction(record.getActionId());
        ImprovePlan plan = getRequiredPlan(action.getPlanId());
        if (PLAN_VERIFIED.equals(plan.getStatus())) {
            throw new IllegalStateException("Verified plan cannot update records");
        }
        validateRecordRequest(request);
        applyRecordFields(record, request, action.getId());
        EntityAuditSupport.touchUpdate(record);
        improvePlanRecordService.updateById(record);
        bindAttachment(record.getAttachmentFileId(), record.getId());
        applyRecordActionUpdate(action, request);
        return record;
    }

    @Override
    @Transactional
    public void deleteRecord(Long recordId) {
        ImprovePlanRecord record = getRequiredRecord(recordId);
        ImprovePlanAction action = getRequiredAction(record.getActionId());
        ImprovePlan plan = getRequiredPlan(action.getPlanId());
        if (PLAN_VERIFIED.equals(plan.getStatus())) {
            throw new IllegalStateException("Verified plan cannot delete records");
        }
        EntityAuditSupport.touchDelete(record);
        improvePlanRecordService.updateById(record);
        syncPlanStatusFromActions(action.getPlanId(), false);
    }

    private ImprovePlanDetailVO buildDetail(ImprovePlan plan) {
        ImprovePlanDetailVO detail = BeanUtil.copyProperties(plan, ImprovePlanDetailVO.class);
        SysUser owner = plan.getOwnerUserId() == null ? null : sysUserService.getById(plan.getOwnerUserId());
        detail.setOwnerUserName(owner == null ? null : owner.getRealName());
        detail.setSourceDisplayName(resolveSourceDisplayName(plan.getSourceType(), plan.getSourceId()));
        detail.setTargetDisplayName(resolveTargetDisplayName(plan.getTargetType(), plan.getTargetId()));
        List<ImprovePlanAction> actions = listActiveActions(plan.getId());
        List<ImprovePlanRecord> records = listActiveRecords(actions.stream().map(ImprovePlanAction::getId).toList());
        Map<Long, List<ImprovePlanRecord>> recordMap = records.stream()
                .collect(Collectors.groupingBy(ImprovePlanRecord::getActionId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, SysUser> userMap = buildUserMap(actions, records, plan.getOwnerUserId());
        Map<Long, SysFile> fileMap = buildFileMap(records);
        List<ImprovePlanActionVO> actionVOs = actions.stream()
                .sorted(Comparator.comparing(ImprovePlanAction::getSortNo).thenComparing(ImprovePlanAction::getId))
                .map(action -> toActionVO(action, recordMap.getOrDefault(action.getId(), List.of()), userMap, fileMap))
                .toList();
        detail.setActions(actionVOs);
        detail.setActionCount(actionVOs.size());
        detail.setCompletedActionCount((int) actionVOs.stream().filter(action -> isDoneStatus(action.getStatus())).count());
        detail.setOverdueFlag(isPlanOverdue(plan) ? 1 : 0);
        return detail;
    }

    private ImprovePlanActionVO toActionVO(ImprovePlanAction action,
                                           List<ImprovePlanRecord> records,
                                           Map<Long, SysUser> userMap,
                                           Map<Long, SysFile> fileMap) {
        ImprovePlanActionVO vo = BeanUtil.copyProperties(action, ImprovePlanActionVO.class);
        SysUser responsible = userMap.get(action.getResponsibleUserId());
        vo.setResponsibleUserName(responsible == null ? null : responsible.getRealName());
        vo.setOverdueFlag(isActionOverdue(action) ? 1 : 0);
        vo.setRecordCount(records.size());
        vo.setRecords(records.stream()
                .sorted(Comparator.comparing(ImprovePlanRecord::getRecordTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ImprovePlanRecord::getId, Comparator.reverseOrder()))
                .map(record -> toRecordVO(record, userMap, fileMap))
                .toList());
        return vo;
    }

    private ImprovePlanRecordVO toRecordVO(ImprovePlanRecord record,
                                           Map<Long, SysUser> userMap,
                                           Map<Long, SysFile> fileMap) {
        ImprovePlanRecordVO vo = BeanUtil.copyProperties(record, ImprovePlanRecordVO.class);
        SysUser recorder = userMap.get(record.getRecorderUserId());
        SysFile file = fileMap.get(record.getAttachmentFileId());
        vo.setRecorderUserName(recorder == null ? null : recorder.getRealName());
        vo.setAttachmentFileName(file == null ? null : file.getOriginalName());
        return vo;
    }

    private void validatePlanRequest(ImprovePlanSaveRequest request, Long currentId) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (!StringUtils.hasText(request.getPlanCode())) {
            throw new IllegalArgumentException("Plan code is required");
        }
        if (!StringUtils.hasText(request.getPlanName())) {
            throw new IllegalArgumentException("Plan name is required");
        }
        if (!StringUtils.hasText(request.getSourceType()) || request.getSourceId() == null) {
            throw new IllegalArgumentException("Source type and source id are required");
        }
        if (!StringUtils.hasText(request.getTargetType()) || request.getTargetId() == null) {
            throw new IllegalArgumentException("Target type and target id are required");
        }
        if (request.getOwnerUserId() == null || !isActiveUserId(request.getOwnerUserId())) {
            throw new IllegalArgumentException("Owner user is invalid");
        }
        if (request.getStartDate() == null || request.getDueDate() == null) {
            throw new IllegalArgumentException("Start date and due date are required");
        }
        if (request.getDueDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Due date cannot be earlier than start date");
        }
        boolean duplicated = lambdaQuery()
                .eq(ImprovePlan::getPlanCode, request.getPlanCode().trim())
                .eq(ImprovePlan::getIsDeleted, 0)
                .ne(currentId != null, ImprovePlan::getId, currentId)
                .exists();
        if (duplicated) {
            throw new IllegalStateException("Plan code already exists");
        }
        validateActionRequests(request.getActions());
    }

    private void validateActionRequests(List<ImprovePlanActionRequest> actions) {
        if (actions == null || actions.isEmpty()) {
            throw new IllegalArgumentException("At least one action is required");
        }
        LinkedHashSet<String> uniqueCodes = new LinkedHashSet<>();
        for (ImprovePlanActionRequest action : actions) {
            if (action == null) {
                throw new IllegalArgumentException("Action cannot be null");
            }
            if (!StringUtils.hasText(action.getActionCode())) {
                throw new IllegalArgumentException("Action code is required");
            }
            if (!StringUtils.hasText(action.getActionTitle())) {
                throw new IllegalArgumentException("Action title is required");
            }
            if (!StringUtils.hasText(action.getActionDesc())) {
                throw new IllegalArgumentException("Action description is required");
            }
            if (action.getResponsibleUserId() == null || !isActiveUserId(action.getResponsibleUserId())) {
                throw new IllegalArgumentException("Action responsible user is invalid");
            }
            if (action.getStartDate() == null || action.getDueDate() == null) {
                throw new IllegalArgumentException("Action start date and due date are required");
            }
            if (action.getDueDate().isBefore(action.getStartDate())) {
                throw new IllegalArgumentException("Action due date cannot be earlier than start date");
            }
            validateProgress(action.getProgressPercent() == null ? BigDecimal.ZERO : action.getProgressPercent());
            if (!uniqueCodes.add(action.getActionCode().trim().toUpperCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Duplicate action code: " + action.getActionCode());
            }
        }
    }

    private void validateRecordRequest(ImprovePlanRecordSaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (!StringUtils.hasText(request.getRecordType())) {
            throw new IllegalArgumentException("Record type is required");
        }
        if (!StringUtils.hasText(request.getRecordContent())) {
            throw new IllegalArgumentException("Record content is required");
        }
        if (request.getRecorderUserId() == null || !isActiveUserId(request.getRecorderUserId())) {
            throw new IllegalArgumentException("Recorder user is invalid");
        }
        if (request.getAttachmentFileId() != null) {
            SysFile file = sysFileService.getById(request.getAttachmentFileId());
            if (file == null || (file.getIsDeleted() != null && file.getIsDeleted() != 0)) {
                throw new IllegalArgumentException("Attachment file is invalid");
            }
        }
        if (request.getProgressPercent() != null) {
            validateProgress(request.getProgressPercent());
        }
        if (StringUtils.hasText(request.getActionStatus())) {
            validateActionStatus(request.getActionStatus());
        }
    }

    private void applyPlanFields(ImprovePlan plan, ImprovePlanSaveRequest request) {
        plan.setPlanCode(request.getPlanCode().trim());
        plan.setPlanName(request.getPlanName().trim());
        plan.setSourceType(normalizeEnum(request.getSourceType()));
        plan.setSourceId(request.getSourceId());
        plan.setTargetType(normalizeEnum(request.getTargetType()));
        plan.setTargetId(request.getTargetId());
        plan.setOwnerUserId(request.getOwnerUserId());
        plan.setStartDate(request.getStartDate());
        plan.setDueDate(request.getDueDate());
        plan.setPriority(request.getPriority() == null ? 0 : request.getPriority());
        plan.setRemark(normalizeText(request.getRemark()));
    }

    private void replaceActions(Long planId, List<ImprovePlanActionRequest> actionRequests) {
        List<ImprovePlanAction> existingActions = listActiveActions(planId);
        if (!existingActions.isEmpty()) {
            markRecordsDeleted(existingActions.stream().map(ImprovePlanAction::getId).toList());
            existingActions.forEach(action -> {
                action.setActionCode(releaseUniqueCode(action.getActionCode(), action.getId()));
                EntityAuditSupport.touchDelete(action);
            });
            improvePlanActionService.updateBatchById(existingActions);
        }
        int index = 1;
        for (ImprovePlanActionRequest request : actionRequests) {
            releaseDeletedActionCodes(planId, request.getActionCode().trim());
            ImprovePlanAction action = new ImprovePlanAction();
            action.setPlanId(planId);
            action.setActionCode(request.getActionCode().trim());
            action.setActionTitle(request.getActionTitle().trim());
            action.setActionDesc(request.getActionDesc().trim());
            action.setResponsibleUserId(request.getResponsibleUserId());
            action.setStartDate(request.getStartDate());
            action.setDueDate(request.getDueDate());
            action.setProgressPercent(request.getProgressPercent() == null ? BigDecimal.ZERO : request.getProgressPercent());
            action.setStatus(StringUtils.hasText(request.getStatus()) ? validateActionStatus(request.getStatus()) : ACTION_PENDING);
            action.setSortNo(request.getSortNo() == null ? index : request.getSortNo());
            action.setRemark(normalizeText(request.getRemark()));
            EntityAuditSupport.touchCreate(action);
            improvePlanActionService.save(action);
            index++;
        }
    }

    private void applyRecordFields(ImprovePlanRecord record, ImprovePlanRecordSaveRequest request, Long actionId) {
        record.setActionId(actionId);
        record.setRecordType(normalizeEnum(request.getRecordType()));
        record.setRecordContent(request.getRecordContent().trim());
        record.setRecordTime(request.getRecordTime() == null ? LocalDateTime.now() : request.getRecordTime());
        record.setRecorderUserId(request.getRecorderUserId());
        record.setAttachmentFileId(request.getAttachmentFileId());
        record.setRemark(normalizeText(request.getRemark()));
    }

    private void applyRecordActionUpdate(ImprovePlanAction action, ImprovePlanRecordSaveRequest request) {
        boolean changed = false;
        if (request.getProgressPercent() != null) {
            action.setProgressPercent(request.getProgressPercent());
            changed = true;
        }
        if (StringUtils.hasText(request.getActionStatus())) {
            action.setStatus(validateActionStatus(request.getActionStatus()));
            changed = true;
        } else if (request.getProgressPercent() != null && request.getProgressPercent().compareTo(new BigDecimal("100")) >= 0
                && !ACTION_VERIFIED.equals(action.getStatus())) {
            action.setStatus(ACTION_COMPLETED);
            changed = true;
        } else if (request.getProgressPercent() != null && request.getProgressPercent().compareTo(BigDecimal.ZERO) > 0
                && ACTION_PENDING.equals(action.getStatus())) {
            action.setStatus(ACTION_IN_PROGRESS);
            changed = true;
        }
        if (changed) {
            EntityAuditSupport.touchUpdate(action);
            improvePlanActionService.updateById(action);
            syncPlanStatusFromActions(action.getPlanId(), false);
        }
    }

    private void syncPlanStatusFromActions(Long planId, boolean preserveVerified) {
        ImprovePlan plan = getRequiredPlan(planId);
        if (preserveVerified && PLAN_VERIFIED.equals(plan.getStatus())) {
            return;
        }
        List<ImprovePlanAction> actions = listActiveActions(planId);
        if (actions.isEmpty()) {
            plan.setStatus(PLAN_PENDING);
            plan.setClosedAt(null);
        } else if (actions.stream().allMatch(this::isActionDone)) {
            if (!PLAN_VERIFIED.equals(plan.getStatus())) {
                plan.setStatus(PLAN_COMPLETED);
            }
            if (!PLAN_VERIFIED.equals(plan.getStatus())) {
                plan.setClosedAt(LocalDateTime.now());
            }
        } else if (actions.stream().anyMatch(action -> ACTION_IN_PROGRESS.equals(action.getStatus())
                || (action.getProgressPercent() != null && action.getProgressPercent().compareTo(BigDecimal.ZERO) > 0))) {
            plan.setStatus(PLAN_IN_PROGRESS);
            plan.setClosedAt(null);
        } else {
            plan.setStatus(PLAN_PENDING);
            plan.setClosedAt(null);
        }
        EntityAuditSupport.touchUpdate(plan);
        updateById(plan);
    }

    private ImprovePlan getRequiredPlan(Long planId) {
        ImprovePlan plan = getActivePlan(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Improve plan not found");
        }
        return plan;
    }

    private ImprovePlan getActivePlan(Long planId) {
        if (planId == null) {
            return null;
        }
        return lambdaQuery()
                .eq(ImprovePlan::getId, planId)
                .eq(ImprovePlan::getIsDeleted, 0)
                .one();
    }

    private ImprovePlanAction getRequiredAction(Long actionId) {
        ImprovePlanAction action = improvePlanActionService.getOne(new LambdaQueryWrapper<ImprovePlanAction>()
                .eq(ImprovePlanAction::getId, actionId)
                .eq(ImprovePlanAction::getIsDeleted, 0), false);
        if (action == null) {
            throw new IllegalArgumentException("Improve action not found");
        }
        return action;
    }

    private ImprovePlanRecord getRequiredRecord(Long recordId) {
        ImprovePlanRecord record = improvePlanRecordService.getOne(new LambdaQueryWrapper<ImprovePlanRecord>()
                .eq(ImprovePlanRecord::getId, recordId)
                .eq(ImprovePlanRecord::getIsDeleted, 0), false);
        if (record == null) {
            throw new IllegalArgumentException("Improve record not found");
        }
        return record;
    }

    private List<ImprovePlanAction> listActiveActions(Long planId) {
        return improvePlanActionService.list(new LambdaQueryWrapper<ImprovePlanAction>()
                .eq(ImprovePlanAction::getPlanId, planId)
                .eq(ImprovePlanAction::getIsDeleted, 0)
                .orderByAsc(ImprovePlanAction::getSortNo)
                .orderByAsc(ImprovePlanAction::getId));
    }

    private List<ImprovePlanRecord> listActiveRecords(List<Long> actionIds) {
        if (actionIds == null || actionIds.isEmpty()) {
            return List.of();
        }
        return improvePlanRecordService.list(new LambdaQueryWrapper<ImprovePlanRecord>()
                .in(ImprovePlanRecord::getActionId, actionIds)
                .eq(ImprovePlanRecord::getIsDeleted, 0));
    }

    private void markRecordsDeleted(List<Long> actionIds) {
        List<ImprovePlanRecord> records = listActiveRecords(actionIds);
        if (records.isEmpty()) {
            return;
        }
        records.forEach(EntityAuditSupport::touchDelete);
        improvePlanRecordService.updateBatchById(records);
    }

    private void bindAttachment(Long fileId, Long recordId) {
        if (fileId != null && recordId != null) {
            fileStorageService.bindFile(fileId, "IMPROVE_PLAN_RECORD", recordId);
        }
    }

    private Map<Long, SysUser> buildUserMap(List<ImprovePlanAction> actions, List<ImprovePlanRecord> records, Long ownerUserId) {
        LinkedHashSet<Long> userIds = new LinkedHashSet<>();
        if (ownerUserId != null) {
            userIds.add(ownerUserId);
        }
        for (ImprovePlanAction action : actions) {
            if (action.getResponsibleUserId() != null) {
                userIds.add(action.getResponsibleUserId());
            }
        }
        for (ImprovePlanRecord record : records) {
            if (record.getRecorderUserId() != null) {
                userIds.add(record.getRecorderUserId());
            }
        }
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return sysUserService.listByIds(userIds).stream()
                .filter(user -> user.getIsDeleted() == null || user.getIsDeleted() == 0)
                .collect(Collectors.toMap(SysUser::getId, user -> user, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, SysFile> buildFileMap(List<ImprovePlanRecord> records) {
        Set<Long> fileIds = records.stream()
                .map(ImprovePlanRecord::getAttachmentFileId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (fileIds.isEmpty()) {
            return Map.of();
        }
        return sysFileService.listByIds(fileIds).stream()
                .filter(file -> file.getIsDeleted() == null || file.getIsDeleted() == 0)
                .collect(Collectors.toMap(SysFile::getId, file -> file, (left, right) -> left, LinkedHashMap::new));
    }

    private String resolveSourceDisplayName(String sourceType, Long sourceId) {
        if (!StringUtils.hasText(sourceType) || sourceId == null) {
            return null;
        }
        String type = normalizeEnum(sourceType);
        if ("SURVEY_QUESTIONNAIRE".equals(type)) {
            SurveyQuestionnaire questionnaire = surveyQuestionnaireService.getActiveQuestionnaireEntity(sourceId);
            return questionnaire == null ? type + "#" + sourceId : questionnaire.getTitle();
        }
        if ("SURVEY_RESPONSE".equals(type)) {
            return type + "#" + sourceId;
        }
        return type + "#" + sourceId;
    }

    private String resolveTargetDisplayName(String targetType, Long targetId) {
        if (!StringUtils.hasText(targetType) || targetId == null) {
            return null;
        }
        String type = normalizeEnum(targetType);
        if ("PROGRAM_TARGET".equals(type)) {
            TrProgramTarget target = trProgramTargetService.getById(targetId);
            return target == null ? type + "#" + targetId : target.getTargetName();
        }
        if ("GRADUATION_REQUIREMENT".equals(type)) {
            TrGraduationRequirement requirement = trGraduationRequirementService.getById(targetId);
            return requirement == null ? type + "#" + targetId : requirement.getRequirementName();
        }
        if ("COURSE".equals(type)) {
            var course = eduCourseService.getById(targetId);
            return course == null ? type + "#" + targetId : course.getCourseName();
        }
        if ("SURVEY_QUESTIONNAIRE".equals(type)) {
            SurveyQuestionnaire questionnaire = surveyQuestionnaireService.getActiveQuestionnaireEntity(targetId);
            return questionnaire == null ? type + "#" + targetId : questionnaire.getTitle();
        }
        return type + "#" + targetId;
    }

    private String buildReminderContent(ImprovePlan plan, List<ImprovePlanAction> actions) {
        StringBuilder builder = new StringBuilder();
        builder.append("Improve plan reminder. Plan: ").append(plan.getPlanName()).append('.');
        builder.append(" Due date: ").append(plan.getDueDate()).append('.');
        builder.append(" Pending actions: ").append(actions.size()).append('.');
        for (ImprovePlanAction action : actions) {
            builder.append(" [").append(action.getActionCode()).append("] ")
                    .append(action.getActionTitle()).append(" due ").append(action.getDueDate()).append(';');
        }
        return builder.toString();
    }

    private boolean isActiveUserId(Long userId) {
        SysUser user = sysUserService.getById(userId);
        return user != null && (user.getIsDeleted() == null || user.getIsDeleted() == 0);
    }

    private void ensureEditable(ImprovePlan plan) {
        if (PLAN_VERIFIED.equals(plan.getStatus())) {
            throw new IllegalStateException("Verified plan cannot be edited");
        }
    }

    private void releaseDeletedPlanCodes(String planCode, Long currentId) {
        if (!StringUtils.hasText(planCode)) {
            return;
        }
        List<ImprovePlan> deletedPlans = list(new LambdaQueryWrapper<ImprovePlan>()
                .eq(ImprovePlan::getPlanCode, planCode.trim())
                .eq(ImprovePlan::getIsDeleted, 1)
                .ne(currentId != null, ImprovePlan::getId, currentId));
        if (deletedPlans.isEmpty()) {
            return;
        }
        deletedPlans.forEach(plan -> {
            plan.setPlanCode(releaseUniqueCode(plan.getPlanCode(), plan.getId()));
            EntityAuditSupport.touchUpdate(plan);
        });
        updateBatchById(deletedPlans);
    }

    private void releaseDeletedActionCodes(Long planId, String actionCode) {
        if (planId == null || !StringUtils.hasText(actionCode)) {
            return;
        }
        List<ImprovePlanAction> deletedActions = improvePlanActionService.list(new LambdaQueryWrapper<ImprovePlanAction>()
                .eq(ImprovePlanAction::getPlanId, planId)
                .eq(ImprovePlanAction::getActionCode, actionCode.trim())
                .eq(ImprovePlanAction::getIsDeleted, 1));
        if (deletedActions.isEmpty()) {
            return;
        }
        deletedActions.forEach(action -> {
            action.setActionCode(releaseUniqueCode(action.getActionCode(), action.getId()));
            EntityAuditSupport.touchUpdate(action);
        });
        improvePlanActionService.updateBatchById(deletedActions);
    }

    private String releaseUniqueCode(String originalCode, Long id) {
        String base = StringUtils.hasText(originalCode) ? originalCode.trim() : "DELETED";
        String suffix = "_DEL_" + (id == null ? System.currentTimeMillis() : id);
        int keepLength = Math.max(0, 50 - suffix.length());
        String prefix = base.length() > keepLength ? base.substring(0, keepLength) : base;
        return prefix + suffix;
    }

    private boolean isPlanOverdue(ImprovePlan plan) {
        return plan.getDueDate() != null
                && plan.getDueDate().isBefore(LocalDate.now())
                && !PLAN_COMPLETED.equals(plan.getStatus())
                && !PLAN_VERIFIED.equals(plan.getStatus());
    }

    private boolean isActionOverdue(ImprovePlanAction action) {
        return action.getDueDate() != null
                && action.getDueDate().isBefore(LocalDate.now())
                && !isActionDone(action);
    }

    private boolean isActionDone(ImprovePlanAction action) {
        return action != null && isDoneStatus(action.getStatus());
    }

    private boolean isDoneStatus(String status) {
        return ACTION_COMPLETED.equals(status) || ACTION_VERIFIED.equals(status);
    }

    private void validateProgress(BigDecimal progressPercent) {
        if (progressPercent == null) {
            return;
        }
        if (progressPercent.compareTo(BigDecimal.ZERO) < 0 || progressPercent.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Progress percent must be between 0 and 100");
        }
    }

    private String validateActionStatus(String status) {
        String normalized = normalizeEnum(status);
        if (!ACTION_PENDING.equals(normalized)
                && !ACTION_IN_PROGRESS.equals(normalized)
                && !ACTION_COMPLETED.equals(normalized)
                && !ACTION_VERIFIED.equals(normalized)) {
            throw new IllegalArgumentException("Unsupported action status: " + status);
        }
        return normalized;
    }

    private String normalizeEnum(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : null;
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

}




