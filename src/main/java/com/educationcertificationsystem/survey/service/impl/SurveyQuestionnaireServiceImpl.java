package com.educationcertificationsystem.survey.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.constant.SurveyMqConstants;
import com.educationcertificationsystem.model.dto.notice.NoticeSendRequest;
import com.educationcertificationsystem.model.dto.survey.SurveyDispatchRequest;
import com.educationcertificationsystem.model.dto.survey.SurveyPublishEvent;
import com.educationcertificationsystem.model.dto.survey.SurveyQuestionItemRequest;
import com.educationcertificationsystem.model.dto.survey.SurveyQuestionMatrixColumnRequest;
import com.educationcertificationsystem.model.dto.survey.SurveyQuestionMatrixRowRequest;
import com.educationcertificationsystem.model.dto.survey.SurveyQuestionOptionRequest;
import com.educationcertificationsystem.model.dto.survey.SurveyQuestionnaireSaveRequest;
import com.educationcertificationsystem.model.dto.survey.SurveyQuestionnaireScopeRequest;
import com.educationcertificationsystem.model.entity.EduStudent;
import com.educationcertificationsystem.model.entity.EduTeacher;
import com.educationcertificationsystem.model.entity.OrgClass;
import com.educationcertificationsystem.model.entity.OrgGrade;
import com.educationcertificationsystem.model.entity.OrgMajor;
import com.educationcertificationsystem.model.entity.SurveyQuestionnaire;
import com.educationcertificationsystem.model.entity.SurveyPublishTask;
import com.educationcertificationsystem.model.entity.SurveyQuestion;
import com.educationcertificationsystem.model.entity.SurveyQuestionMatrixColumn;
import com.educationcertificationsystem.model.entity.SurveyQuestionMatrixRow;
import com.educationcertificationsystem.model.entity.SurveyQuestionOption;
import com.educationcertificationsystem.model.entity.SurveyQuestionnaireScope;
import com.educationcertificationsystem.model.entity.SysRole;
import com.educationcertificationsystem.model.entity.SysUser;
import com.educationcertificationsystem.model.entity.SysUserRole;
import com.educationcertificationsystem.model.vo.survey.SurveyPublishTaskVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionDetailVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionMatrixColumnVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionMatrixRowVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionOptionVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionnaireDetailVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionnairePageVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionnaireScopeVO;
import com.educationcertificationsystem.notice.service.NoticeMessageService;
import com.educationcertificationsystem.org.service.OrgClassService;
import com.educationcertificationsystem.org.service.OrgGradeService;
import com.educationcertificationsystem.org.service.OrgMajorService;
import com.educationcertificationsystem.role.service.SysRoleService;
import com.educationcertificationsystem.role.service.SysUserRoleService;
import com.educationcertificationsystem.survey.mapper.SurveyQuestionnaireMapper;
import com.educationcertificationsystem.survey.service.SurveyPublishTaskService;
import com.educationcertificationsystem.survey.service.SurveyQuestionMatrixColumnService;
import com.educationcertificationsystem.survey.service.SurveyQuestionMatrixRowService;
import com.educationcertificationsystem.survey.service.SurveyQuestionOptionService;
import com.educationcertificationsystem.survey.service.SurveyQuestionnaireService;
import com.educationcertificationsystem.survey.service.SurveyQuestionnaireScopeService;
import com.educationcertificationsystem.survey.service.SurveyQuestionService;
import com.educationcertificationsystem.support.EntityAuditSupport;
import com.educationcertificationsystem.course.service.EduStudentService;
import com.educationcertificationsystem.course.service.EduTeacherService;
import com.educationcertificationsystem.user.service.SysUserService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

/**
* @author Lizc233
* @description 针对表【survey_questionnaire(问卷主表)】的数据库操作Service实现
* @createDate 2026-07-16 14:29:35
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyQuestionnaireServiceImpl extends ServiceImpl<SurveyQuestionnaireMapper, SurveyQuestionnaire>
    implements SurveyQuestionnaireService {

    private static final String TARGET_STUDENT = "STUDENT";
    private static final String TARGET_IN_SCHOOL = "IN_SCHOOL_STUDENT";
    private static final String TARGET_GRADUATE = "GRADUATE";
    private static final String TARGET_TEACHER = "TEACHER";
    private static final String TARGET_EMPLOYER = "EMPLOYER";
    private static final String TARGET_ALL = "ALL";
    private static final String SCOPE_ROLE = "ROLE";
    private static final String SCOPE_GRADE = "GRADE";
    private static final String SCOPE_CLASS = "CLASS";
    private static final String SCOPE_MAJOR = "MAJOR";
    private static final String SCOPE_USER = "USER";
    private static final String QUESTION_SINGLE = "SINGLE";
    private static final String QUESTION_MULTIPLE = "MULTIPLE";
    private static final String QUESTION_SCALE = "SCALE";
    private static final String QUESTION_TEXT = "TEXT";
    private static final String QUESTION_MATRIX = "MATRIX";

    private final SurveyQuestionnaireScopeService surveyQuestionnaireScopeService;
    private final SurveyQuestionService surveyQuestionService;
    private final SurveyQuestionOptionService surveyQuestionOptionService;
    private final SurveyQuestionMatrixRowService surveyQuestionMatrixRowService;
    private final SurveyQuestionMatrixColumnService surveyQuestionMatrixColumnService;
    private final SurveyPublishTaskService surveyPublishTaskService;
    private final NoticeMessageService noticeMessageService;
    private final RabbitTemplate rabbitTemplate;
    private final SysUserService sysUserService;
    private final EduStudentService eduStudentService;
    private final EduTeacherService eduTeacherService;
    private final OrgClassService orgClassService;
    private final OrgGradeService orgGradeService;
    private final OrgMajorService orgMajorService;
    private final SysRoleService sysRoleService;
    private final SysUserRoleService sysUserRoleService;

    @Override
    public Page<SurveyQuestionnairePageVO> pageByCondition(long pageNum,
                                                           long pageSize,
                                                           String publishStatus,
                                                           String questionnaireType,
                                                           String targetObjectType,
                                                           String keyword) {
        long current = Math.max(pageNum, 1L);
        long size = Math.max(pageSize, 1L);
        long offset = (current - 1) * size;
        long total = baseMapper.countByCondition(normalizeText(publishStatus), normalizeText(questionnaireType),
                normalizeText(targetObjectType), normalizeText(keyword));
        List<SurveyQuestionnairePageVO> records = total == 0
                ? List.of()
                : baseMapper.selectPageByCondition(offset, size, normalizeText(publishStatus),
                normalizeText(questionnaireType), normalizeText(targetObjectType), normalizeText(keyword));
        Page<SurveyQuestionnairePageVO> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    @Override
    public SurveyQuestionnaireDetailVO getDetail(Long id) {
        SurveyQuestionnaire questionnaire = getActiveQuestionnaire(id);
        return questionnaire == null ? null : buildDetail(questionnaire);
    }

    @Override
    public SurveyQuestionnaireDetailVO preview(Long id) {
        return getDetail(id);
    }

    @Override
    @Transactional
    public SurveyQuestionnaireDetailVO createQuestionnaire(SurveyQuestionnaireSaveRequest request) {
        validateSaveRequest(request, null);
        SurveyQuestionnaire questionnaire = new SurveyQuestionnaire();
        applyQuestionnaireFields(questionnaire, request);
        questionnaire.setPublishStatus(SurveyMqConstants.QUESTIONNAIRE_STATUS_DRAFT);
        questionnaire.setMqStatus(SurveyMqConstants.MQ_STATUS_NONE);
        EntityAuditSupport.touchCreate(questionnaire);
        save(questionnaire);
        replaceScopes(questionnaire.getId(), request.getScopes());
        replaceQuestions(questionnaire.getId(), request.getQuestions());
        return getDetail(questionnaire.getId());
    }

    @Override
    @Transactional
    public SurveyQuestionnaireDetailVO updateQuestionnaire(Long id, SurveyQuestionnaireSaveRequest request) {
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(id);
        ensureEditable(questionnaire);
        validateSaveRequest(request, id);
        applyQuestionnaireFields(questionnaire, request);
        EntityAuditSupport.touchUpdate(questionnaire);
        updateById(questionnaire);
        replaceScopes(id, request.getScopes());
        replaceQuestions(id, request.getQuestions());
        return getDetail(id);
    }

    @Override
    @Transactional
    public void deleteQuestionnaire(Long id) {
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(id);
        ensureEditable(questionnaire);
        EntityAuditSupport.touchDelete(questionnaire);
        updateById(questionnaire);
        markScopesDeleted(id);
        markQuestionsDeleted(id);
        markTasksDeleted(id);
    }

    @Override
    @Transactional
    public SurveyQuestionnaire publish(Long id, SurveyDispatchRequest request) {
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(id);
        validateCanPublish(questionnaire);
        return createTaskAndDispatch(questionnaire, SurveyMqConstants.TASK_ACTION_PUBLISH, request);
    }

    @Override
    @Transactional
    public SurveyQuestionnaire retryPublish(Long id, SurveyDispatchRequest request) {
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(id);
        if (!SurveyMqConstants.QUESTIONNAIRE_STATUS_PUBLISH_FAILED.equals(questionnaire.getPublishStatus())
                && !SurveyMqConstants.MQ_STATUS_FAILED.equals(questionnaire.getMqStatus())
                && !SurveyMqConstants.QUESTIONNAIRE_STATUS_REVOKED.equals(questionnaire.getPublishStatus())) {
            throw new IllegalStateException("Questionnaire is not in retryable status");
        }
        validateHasQuestions(questionnaire.getId());
        validateScopesForDispatch(questionnaire);
        return createTaskAndDispatch(questionnaire, SurveyMqConstants.TASK_ACTION_RETRY, request);
    }

    @Override
    @Transactional
    public SurveyQuestionnaire revoke(Long id, String remark) {
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(id);
        if (SurveyMqConstants.QUESTIONNAIRE_STATUS_DRAFT.equals(questionnaire.getPublishStatus())) {
            throw new IllegalStateException("Draft questionnaire cannot be revoked");
        }
        if (SurveyMqConstants.QUESTIONNAIRE_STATUS_ENDED.equals(questionnaire.getPublishStatus())) {
            throw new IllegalStateException("Ended questionnaire cannot be revoked");
        }
        questionnaire.setPublishStatus(SurveyMqConstants.QUESTIONNAIRE_STATUS_REVOKED);
        questionnaire.setMqStatus(SurveyMqConstants.MQ_STATUS_NONE);
        if (StringUtils.hasText(remark)) {
            questionnaire.setRemark(remark.trim());
        }
        EntityAuditSupport.touchUpdate(questionnaire);
        updateById(questionnaire);
        SurveyPublishTask latestTask = getLatestTask(id);
        if (latestTask != null) {
            latestTask.setPublishStatus(SurveyMqConstants.TASK_STATUS_REVOKED);
            latestTask.setRevokedAt(LocalDateTime.now());
            if (StringUtils.hasText(remark)) {
                latestTask.setRemark(remark.trim());
            }
            EntityAuditSupport.touchUpdate(latestTask);
            surveyPublishTaskService.updateById(latestTask);
        }
        return questionnaire;
    }

    @Override
    @Transactional
    public SurveyQuestionnaire end(Long id, String remark) {
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(id);
        if (SurveyMqConstants.QUESTIONNAIRE_STATUS_ENDED.equals(questionnaire.getPublishStatus())) {
            return questionnaire;
        }
        questionnaire.setPublishStatus(SurveyMqConstants.QUESTIONNAIRE_STATUS_ENDED);
        if (StringUtils.hasText(remark)) {
            questionnaire.setRemark(remark.trim());
        }
        EntityAuditSupport.touchUpdate(questionnaire);
        updateById(questionnaire);
        return questionnaire;
    }

    @Override
    @Transactional
    public SurveyQuestionnaire sendDeadlineReminder(Long id, SurveyDispatchRequest request) {
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(id);
        if (!SurveyMqConstants.QUESTIONNAIRE_STATUS_PUBLISHED.equals(questionnaire.getPublishStatus())) {
            throw new IllegalStateException("Only published questionnaire can send reminders");
        }
        validateScopesForDispatch(questionnaire);
        return createTaskAndDispatch(questionnaire, SurveyMqConstants.TASK_ACTION_REMIND, request);
    }

    @Override
    public Page<SurveyPublishTaskVO> pagePublishTasks(Long questionnaireId, long pageNum, long pageSize) {
        getRequiredQuestionnaire(questionnaireId);
        long current = Math.max(pageNum, 1L);
        long size = Math.max(pageSize, 1L);
        LambdaQueryWrapper<SurveyPublishTask> wrapper = new LambdaQueryWrapper<SurveyPublishTask>()
                .eq(SurveyPublishTask::getQuestionnaireId, questionnaireId)
                .eq(SurveyPublishTask::getIsDeleted, 0)
                .orderByDesc(SurveyPublishTask::getCreatedAt)
                .orderByDesc(SurveyPublishTask::getId);
        Page<SurveyPublishTask> page = surveyPublishTaskService.page(new Page<>(current, size), wrapper);
        Page<SurveyPublishTaskVO> result = new Page<>(current, size);
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toTaskVO).toList());
        return result;
    }

    @Override
    @Transactional
    public void handlePublishEvent(SurveyPublishEvent event) {
        SurveyPublishTask task = surveyPublishTaskService.getById(event.getTaskId());
        if (task == null || task.getIsDeleted() != null && task.getIsDeleted() != 0) {
            throw new IllegalArgumentException("Publish task not found");
        }
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(event.getQuestionnaireId());
        try {
            List<Long> recipientUserIds = resolveRecipientUserIds(questionnaire);
            if (recipientUserIds.isEmpty()) {
                throw new IllegalStateException("No recipient users matched the current questionnaire scope");
            }
            NoticeSendRequest noticeRequest = buildNoticeRequest(questionnaire, event, recipientUserIds);
            noticeMessageService.sendNotice(noticeRequest);
            task.setMqStatus(SurveyMqConstants.MQ_STATUS_CONSUMED);
            task.setPublishedAt(LocalDateTime.now());
            task.setErrorMessage(null);
            task.setPublishStatus(isRemindAction(event.getActionType())
                    ? SurveyMqConstants.TASK_STATUS_REMINDED
                    : SurveyMqConstants.TASK_STATUS_PUBLISHED);
            EntityAuditSupport.touchUpdate(task);
            surveyPublishTaskService.updateById(task);

            questionnaire.setMqStatus(SurveyMqConstants.MQ_STATUS_CONSUMED);
            if (!isRemindAction(event.getActionType())) {
                questionnaire.setPublishStatus(SurveyMqConstants.QUESTIONNAIRE_STATUS_PUBLISHED);
            }
            EntityAuditSupport.touchUpdate(questionnaire);
            updateById(questionnaire);
        } catch (Exception ex) {
            log.error("Handle survey publish event failed, taskId={}, questionnaireId={}",
                    event.getTaskId(), event.getQuestionnaireId(), ex);
            markTaskFailed(task, questionnaire, event.getActionType(), ex.getMessage());
        }
    }

    private SurveyQuestionnaire createTaskAndDispatch(SurveyQuestionnaire questionnaire,
                                                      String actionType,
                                                      SurveyDispatchRequest request) {
        SurveyPublishTask task = new SurveyPublishTask();
        task.setQuestionnaireId(questionnaire.getId());
        task.setPublishBatchNo(buildPublishBatchNo(actionType));
        task.setPublishStatus(isRemindAction(actionType)
                ? SurveyMqConstants.TASK_STATUS_REMINDING
                : SurveyMqConstants.TASK_STATUS_PUBLISHING);
        task.setMqStatus(SurveyMqConstants.MQ_STATUS_WAITING);
        task.setRetryCount(resolveRetryCount(questionnaire.getId(), actionType));
        task.setRemark(request == null ? null : normalizeText(request.getRemark()));
        EntityAuditSupport.touchCreate(task);
        surveyPublishTaskService.save(task);

        questionnaire.setMqStatus(SurveyMqConstants.MQ_STATUS_WAITING);
        if (!isRemindAction(actionType)) {
            questionnaire.setPublishStatus(SurveyMqConstants.QUESTIONNAIRE_STATUS_PUBLISHING);
        }
        if (request != null && StringUtils.hasText(request.getRemark())) {
            questionnaire.setRemark(request.getRemark().trim());
        }
        EntityAuditSupport.touchUpdate(questionnaire);
        updateById(questionnaire);

        SurveyPublishEvent event = new SurveyPublishEvent(
                task.getId(),
                questionnaire.getId(),
                actionType,
                request == null ? null : request.getOperatorUserId(),
                request == null ? null : normalizeText(request.getRemark()),
                LocalDateTime.now());
        dispatchEventAfterCommit(task.getId(), questionnaire.getId(), actionType, event);
        return questionnaire;
    }

    private void dispatchEventAfterCommit(Long taskId,
                                          Long questionnaireId,
                                          String actionType,
                                          SurveyPublishEvent event) {
        Runnable sender = () -> {
            try {
                rabbitTemplate.convertAndSend(SurveyMqConstants.SURVEY_EXCHANGE, SurveyMqConstants.SURVEY_ROUTING_KEY, event);
                markTaskSent(taskId, questionnaireId);
            } catch (Exception ex) {
                log.error("Send survey publish event failed, taskId={}, questionnaireId={}", taskId, questionnaireId, ex);
                handleDispatchFailure(taskId, questionnaireId, actionType, ex.getMessage());
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sender.run();
                }
            });
        } else {
            sender.run();
        }
    }

    private void markTaskSent(Long taskId, Long questionnaireId) {
        SurveyPublishTask task = surveyPublishTaskService.getById(taskId);
        if (task != null) {
            task.setMqStatus(SurveyMqConstants.MQ_STATUS_SENT);
            EntityAuditSupport.touchUpdate(task);
            surveyPublishTaskService.updateById(task);
        }
        SurveyQuestionnaire questionnaire = baseMapper.selectById(questionnaireId);
        if (questionnaire != null) {
            questionnaire.setMqStatus(SurveyMqConstants.MQ_STATUS_SENT);
            EntityAuditSupport.touchUpdate(questionnaire);
            updateById(questionnaire);
        }
    }

    private void handleDispatchFailure(Long taskId, Long questionnaireId, String actionType, String errorMessage) {
        SurveyPublishTask task = surveyPublishTaskService.getById(taskId);
        SurveyQuestionnaire questionnaire = baseMapper.selectById(questionnaireId);
        if (task != null && questionnaire != null) {
            markTaskFailed(task, questionnaire, actionType, errorMessage);
        }
    }

    private void markTaskFailed(SurveyPublishTask task,
                                SurveyQuestionnaire questionnaire,
                                String actionType,
                                String errorMessage) {
        task.setMqStatus(SurveyMqConstants.MQ_STATUS_FAILED);
        task.setErrorMessage(trimToLength(errorMessage, 1000));
        task.setPublishStatus(isRemindAction(actionType)
                ? SurveyMqConstants.TASK_STATUS_REMIND_FAILED
                : SurveyMqConstants.TASK_STATUS_PUBLISH_FAILED);
        EntityAuditSupport.touchUpdate(task);
        surveyPublishTaskService.updateById(task);

        questionnaire.setMqStatus(SurveyMqConstants.MQ_STATUS_FAILED);
        if (!isRemindAction(actionType)) {
            questionnaire.setPublishStatus(SurveyMqConstants.QUESTIONNAIRE_STATUS_PUBLISH_FAILED);
        }
        EntityAuditSupport.touchUpdate(questionnaire);
        updateById(questionnaire);
    }

    private NoticeSendRequest buildNoticeRequest(SurveyQuestionnaire questionnaire,
                                                 SurveyPublishEvent event,
                                                 List<Long> recipientUserIds) {
        NoticeSendRequest request = new NoticeSendRequest();
        boolean remind = isRemindAction(event.getActionType());
        request.setNoticeType(remind ? "SURVEY_REMINDER" : "SURVEY_PENDING");
        request.setTitle(remind
                ? "Questionnaire deadline reminder: " + questionnaire.getTitle()
                : "Please complete questionnaire: " + questionnaire.getTitle());
        request.setContent(buildNoticeContent(questionnaire, remind));
        request.setSenderUserId(event.getOperatorUserId());
        request.setBizType("SURVEY_QUESTIONNAIRE");
        request.setBizId(questionnaire.getId());
        request.setChannelType("INTERNAL");
        request.setPriorityLevel(remind ? 2 : 1);
        request.setSendAt(LocalDateTime.now());
        request.setExpireAt(questionnaire.getEndTime());
        request.setRemark(event.getRemark());
        request.setRecipientUserIds(recipientUserIds);
        request.setOperatorUserId(event.getOperatorUserId());
        return request;
    }

    private String buildNoticeContent(SurveyQuestionnaire questionnaire, boolean remind) {
        StringBuilder builder = new StringBuilder();
        builder.append(remind ? "The questionnaire is approaching its deadline." : "A new questionnaire is available.");
        builder.append(" Title: ").append(questionnaire.getTitle()).append('.');
        if (StringUtils.hasText(questionnaire.getSubtitle())) {
            builder.append(" Subtitle: ").append(questionnaire.getSubtitle()).append('.');
        }
        if (questionnaire.getStartTime() != null) {
            builder.append(" Start time: ").append(questionnaire.getStartTime()).append('.');
        }
        if (questionnaire.getEndTime() != null) {
            builder.append(" End time: ").append(questionnaire.getEndTime()).append('.');
        }
        return builder.toString();
    }

    private List<Long> resolveRecipientUserIds(SurveyQuestionnaire questionnaire) {
        List<SurveyQuestionnaireScope> scopes = listActiveScopes(questionnaire.getId());
        LinkedHashSet<Long> userIds = new LinkedHashSet<>();
        if (scopes.isEmpty()) {
            userIds.addAll(resolveUsersWithoutScopes(questionnaire));
        } else {
            for (SurveyQuestionnaireScope scope : scopes) {
                userIds.addAll(resolveUsersByScope(scope));
            }
        }
        LinkedHashSet<Long> filtered = filterByTargetObjectType(userIds, questionnaire.getTargetObjectType());
        if (filtered.isEmpty() && TARGET_EMPLOYER.equalsIgnoreCase(normalizeEnum(questionnaire.getTargetObjectType()))) {
            filtered.addAll(userIds);
        }
        if (filtered.isEmpty()) {
            return List.of();
        }
        Map<Long, SysUser> activeUsers = sysUserService.listByIds(filtered).stream()
                .filter(user -> user.getIsDeleted() == null || user.getIsDeleted() == 0)
                .collect(Collectors.toMap(SysUser::getId, user -> user, (left, right) -> left, LinkedHashMap::new));
        return filtered.stream().filter(activeUsers::containsKey).toList();
    }

    private Collection<Long> resolveUsersWithoutScopes(SurveyQuestionnaire questionnaire) {
        String target = normalizeEnum(questionnaire.getTargetObjectType());
        return switch (target) {
            case TARGET_STUDENT, TARGET_IN_SCHOOL -> listActiveStudents().stream()
                    .map(EduStudent::getUserId)
                    .filter(Objects::nonNull)
                    .toList();
            case TARGET_GRADUATE -> listActiveStudents().stream()
                    .filter(student -> student.getGraduationStatus() != null && student.getGraduationStatus() != 0)
                    .map(EduStudent::getUserId)
                    .filter(Objects::nonNull)
                    .toList();
            case TARGET_TEACHER -> listActiveTeachers().stream()
                    .map(EduTeacher::getUserId)
                    .filter(Objects::nonNull)
                    .toList();
            case TARGET_ALL -> sysUserService.list(new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getIsDeleted, 0))
                    .stream()
                    .map(SysUser::getId)
                    .toList();
            default -> List.of();
        };
    }

    private Collection<Long> resolveUsersByScope(SurveyQuestionnaireScope scope) {
        String scopeType = normalizeEnum(scope.getScopeType());
        return switch (scopeType) {
            case SCOPE_USER -> scope.getScopeId() == null ? List.of() : List.of(scope.getScopeId());
            case SCOPE_ROLE -> sysUserRoleService.list(new LambdaQueryWrapper<SysUserRole>()
                            .eq(SysUserRole::getRoleId, scope.getScopeId())
                            .eq(SysUserRole::getIsDeleted, 0))
                    .stream()
                    .map(SysUserRole::getUserId)
                    .filter(Objects::nonNull)
                    .toList();
            case SCOPE_CLASS -> listActiveStudents().stream()
                    .filter(student -> Objects.equals(student.getClassId(), scope.getScopeId()))
                    .map(EduStudent::getUserId)
                    .filter(Objects::nonNull)
                    .toList();
            case SCOPE_GRADE -> {
                Set<Long> classIds = orgClassService.list(new LambdaQueryWrapper<OrgClass>()
                                .eq(OrgClass::getGradeId, scope.getScopeId())
                                .eq(OrgClass::getIsDeleted, 0))
                        .stream()
                        .map(OrgClass::getId)
                        .collect(Collectors.toSet());
                yield listActiveStudents().stream()
                        .filter(student -> classIds.contains(student.getClassId()))
                        .map(EduStudent::getUserId)
                        .filter(Objects::nonNull)
                        .toList();
            }
            case SCOPE_MAJOR -> {
                Set<Long> classIds = orgClassService.list(new LambdaQueryWrapper<OrgClass>()
                                .eq(OrgClass::getMajorId, scope.getScopeId())
                                .eq(OrgClass::getIsDeleted, 0))
                        .stream()
                        .map(OrgClass::getId)
                        .collect(Collectors.toSet());
                LinkedHashSet<Long> result = new LinkedHashSet<>();
                listActiveStudents().stream()
                        .filter(student -> classIds.contains(student.getClassId()))
                        .map(EduStudent::getUserId)
                        .filter(Objects::nonNull)
                        .forEach(result::add);
                listActiveTeachers().stream()
                        .filter(teacher -> Objects.equals(teacher.getMajorId(), scope.getScopeId()))
                        .map(EduTeacher::getUserId)
                        .filter(Objects::nonNull)
                        .forEach(result::add);
                yield result;
            }
            default -> List.of();
        };
    }

    private LinkedHashSet<Long> filterByTargetObjectType(Collection<Long> userIds, String targetObjectType) {
        LinkedHashSet<Long> result = new LinkedHashSet<>(userIds);
        String target = normalizeEnum(targetObjectType);
        if (result.isEmpty() || TARGET_ALL.equals(target) || TARGET_EMPLOYER.equals(target) || !StringUtils.hasText(target)) {
            return result;
        }
        Set<Long> allowedUserIds;
        if (TARGET_STUDENT.equals(target) || TARGET_IN_SCHOOL.equals(target)) {
            allowedUserIds = listActiveStudents().stream()
                    .map(EduStudent::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } else if (TARGET_GRADUATE.equals(target)) {
            allowedUserIds = listActiveStudents().stream()
                    .filter(student -> student.getGraduationStatus() != null && student.getGraduationStatus() != 0)
                    .map(EduStudent::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } else if (TARGET_TEACHER.equals(target)) {
            allowedUserIds = listActiveTeachers().stream()
                    .map(EduTeacher::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } else {
            return result;
        }
        result.retainAll(allowedUserIds);
        return result;
    }

    private SurveyQuestionnaireDetailVO buildDetail(SurveyQuestionnaire questionnaire) {
        SurveyQuestionnaireDetailVO detail = BeanUtil.copyProperties(questionnaire, SurveyQuestionnaireDetailVO.class);
        List<SurveyQuestionnaireScope> scopes = listActiveScopes(questionnaire.getId());
        List<SurveyQuestion> questions = listActiveQuestions(questionnaire.getId());
        detail.setScopeCount(scopes.size());
        detail.setQuestionCount(questions.size());
        detail.setScopes(scopes.stream().map(this::toScopeVO).toList());
        detail.setQuestions(buildQuestionVOs(questions));
        return detail;
    }

    private List<SurveyQuestionDetailVO> buildQuestionVOs(List<SurveyQuestion> questions) {
        if (questions.isEmpty()) {
            return List.of();
        }
        List<Long> questionIds = questions.stream().map(SurveyQuestion::getId).toList();
        Map<Long, List<SurveyQuestionOption>> optionsMap = surveyQuestionOptionService.list(
                        new LambdaQueryWrapper<SurveyQuestionOption>()
                                .in(SurveyQuestionOption::getQuestionId, questionIds)
                                .eq(SurveyQuestionOption::getIsDeleted, 0)
                                .orderByAsc(SurveyQuestionOption::getSortNo)
                                .orderByAsc(SurveyQuestionOption::getId))
                .stream()
                .collect(Collectors.groupingBy(SurveyQuestionOption::getQuestionId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<SurveyQuestionMatrixRow>> rowMap = surveyQuestionMatrixRowService.list(
                        new LambdaQueryWrapper<SurveyQuestionMatrixRow>()
                                .in(SurveyQuestionMatrixRow::getQuestionId, questionIds)
                                .eq(SurveyQuestionMatrixRow::getIsDeleted, 0)
                                .orderByAsc(SurveyQuestionMatrixRow::getSortNo)
                                .orderByAsc(SurveyQuestionMatrixRow::getId))
                .stream()
                .collect(Collectors.groupingBy(SurveyQuestionMatrixRow::getQuestionId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<SurveyQuestionMatrixColumn>> columnMap = surveyQuestionMatrixColumnService.list(
                        new LambdaQueryWrapper<SurveyQuestionMatrixColumn>()
                                .in(SurveyQuestionMatrixColumn::getQuestionId, questionIds)
                                .eq(SurveyQuestionMatrixColumn::getIsDeleted, 0)
                                .orderByAsc(SurveyQuestionMatrixColumn::getSortNo)
                                .orderByAsc(SurveyQuestionMatrixColumn::getId))
                .stream()
                .collect(Collectors.groupingBy(SurveyQuestionMatrixColumn::getQuestionId, LinkedHashMap::new, Collectors.toList()));
        return questions.stream().sorted(Comparator.comparing(SurveyQuestion::getSortNo).thenComparing(SurveyQuestion::getId))
                .map(question -> {
                    SurveyQuestionDetailVO vo = BeanUtil.copyProperties(question, SurveyQuestionDetailVO.class);
                    vo.setOptions(optionsMap.getOrDefault(question.getId(), List.of()).stream()
                            .map(item -> BeanUtil.copyProperties(item, SurveyQuestionOptionVO.class))
                            .toList());
                    vo.setMatrixRows(rowMap.getOrDefault(question.getId(), List.of()).stream()
                            .map(item -> BeanUtil.copyProperties(item, SurveyQuestionMatrixRowVO.class))
                            .toList());
                    vo.setMatrixColumns(columnMap.getOrDefault(question.getId(), List.of()).stream()
                            .map(item -> BeanUtil.copyProperties(item, SurveyQuestionMatrixColumnVO.class))
                            .toList());
                    return vo;
                }).toList();
    }

    private SurveyQuestionnaireScopeVO toScopeVO(SurveyQuestionnaireScope scope) {
        SurveyQuestionnaireScopeVO vo = BeanUtil.copyProperties(scope, SurveyQuestionnaireScopeVO.class);
        vo.setScopeName(resolveScopeName(scope.getScopeType(), scope.getScopeId()));
        return vo;
    }

    private String resolveScopeName(String scopeType, Long scopeId) {
        if (scopeId == null) {
            return null;
        }
        String type = normalizeEnum(scopeType);
        if (SCOPE_ROLE.equals(type)) {
            SysRole role = sysRoleService.getById(scopeId);
            return role == null ? null : role.getRoleName();
        }
        if (SCOPE_GRADE.equals(type)) {
            OrgGrade grade = orgGradeService.getById(scopeId);
            return grade == null ? null : String.valueOf(grade.getGradeYear());
        }
        if (SCOPE_CLASS.equals(type)) {
            OrgClass orgClass = orgClassService.getById(scopeId);
            return orgClass == null ? null : orgClass.getClassName();
        }
        if (SCOPE_MAJOR.equals(type)) {
            OrgMajor major = orgMajorService.getById(scopeId);
            return major == null ? null : major.getMajorName();
        }
        if (SCOPE_USER.equals(type)) {
            SysUser user = sysUserService.getById(scopeId);
            return user == null ? null : user.getRealName();
        }
        return null;
    }

    private SurveyPublishTaskVO toTaskVO(SurveyPublishTask task) {
        return BeanUtil.copyProperties(task, SurveyPublishTaskVO.class);
    }

    private void validateSaveRequest(SurveyQuestionnaireSaveRequest request, Long currentId) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (!StringUtils.hasText(request.getQuestionnaireCode())) {
            throw new IllegalArgumentException("Questionnaire code is required");
        }
        if (!StringUtils.hasText(request.getTitle())) {
            throw new IllegalArgumentException("Questionnaire title is required");
        }
        if (!StringUtils.hasText(request.getQuestionnaireType())) {
            throw new IllegalArgumentException("Questionnaire type is required");
        }
        if (!StringUtils.hasText(request.getTargetObjectType())) {
            throw new IllegalArgumentException("Target object type is required");
        }
        if (request.getStartTime() != null && request.getEndTime() != null
                && !request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be later than start time");
        }
        boolean duplicatedCode = lambdaQuery()
                .eq(SurveyQuestionnaire::getQuestionnaireCode, request.getQuestionnaireCode().trim())
                .eq(SurveyQuestionnaire::getIsDeleted, 0)
                .ne(currentId != null, SurveyQuestionnaire::getId, currentId)
                .exists();
        if (duplicatedCode) {
            throw new IllegalStateException("Questionnaire code already exists");
        }
        validateScopes(request.getScopes());
        validateQuestions(request.getQuestions());
    }

    private void validateScopes(List<SurveyQuestionnaireScopeRequest> scopes) {
        LinkedHashSet<String> uniqueKeys = new LinkedHashSet<>();
        if (scopes == null) {
            return;
        }
        for (SurveyQuestionnaireScopeRequest scope : scopes) {
            if (scope == null) {
                throw new IllegalArgumentException("Scope item cannot be null");
            }
            if (!StringUtils.hasText(scope.getScopeType()) || scope.getScopeId() == null) {
                throw new IllegalArgumentException("Scope type and scope id are required");
            }
            String type = normalizeEnum(scope.getScopeType());
            validateScopeExists(type, scope.getScopeId());
            String uniqueKey = type + ":" + scope.getScopeId();
            if (!uniqueKeys.add(uniqueKey)) {
                throw new IllegalArgumentException("Duplicate scope: " + uniqueKey);
            }
        }
    }

    private void validateQuestions(List<SurveyQuestionItemRequest> questions) {
        if (questions == null) {
            return;
        }
        LinkedHashSet<String> uniqueCodes = new LinkedHashSet<>();
        for (int i = 0; i < questions.size(); i++) {
            SurveyQuestionItemRequest question = questions.get(i);
            if (question == null) {
                throw new IllegalArgumentException("Question item cannot be null");
            }
            if (!StringUtils.hasText(question.getQuestionCode())) {
                throw new IllegalArgumentException("Question code is required");
            }
            if (!StringUtils.hasText(question.getQuestionText())) {
                throw new IllegalArgumentException("Question text is required");
            }
            if (!StringUtils.hasText(question.getQuestionType())) {
                throw new IllegalArgumentException("Question type is required");
            }
            String uniqueKey = normalizeEnum(question.getQuestionCode());
            if (!uniqueCodes.add(uniqueKey)) {
                throw new IllegalArgumentException("Duplicate question code: " + question.getQuestionCode());
            }
            validateQuestionStructure(question, i);
        }
    }

    private void validateQuestionStructure(SurveyQuestionItemRequest question, int index) {
        String questionType = normalizeEnum(question.getQuestionType());
        if (QUESTION_TEXT.equals(questionType)) {
            return;
        }
        if (QUESTION_SINGLE.equals(questionType) || QUESTION_MULTIPLE.equals(questionType) || QUESTION_SCALE.equals(questionType)) {
            validateOptions(question.getOptions(), index);
            return;
        }
        if (QUESTION_MATRIX.equals(questionType)) {
            validateMatrixRows(question.getMatrixRows(), index);
            validateMatrixColumns(question.getMatrixColumns(), index);
            return;
        }
        throw new IllegalArgumentException("Unsupported question type: " + question.getQuestionType());
    }

    private void validateOptions(List<SurveyQuestionOptionRequest> options, int questionIndex) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Question #" + (questionIndex + 1) + " must have options");
        }
        LinkedHashSet<String> uniqueCodes = new LinkedHashSet<>();
        for (SurveyQuestionOptionRequest option : options) {
            if (option == null) {
                throw new IllegalArgumentException("Option cannot be null");
            }
            if (!StringUtils.hasText(option.getOptionCode())
                    || !StringUtils.hasText(option.getOptionText())
                    || !StringUtils.hasText(option.getOptionValue())) {
                throw new IllegalArgumentException("Option code, text and value are required");
            }
            String uniqueKey = normalizeEnum(option.getOptionCode());
            if (!uniqueCodes.add(uniqueKey)) {
                throw new IllegalArgumentException("Duplicate option code: " + option.getOptionCode());
            }
        }
    }

    private void validateMatrixRows(List<SurveyQuestionMatrixRowRequest> rows, int questionIndex) {
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("Question #" + (questionIndex + 1) + " must have matrix rows");
        }
        LinkedHashSet<String> uniqueCodes = new LinkedHashSet<>();
        for (SurveyQuestionMatrixRowRequest row : rows) {
            if (row == null || !StringUtils.hasText(row.getRowCode()) || !StringUtils.hasText(row.getRowText())) {
                throw new IllegalArgumentException("Matrix row code and text are required");
            }
            String uniqueKey = normalizeEnum(row.getRowCode());
            if (!uniqueCodes.add(uniqueKey)) {
                throw new IllegalArgumentException("Duplicate matrix row code: " + row.getRowCode());
            }
        }
    }

    private void validateMatrixColumns(List<SurveyQuestionMatrixColumnRequest> columns, int questionIndex) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Question #" + (questionIndex + 1) + " must have matrix columns");
        }
        LinkedHashSet<String> uniqueCodes = new LinkedHashSet<>();
        for (SurveyQuestionMatrixColumnRequest column : columns) {
            if (column == null || !StringUtils.hasText(column.getColCode())
                    || !StringUtils.hasText(column.getColText())
                    || !StringUtils.hasText(column.getColValue())) {
                throw new IllegalArgumentException("Matrix column code, text and value are required");
            }
            String uniqueKey = normalizeEnum(column.getColCode());
            if (!uniqueCodes.add(uniqueKey)) {
                throw new IllegalArgumentException("Duplicate matrix column code: " + column.getColCode());
            }
        }
    }

    private void validateCanPublish(SurveyQuestionnaire questionnaire) {
        if (SurveyMqConstants.QUESTIONNAIRE_STATUS_PUBLISHING.equals(questionnaire.getPublishStatus())) {
            throw new IllegalStateException("Questionnaire is already publishing");
        }
        if (SurveyMqConstants.QUESTIONNAIRE_STATUS_PUBLISHED.equals(questionnaire.getPublishStatus())) {
            throw new IllegalStateException("Questionnaire is already published");
        }
        validateHasQuestions(questionnaire.getId());
        validateScopesForDispatch(questionnaire);
    }

    private void validateHasQuestions(Long questionnaireId) {
        long count = surveyQuestionService.count(new LambdaQueryWrapper<SurveyQuestion>()
                .eq(SurveyQuestion::getQuestionnaireId, questionnaireId)
                .eq(SurveyQuestion::getIsDeleted, 0));
        if (count == 0) {
            throw new IllegalStateException("Questionnaire without questions cannot be published");
        }
    }

    private void validateScopesForDispatch(SurveyQuestionnaire questionnaire) {
        List<SurveyQuestionnaireScope> scopes = listActiveScopes(questionnaire.getId());
        String target = normalizeEnum(questionnaire.getTargetObjectType());
        if (scopes.isEmpty() && TARGET_EMPLOYER.equals(target)) {
            throw new IllegalStateException("Employer questionnaires require user or role scopes");
        }
    }

    private void ensureEditable(SurveyQuestionnaire questionnaire) {
        if (SurveyMqConstants.QUESTIONNAIRE_STATUS_PUBLISHING.equals(questionnaire.getPublishStatus())
                || SurveyMqConstants.QUESTIONNAIRE_STATUS_PUBLISHED.equals(questionnaire.getPublishStatus())) {
            throw new IllegalStateException("Published or publishing questionnaire cannot be edited");
        }
    }

    private SurveyQuestionnaire getRequiredQuestionnaire(Long id) {
        SurveyQuestionnaire questionnaire = getActiveQuestionnaire(id);
        if (questionnaire == null) {
            throw new IllegalArgumentException("Questionnaire not found");
        }
        return questionnaire;
    }

    private SurveyQuestionnaire getActiveQuestionnaire(Long id) {
        if (id == null) {
            return null;
        }
        return lambdaQuery()
                .eq(SurveyQuestionnaire::getId, id)
                .eq(SurveyQuestionnaire::getIsDeleted, 0)
                .one();
    }

    private void applyQuestionnaireFields(SurveyQuestionnaire questionnaire, SurveyQuestionnaireSaveRequest request) {
        questionnaire.setQuestionnaireCode(request.getQuestionnaireCode().trim());
        questionnaire.setTitle(request.getTitle().trim());
        questionnaire.setSubtitle(normalizeText(request.getSubtitle()));
        questionnaire.setQuestionnaireType(normalizeEnum(request.getQuestionnaireType()));
        questionnaire.setTargetObjectType(normalizeEnum(request.getTargetObjectType()));
        questionnaire.setTargetObjectId(request.getTargetObjectId());
        questionnaire.setAnonymousFlag(request.getAnonymousFlag() == null ? 0 : request.getAnonymousFlag());
        questionnaire.setStartTime(request.getStartTime());
        questionnaire.setEndTime(request.getEndTime());
        questionnaire.setRemark(normalizeText(request.getRemark()));
    }

    private void replaceScopes(Long questionnaireId, List<SurveyQuestionnaireScopeRequest> scopes) {
        markScopesDeleted(questionnaireId);
        if (scopes == null || scopes.isEmpty()) {
            return;
        }
        for (SurveyQuestionnaireScopeRequest scope : scopes) {
            SurveyQuestionnaireScope entity = new SurveyQuestionnaireScope();
            entity.setQuestionnaireId(questionnaireId);
            entity.setScopeType(normalizeEnum(scope.getScopeType()));
            entity.setScopeId(scope.getScopeId());
            entity.setRemark(normalizeText(scope.getRemark()));
            EntityAuditSupport.touchCreate(entity);
            surveyQuestionnaireScopeService.save(entity);
        }
    }

    private void replaceQuestions(Long questionnaireId, List<SurveyQuestionItemRequest> questions) {
        markQuestionsDeleted(questionnaireId);
        if (questions == null || questions.isEmpty()) {
            return;
        }
        List<SurveyQuestionItemRequest> sortedQuestions = new ArrayList<>(questions);
        sortedQuestions.sort(Comparator.comparing(item -> item.getSortNo() == null ? Integer.MAX_VALUE : item.getSortNo()));
        for (int index = 0; index < sortedQuestions.size(); index++) {
            SurveyQuestionItemRequest questionRequest = sortedQuestions.get(index);
            SurveyQuestion question = new SurveyQuestion();
            question.setQuestionnaireId(questionnaireId);
            question.setQuestionCode(questionRequest.getQuestionCode().trim());
            question.setQuestionText(questionRequest.getQuestionText().trim());
            question.setQuestionType(normalizeEnum(questionRequest.getQuestionType()));
            question.setIsRequired(questionRequest.getIsRequired() == null ? 1 : questionRequest.getIsRequired());
            question.setSortNo(questionRequest.getSortNo() == null ? index + 1 : questionRequest.getSortNo());
            question.setMinSelect(questionRequest.getMinSelect());
            question.setMaxSelect(questionRequest.getMaxSelect());
            question.setScoreWeight(questionRequest.getScoreWeight());
            question.setMatrixType(normalizeText(questionRequest.getMatrixType()));
            question.setRemark(normalizeText(questionRequest.getRemark()));
            EntityAuditSupport.touchCreate(question);
            surveyQuestionService.save(question);
            saveQuestionChildren(question.getId(), questionRequest);
        }
    }

    private void saveQuestionChildren(Long questionId, SurveyQuestionItemRequest request) {
        if (request.getOptions() != null) {
            for (int index = 0; index < request.getOptions().size(); index++) {
                SurveyQuestionOptionRequest optionRequest = request.getOptions().get(index);
                SurveyQuestionOption option = new SurveyQuestionOption();
                option.setQuestionId(questionId);
                option.setOptionCode(optionRequest.getOptionCode().trim());
                option.setOptionText(optionRequest.getOptionText().trim());
                option.setOptionValue(optionRequest.getOptionValue().trim());
                option.setOptionScore(optionRequest.getOptionScore());
                option.setIsOther(optionRequest.getIsOther() == null ? 0 : optionRequest.getIsOther());
                option.setSortNo(optionRequest.getSortNo() == null ? index + 1 : optionRequest.getSortNo());
                option.setRemark(normalizeText(optionRequest.getRemark()));
                EntityAuditSupport.touchCreate(option);
                surveyQuestionOptionService.save(option);
            }
        }
        if (request.getMatrixRows() != null) {
            for (int index = 0; index < request.getMatrixRows().size(); index++) {
                SurveyQuestionMatrixRowRequest rowRequest = request.getMatrixRows().get(index);
                SurveyQuestionMatrixRow row = new SurveyQuestionMatrixRow();
                row.setQuestionId(questionId);
                row.setRowCode(rowRequest.getRowCode().trim());
                row.setRowText(rowRequest.getRowText().trim());
                row.setSortNo(rowRequest.getSortNo() == null ? index + 1 : rowRequest.getSortNo());
                row.setRemark(normalizeText(rowRequest.getRemark()));
                EntityAuditSupport.touchCreate(row);
                surveyQuestionMatrixRowService.save(row);
            }
        }
        if (request.getMatrixColumns() != null) {
            for (int index = 0; index < request.getMatrixColumns().size(); index++) {
                SurveyQuestionMatrixColumnRequest columnRequest = request.getMatrixColumns().get(index);
                SurveyQuestionMatrixColumn column = new SurveyQuestionMatrixColumn();
                column.setQuestionId(questionId);
                column.setColCode(columnRequest.getColCode().trim());
                column.setColText(columnRequest.getColText().trim());
                column.setColValue(columnRequest.getColValue().trim());
                column.setSortNo(columnRequest.getSortNo() == null ? index + 1 : columnRequest.getSortNo());
                column.setRemark(normalizeText(columnRequest.getRemark()));
                EntityAuditSupport.touchCreate(column);
                surveyQuestionMatrixColumnService.save(column);
            }
        }
    }

    private void markScopesDeleted(Long questionnaireId) {
        List<SurveyQuestionnaireScope> scopes = listActiveScopes(questionnaireId);
        if (scopes.isEmpty()) {
            return;
        }
        scopes.forEach(EntityAuditSupport::touchDelete);
        surveyQuestionnaireScopeService.updateBatchById(scopes);
    }

    private void markQuestionsDeleted(Long questionnaireId) {
        List<SurveyQuestion> questions = listActiveQuestions(questionnaireId);
        if (questions.isEmpty()) {
            return;
        }
        List<Long> questionIds = questions.stream().map(SurveyQuestion::getId).toList();
        markOptionRowsColumnsDeleted(questionIds);
        questions.forEach(EntityAuditSupport::touchDelete);
        surveyQuestionService.updateBatchById(questions);
    }

    private void markOptionRowsColumnsDeleted(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return;
        }
        List<SurveyQuestionOption> options = surveyQuestionOptionService.list(new LambdaQueryWrapper<SurveyQuestionOption>()
                .in(SurveyQuestionOption::getQuestionId, questionIds)
                .eq(SurveyQuestionOption::getIsDeleted, 0));
        if (!options.isEmpty()) {
            options.forEach(EntityAuditSupport::touchDelete);
            surveyQuestionOptionService.updateBatchById(options);
        }
        List<SurveyQuestionMatrixRow> rows = surveyQuestionMatrixRowService.list(new LambdaQueryWrapper<SurveyQuestionMatrixRow>()
                .in(SurveyQuestionMatrixRow::getQuestionId, questionIds)
                .eq(SurveyQuestionMatrixRow::getIsDeleted, 0));
        if (!rows.isEmpty()) {
            rows.forEach(EntityAuditSupport::touchDelete);
            surveyQuestionMatrixRowService.updateBatchById(rows);
        }
        List<SurveyQuestionMatrixColumn> columns = surveyQuestionMatrixColumnService.list(
                new LambdaQueryWrapper<SurveyQuestionMatrixColumn>()
                        .in(SurveyQuestionMatrixColumn::getQuestionId, questionIds)
                        .eq(SurveyQuestionMatrixColumn::getIsDeleted, 0));
        if (!columns.isEmpty()) {
            columns.forEach(EntityAuditSupport::touchDelete);
            surveyQuestionMatrixColumnService.updateBatchById(columns);
        }
    }

    private void markTasksDeleted(Long questionnaireId) {
        List<SurveyPublishTask> tasks = surveyPublishTaskService.list(new LambdaQueryWrapper<SurveyPublishTask>()
                .eq(SurveyPublishTask::getQuestionnaireId, questionnaireId)
                .eq(SurveyPublishTask::getIsDeleted, 0));
        if (tasks.isEmpty()) {
            return;
        }
        tasks.forEach(EntityAuditSupport::touchDelete);
        surveyPublishTaskService.updateBatchById(tasks);
    }

    private List<SurveyQuestionnaireScope> listActiveScopes(Long questionnaireId) {
        return surveyQuestionnaireScopeService.list(new LambdaQueryWrapper<SurveyQuestionnaireScope>()
                .eq(SurveyQuestionnaireScope::getQuestionnaireId, questionnaireId)
                .eq(SurveyQuestionnaireScope::getIsDeleted, 0)
                .orderByAsc(SurveyQuestionnaireScope::getId));
    }

    private List<SurveyQuestion> listActiveQuestions(Long questionnaireId) {
        return surveyQuestionService.list(new LambdaQueryWrapper<SurveyQuestion>()
                .eq(SurveyQuestion::getQuestionnaireId, questionnaireId)
                .eq(SurveyQuestion::getIsDeleted, 0)
                .orderByAsc(SurveyQuestion::getSortNo)
                .orderByAsc(SurveyQuestion::getId));
    }

    private SurveyPublishTask getLatestTask(Long questionnaireId) {
        return surveyPublishTaskService.getOne(new LambdaQueryWrapper<SurveyPublishTask>()
                .eq(SurveyPublishTask::getQuestionnaireId, questionnaireId)
                .eq(SurveyPublishTask::getIsDeleted, 0)
                .orderByDesc(SurveyPublishTask::getCreatedAt)
                .orderByDesc(SurveyPublishTask::getId)
                .last("limit 1"), false);
    }

    private int resolveRetryCount(Long questionnaireId, String actionType) {
        if (!SurveyMqConstants.TASK_ACTION_RETRY.equals(actionType)) {
            return 0;
        }
        SurveyPublishTask latestTask = getLatestTask(questionnaireId);
        return latestTask == null || latestTask.getRetryCount() == null ? 1 : latestTask.getRetryCount() + 1;
    }

    private void validateScopeExists(String scopeType, Long scopeId) {
        boolean exists = switch (scopeType) {
            case SCOPE_ROLE -> isActiveEntity(sysRoleService.getById(scopeId));
            case SCOPE_GRADE -> isActiveEntity(orgGradeService.getById(scopeId));
            case SCOPE_CLASS -> isActiveEntity(orgClassService.getById(scopeId));
            case SCOPE_MAJOR -> isActiveEntity(orgMajorService.getById(scopeId));
            case SCOPE_USER -> isActiveEntity(sysUserService.getById(scopeId));
            default -> throw new IllegalArgumentException("Unsupported scope type: " + scopeType);
        };
        if (!exists) {
            throw new IllegalArgumentException("Scope target not found: " + scopeType + ":" + scopeId);
        }
    }

    private boolean isActiveEntity(Object entity) {
        if (entity == null) {
            return false;
        }
        Object deleted = BeanUtil.getProperty(entity, "isDeleted");
        return !(deleted instanceof Number number) || number.intValue() == 0;
    }

    private List<EduStudent> listActiveStudents() {
        return eduStudentService.list(new LambdaQueryWrapper<EduStudent>()
                .eq(EduStudent::getIsDeleted, 0));
    }

    private List<EduTeacher> listActiveTeachers() {
        return eduTeacherService.list(new LambdaQueryWrapper<EduTeacher>()
                .eq(EduTeacher::getIsDeleted, 0));
    }

    private boolean isRemindAction(String actionType) {
        return SurveyMqConstants.TASK_ACTION_REMIND.equals(actionType);
    }

    private String buildPublishBatchNo(String actionType) {
        return "SQ-" + normalizeEnum(actionType) + "-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String normalizeEnum(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : null;
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String trimToLength(String value, int maxLength) {
        if (!StringUtils.hasText(value) || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}




