# 成员E代码整理

> 说明：以下内容按“问卷设计与发布、持续改进计划、自评报告、AI辅助”四条业务线整理，保留 `controller-service-impl-mapper` 主链路，并补充了较多注释，方便直接粘贴到 Word 作为成员 E 材料。

## 1. 成员E分工范围

- F21 问卷设计、发布与异步投递
- F22 问卷填报与回收统计
- F23 持续改进计划与改进记录
- F24 自评报告任务与协同撰写
- F25 AI 智能分析与业务写回

## 2. 完整流程一：F21 问卷设计、发布与 MQ 投递

### 2.1 Controller

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/surveys/questionnaires")
public class SurveyQuestionnaireController {

    // 控制层只负责收参与返回结果，不写复杂业务逻辑。
    // 真正的问卷保存、发布、撤回、催办全部下沉到 service 层。
    private final SurveyQuestionnaireService surveyQuestionnaireService;

    // 问卷列表接口。
    // 这个接口主要给前端列表页使用。
    // 支持按照发布状态、问卷类型、目标对象类型和关键字进行分页筛选。
    @GetMapping
    public Result<Page<SurveyQuestionnairePageVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String publishStatus,
            @RequestParam(required = false) String questionnaireType,
            @RequestParam(required = false) String targetObjectType,
            @RequestParam(required = false) String keyword) {
        return Result.success(surveyQuestionnaireService.pageByCondition(
                pageNum, pageSize, publishStatus, questionnaireType, targetObjectType, keyword));
    }

    // 问卷详情接口。
    // 这里返回的不是单一主表，而是聚合视图对象。
    // 其中会包含问卷主信息、题目列表、题目子项、适用范围以及发布任务情况。
    @GetMapping("/{id}")
    public Result<SurveyQuestionnaireDetailVO> detail(@PathVariable Long id) {
        SurveyQuestionnaireDetailVO detail = surveyQuestionnaireService.getDetail(id);
        return detail == null ? Result.error("Questionnaire not found") : Result.success(detail);
    }

    // 预览接口。
    // 当前实现中预览和详情复用同一套 service 结果。
    // 这样可以避免维护两套完全相同的数据拼装逻辑。
    @GetMapping("/{id}/preview")
    public Result<SurveyQuestionnaireDetailVO> preview(@PathVariable Long id) {
        SurveyQuestionnaireDetailVO detail = surveyQuestionnaireService.preview(id);
        return detail == null ? Result.error("Questionnaire not found") : Result.success(detail);
    }

    // 创建问卷接口。
    // 一个问卷通常不是只存一张表，而是要同时保存：
    // 1. 问卷主表
    // 2. 问卷适用范围
    // 3. 题目列表
    // 4. 选项、矩阵行列等题目子表
    // 因此这里显式加上事务，保证整套数据要么全成功，要么全回滚。
    @PostMapping
    @Transactional
    public Result<SurveyQuestionnaireDetailVO> create(@RequestBody SurveyQuestionnaireSaveRequest request) {
        try {
            return Result.success(surveyQuestionnaireService.createQuestionnaire(request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Create questionnaire failed");
        }
    }

    // 更新问卷接口。
    // 这里会走与创建相似的结构保存逻辑。
    // 但更新前需要额外判断当前问卷是否允许编辑。
    // 对于“发布中”和“已发布”的问卷，系统一般禁止再修改题目结构。
    @PutMapping("/{id}")
    @Transactional
    public Result<SurveyQuestionnaireDetailVO> update(@PathVariable Long id,
                                                      @RequestBody SurveyQuestionnaireSaveRequest request) {
        try {
            return Result.success(surveyQuestionnaireService.updateQuestionnaire(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Update questionnaire failed");
        }
    }

    // 发布问卷接口。
    // 该接口并不是直接同步给所有用户发消息，
    // 而是先创建发布任务，再交给 MQ 异步处理。
    // 这样可以避免目标用户数量很大时阻塞主线程。
    @PostMapping("/{id}/publish")
    @Transactional
    public Result<SurveyQuestionnaire> publish(@PathVariable Long id,
                                               @RequestBody(required = false) SurveyDispatchRequest request) {
        try {
            return Result.success(surveyQuestionnaireService.publish(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Publish questionnaire failed");
        }
    }

    // 重试发布接口。
    // 用于上一次 MQ 投递失败、或者问卷曾撤回后重新尝试发布。
    @PostMapping("/{id}/retry-publish")
    @Transactional
    public Result<SurveyQuestionnaire> retryPublish(@PathVariable Long id,
                                                    @RequestBody(required = false) SurveyDispatchRequest request) {
        try {
            return Result.success(surveyQuestionnaireService.retryPublish(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Retry publish questionnaire failed");
        }
    }

    // 撤回接口。
    // 已经进入发布状态的问卷，可以被管理员撤回。
    // 撤回后通常不会物理删除数据，而是修改状态，保留审计痕迹。
    @PostMapping("/{id}/revoke")
    @Transactional
    public Result<SurveyQuestionnaire> revoke(@PathVariable Long id,
                                              @RequestBody(required = false) SurveyDispatchRequest request) {
        try {
            return Result.success(surveyQuestionnaireService.revoke(id, request == null ? null : request.getRemark()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Revoke questionnaire failed");
        }
    }

    // 结束接口。
    // 问卷结束后，通常表示不再接受新的填写提交。
    @PostMapping("/{id}/end")
    @Transactional
    public Result<SurveyQuestionnaire> end(@PathVariable Long id,
                                           @RequestBody(required = false) SurveyDispatchRequest request) {
        try {
            return Result.success(surveyQuestionnaireService.end(id, request == null ? null : request.getRemark()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("End questionnaire failed");
        }
    }

    // 截止提醒接口。
    // 该接口的作用是对已经发布但尚未截止的问卷再次发送提醒通知。
    @PostMapping("/{id}/deadline-reminder")
    @Transactional
    public Result<SurveyQuestionnaire> deadlineReminder(@PathVariable Long id,
                                                        @RequestBody(required = false) SurveyDispatchRequest request) {
        try {
            return Result.success(surveyQuestionnaireService.sendDeadlineReminder(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Send deadline reminder failed");
        }
    }
}
```

### 2.2 Service

```java
@Service
@RequiredArgsConstructor
public class SurveyQuestionnaireServiceImpl extends ServiceImpl<SurveyQuestionnaireMapper, SurveyQuestionnaire>
        implements SurveyQuestionnaireService {

    // 这里把问卷业务里出现频率最高的类型、状态定义成常量。
    // 好处有两个：
    // 1. 避免魔法字符串散落在各个方法里
    // 2. 后续改名时只需要改一处
    private static final String TARGET_EMPLOYER = "EMPLOYER";
    private static final String QUESTION_MATRIX = "MATRIX";

    // 以下依赖分别负责不同子模块的数据保存。
    // 例如 scope 负责适用范围，question 负责题目主表，option/row/column 负责题目子表。
    // 这种拆分方式符合“主表 + 子表”的数据库设计思路。
    private final SurveyQuestionnaireScopeService surveyQuestionnaireScopeService;
    private final SurveyQuestionService surveyQuestionService;
    private final SurveyQuestionOptionService surveyQuestionOptionService;
    private final SurveyQuestionMatrixRowService surveyQuestionMatrixRowService;
    private final SurveyQuestionMatrixColumnService surveyQuestionMatrixColumnService;
    private final SurveyPublishTaskService surveyPublishTaskService;
    private final NoticeMessageService noticeMessageService;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public SurveyQuestionnaireDetailVO createQuestionnaire(SurveyQuestionnaireSaveRequest request) {
        // 1. 先校验，确保主表、范围、题目都满足发布前置条件
        // 这一步的目标是尽早失败，把错误挡在数据库写入之前。
        validateSaveRequest(request, null);

        // 2. 先保存主表，拿到 questionnaireId 后再写从表
        // 因为范围表、题目表都依赖 questionnaireId 作为外键。
        SurveyQuestionnaire questionnaire = new SurveyQuestionnaire();
        applyQuestionnaireFields(questionnaire, request);
        questionnaire.setPublishStatus(SurveyMqConstants.QUESTIONNAIRE_STATUS_DRAFT);
        questionnaire.setMqStatus(SurveyMqConstants.MQ_STATUS_NONE);
        EntityAuditSupport.touchCreate(questionnaire);
        save(questionnaire);

        // 3. 范围和题目分开落库，便于后续更新时做整树替换
        // 这也是后续更新时能“先清旧数据再整体重建”的基础。
        replaceScopes(questionnaire.getId(), request.getScopes());
        replaceQuestions(questionnaire.getId(), request.getQuestions());
        return getDetail(questionnaire.getId());
    }

    @Override
    @Transactional
    public SurveyQuestionnaireDetailVO updateQuestionnaire(Long id, SurveyQuestionnaireSaveRequest request) {
        // 已发布或发布中不允许改题，避免投递后的数据不一致
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(id);
        ensureEditable(questionnaire);
        validateSaveRequest(request, id);

        // 主表先更新。
        // 题目和范围后面再整体替换。
        applyQuestionnaireFields(questionnaire, request);
        EntityAuditSupport.touchUpdate(questionnaire);
        updateById(questionnaire);

        // 更新问卷时直接重建题目树和作用范围
        // 这样虽然看起来“粗暴”，但比逐条比对差异更稳定，也更好维护。
        replaceScopes(id, request.getScopes());
        replaceQuestions(id, request.getQuestions());
        return getDetail(id);
    }

    @Override
    @Transactional
    public SurveyQuestionnaire publish(Long id, SurveyDispatchRequest request) {
        // 发布前必须有题目、有范围，并且状态允许发布
        // 如果连问卷结构都不完整，就不应该进入异步投递阶段。
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(id);
        validateCanPublish(questionnaire);
        return createTaskAndDispatch(questionnaire, SurveyMqConstants.TASK_ACTION_PUBLISH, request);
    }

    @Override
    @Transactional
    public SurveyQuestionnaire retryPublish(Long id, SurveyDispatchRequest request) {
        // 重试只允许失败、撤回等可重发状态
        // 这样做可以避免一个正常发布中的问卷被重复发送。
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

    // 发布任务创建后，不在事务里直接发 MQ，而是在事务提交后再投递
    // 这是一个非常重要的设计点。
    // 如果数据库事务还没提交，消息先发出去，会出现“消费者能读到任务，但数据库回滚了”的脏状态。
    private SurveyQuestionnaire createTaskAndDispatch(SurveyQuestionnaire questionnaire,
                                                      String actionType,
                                                      SurveyDispatchRequest request) {
        // 先创建发布任务，记录这一轮发布批次的信息。
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

        // 问卷主表状态同步到“待发布 / 发布中”
        // 这样前端列表页就能立刻感知当前问卷的状态变化。
        questionnaire.setMqStatus(SurveyMqConstants.MQ_STATUS_WAITING);
        if (!isRemindAction(actionType)) {
            questionnaire.setPublishStatus(SurveyMqConstants.QUESTIONNAIRE_STATUS_PUBLISHING);
        }
        if (request != null && StringUtils.hasText(request.getRemark())) {
            questionnaire.setRemark(request.getRemark().trim());
        }
        EntityAuditSupport.touchUpdate(questionnaire);
        updateById(questionnaire);

        // 事务提交后再发送消息，避免主库回滚但消息已经发出去
        // 发送的数据包含任务 id、问卷 id、动作类型、操作人等上下文。
        SurveyPublishEvent event = new SurveyPublishEvent(
                task.getId(), questionnaire.getId(), actionType,
                request == null ? null : request.getOperatorUserId(),
                request == null ? null : normalizeText(request.getRemark()),
                LocalDateTime.now());
        dispatchEventAfterCommit(task.getId(), questionnaire.getId(), actionType, event);
        return questionnaire;
    }

    @Override
    @Transactional
    public void handlePublishEvent(SurveyPublishEvent event) {
        // MQ 消费侧：先拿任务，再计算接收人，最后写回任务状态
        // 该方法体现了“异步通知中心”的业务闭环。
        SurveyPublishTask task = surveyPublishTaskService.getById(event.getTaskId());
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(event.getQuestionnaireId());
        try {
            List<Long> recipientUserIds = resolveRecipientUserIds(questionnaire);
            if (recipientUserIds.isEmpty()) {
                // 没有收件人时不报错，直接视为任务完成。
                // 这样做是为了让系统在空范围场景下保持幂等。
                markTaskPublished(task, questionnaire, event.getActionType());
                return;
            }
            // 组装站内信/通知中心请求对象。
            NoticeSendRequest noticeRequest = buildNoticeRequest(questionnaire, event, recipientUserIds);
            noticeMessageService.sendNotice(noticeRequest);
            // 通知发送成功后回写任务状态。
            markTaskPublished(task, questionnaire, event.getActionType());
        } catch (Exception ex) {
            // 一旦通知发送异常，就记录失败状态和失败原因，便于后续重试。
            markTaskFailed(task, questionnaire, event.getActionType(), ex.getMessage());
        }
    }

    // 范围是“谁能看到 / 谁能收到”的控制层，问卷发布前必须先清空再重建
    // 范围数据通常来自角色、班级、年级、专业或用户本身。
    private void replaceScopes(Long questionnaireId, List<SurveyQuestionnaireScopeRequest> scopes) {
        purgeScopes(questionnaireId);
        if (scopes == null || scopes.isEmpty()) {
            return;
        }
        for (SurveyQuestionnaireScopeRequest scope : scopes) {
            // 每一条 scope 记录对应一条适用范围明细。
            SurveyQuestionnaireScope entity = new SurveyQuestionnaireScope();
            entity.setQuestionnaireId(questionnaireId);
            entity.setScopeType(normalizeEnum(scope.getScopeType()));
            entity.setScopeId(scope.getScopeId());
            entity.setRemark(normalizeText(scope.getRemark()));
            EntityAuditSupport.touchCreate(entity);
            surveyQuestionnaireScopeService.save(entity);
        }
    }

    // 题目树支持单选、多选、量表、矩阵题，保存时统一走“先删后建”
    // 这样可以保证题目顺序、选项顺序、矩阵结构全部与前端提交结果完全一致。
    private void replaceQuestions(Long questionnaireId, List<SurveyQuestionItemRequest> questions) {
        purgeQuestions(questionnaireId);
        if (questions == null || questions.isEmpty()) {
            return;
        }
        List<SurveyQuestionItemRequest> sortedQuestions = new ArrayList<>(questions);
        // 按 sortNo 排序后再保存，确保展示顺序可控。
        sortedQuestions.sort(Comparator.comparing(item -> item.getSortNo() == null ? Integer.MAX_VALUE : item.getSortNo()));
        for (int index = 0; index < sortedQuestions.size(); index++) {
            SurveyQuestionItemRequest questionRequest = sortedQuestions.get(index);
            // 先保存题目主表，再保存题目子项。
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

    // 子表拆成三个方向：选项、行、列；这样矩阵题和普通题共用同一套保存链
    // 普通单选/多选题通常只落 option。
    // 矩阵题则会额外落 row 和 column。
    private void saveQuestionChildren(Long questionId, SurveyQuestionItemRequest request) {
        if (request.getOptions() != null) {
            for (int index = 0; index < request.getOptions().size(); index++) {
                SurveyQuestionOptionRequest optionRequest = request.getOptions().get(index);
                // 保存选项表。
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
                // 保存矩阵行定义。
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
                // 保存矩阵列定义。
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

    // 发布前校验：有题目、有范围、状态合法，才能进入 MQ 流程
    // 这是避免“空问卷”“非法状态问卷”被错误投递的最后一道闸门。
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

    private void validateScopesForDispatch(SurveyQuestionnaire questionnaire) {
        List<SurveyQuestionnaireScope> scopes = listActiveScopes(questionnaire.getId());
        String target = normalizeEnum(questionnaire.getTargetObjectType());
        // 企业/用人单位类问卷必须明确作用人群，否则不能发布
        // 因为这类问卷一般没有系统内自然用户全集，必须显式指定。
        if (scopes.isEmpty() && TARGET_EMPLOYER.equals(target)) {
            throw new IllegalStateException("Employer questionnaires require user or role scopes");
        }
    }
}
```

### 2.3 Mapper

```xml
<mapper namespace="com.educationcertificationsystem.survey.mapper.SurveyQuestionnaireMapper">

    <!-- 主表字段映射，保证前端列表、详情都能直接拿到驼峰字段 -->
    <resultMap id="BaseResultMap" type="com.educationcertificationsystem.model.entity.SurveyQuestionnaire">
        <id property="id" column="id" />
        <result property="questionnaireCode" column="questionnaire_code" />
        <result property="title" column="title" />
        <result property="subtitle" column="subtitle" />
        <result property="questionnaireType" column="questionnaire_type" />
        <result property="targetObjectType" column="target_object_type" />
        <result property="targetObjectId" column="target_object_id" />
        <result property="anonymousFlag" column="anonymous_flag" />
        <result property="publishStatus" column="publish_status" />
        <result property="startTime" column="start_time" />
        <result property="endTime" column="end_time" />
        <result property="mqStatus" column="mq_status" />
        <result property="remark" column="remark" />
    </resultMap>

    <!-- 条件片段：列表页和导出页共用 -->
    <sql id="Condition_Where">
        where q.is_deleted = 0
        <if test="publishStatus != null and publishStatus != ''">
            and q.publish_status = #{publishStatus}
        </if>
        <if test="questionnaireType != null and questionnaireType != ''">
            and q.questionnaire_type = #{questionnaireType}
        </if>
        <if test="targetObjectType != null and targetObjectType != ''">
            and q.target_object_type = #{targetObjectType}
        </if>
        <if test="keyword != null and keyword != ''">
            and (
                q.questionnaire_code like concat('%', #{keyword}, '%')
                or q.title like concat('%', #{keyword}, '%')
                or q.subtitle like concat('%', #{keyword}, '%')
            )
        </if>
    </sql>

    <!-- 分页：除了主表字段，还把题目数、范围数、最近一次发布任务一起带出来 -->
    <select id="selectPageByCondition" resultType="com.educationcertificationsystem.model.vo.survey.SurveyQuestionnairePageVO">
        select q.id,
               q.questionnaire_code as questionnaireCode,
               q.title,
               q.subtitle,
               q.questionnaire_type as questionnaireType,
               q.target_object_type as targetObjectType,
               q.target_object_id as targetObjectId,
               q.anonymous_flag as anonymousFlag,
               q.publish_status as publishStatus,
               q.mq_status as mqStatus,
               q.start_time as startTime,
               q.end_time as endTime,
               (select count(1) from survey_question sq where sq.questionnaire_id = q.id and sq.is_deleted = 0) as questionCount,
               (select count(1) from survey_questionnaire_scope sqs where sqs.questionnaire_id = q.id and sqs.is_deleted = 0) as scopeCount,
               (select spt.publish_batch_no from survey_publish_task spt
                    where spt.questionnaire_id = q.id and spt.is_deleted = 0
                    order by spt.created_at desc, spt.id desc limit 1) as latestPublishBatchNo,
               q.created_at as createdAt,
               q.updated_at as updatedAt,
               q.remark
        from survey_questionnaire q
        <include refid="Condition_Where"/>
        order by q.updated_at desc, q.id desc
        limit #{offset}, #{size}
    </select>
</mapper>
```

## 3. 完整流程二：F23 持续改进计划与改进记录

### 3.1 Controller

```java
@RestController
@RequiredArgsConstructor
@Tag(name = "Improve Plan")
@RequestMapping("/api/improve/plans")
public class ImprovePlanController {

    // 控制层只暴露改进计划闭环相关接口。
    // 例如列表、详情、创建、启动、完成、记录维护等。
    private final ImprovePlanService improvePlanService;

    // 计划列表接口。
    // 提供多维查询能力，方便前端做条件筛选和逾期看板。
    @GetMapping
    public Result<Page<ImprovePlanPageVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long ownerUserId,
            @RequestParam(required = false) Long responsibleUserId,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false, defaultValue = "0") Integer overdueOnly,
            @RequestParam(required = false) String keyword) {
        return Result.success(improvePlanService.pageByCondition(
                pageNum, pageSize, status, sourceType, targetType,
                ownerUserId, responsibleUserId, priority, overdueOnly, keyword));
    }

    // 详情接口。
    // 详情中会返回计划本身，以及行动项、记录、责任人等聚合数据。
    @GetMapping("/{id}")
    public Result<ImprovePlanDetailVO> detail(@PathVariable Long id) {
        ImprovePlanDetailVO detail = improvePlanService.getDetail(id);
        return detail == null ? Result.error("Improve plan not found") : Result.success(detail);
    }

    // 创建接口。
    // 一次提交会同时创建计划主表与行动项子表。
    @PostMapping
    @Transactional
    public Result<ImprovePlanDetailVO> create(@RequestBody ImprovePlanSaveRequest request) {
        try {
            return Result.success(improvePlanService.createPlan(request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Create improve plan failed");
        }
    }

    // 更新接口。
    // 如果计划已经进入最终校验通过状态，则不允许再编辑。
    @PutMapping("/{id}")
    @Transactional
    public Result<ImprovePlanDetailVO> update(@PathVariable Long id,
                                              @RequestBody ImprovePlanSaveRequest request) {
        try {
            return Result.success(improvePlanService.updatePlan(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Update improve plan failed");
        }
    }

    // 启动接口。
    // 用于把计划状态从待执行推进到执行中。
    @PostMapping("/{id}/start")
    @Transactional
    public Result<ImprovePlan> start(@PathVariable Long id) {
        try {
            return Result.success(improvePlanService.startPlan(id));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Start improve plan failed");
        }
    }

    // 完成接口。
    // 只有全部行动项完成后，计划才允许整体完成。
    @PostMapping("/{id}/complete")
    @Transactional
    public Result<ImprovePlan> complete(@PathVariable Long id) {
        try {
            return Result.success(improvePlanService.completePlan(id));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Complete improve plan failed");
        }
    }

    // 行动项进度更新接口。
    // 这是计划执行阶段最常用的接口之一。
    @PostMapping("/actions/{actionId}/progress")
    public Result<ImprovePlanAction> updateActionProgress(@PathVariable Long actionId,
                                                          @RequestBody ImprovePlanActionProgressRequest request) {
        try {
            return Result.success(improvePlanService.updateActionProgress(actionId, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Update improve action progress failed");
        }
    }

    // 过程记录新增接口。
    // 记录通常用于沉淀执行证据、附件、进度说明和阶段性备注。
    @PostMapping("/actions/{actionId}/records")
    @Transactional
    public Result<ImprovePlanRecord> addRecord(@PathVariable Long actionId,
                                               @RequestBody ImprovePlanRecordSaveRequest request) {
        try {
            return Result.success(improvePlanService.addRecord(actionId, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Add improve record failed");
        }
    }
}
```

### 3.2 Service

```java
@Service
@RequiredArgsConstructor
public class ImprovePlanServiceImpl extends ServiceImpl<ImprovePlanMapper, ImprovePlan>
        implements ImprovePlanService {

    // 计划状态。
    // 计划级状态用于列表页总览、详情页显示以及后续提醒、验证等业务判断。
    private static final String PLAN_PENDING = "PENDING";
    private static final String PLAN_IN_PROGRESS = "IN_PROGRESS";
    private static final String PLAN_COMPLETED = "COMPLETED";
    private static final String PLAN_VERIFIED = "VERIFIED";

    // 行动项状态。
    // 行动项状态与计划状态并不是一一对应的，但会反向驱动计划状态同步。
    private static final String ACTION_PENDING = "PENDING";
    private static final String ACTION_IN_PROGRESS = "IN_PROGRESS";
    private static final String ACTION_COMPLETED = "COMPLETED";
    private static final String ACTION_VERIFIED = "VERIFIED";

    // 这些依赖覆盖了“行动项、过程记录、附件、通知”的完整改进闭环。
    private final ImprovePlanActionService improvePlanActionService;
    private final ImprovePlanRecordService improvePlanRecordService;
    private final SysUserService sysUserService;
    private final SysFileService sysFileService;
    private final FileStorageService fileStorageService;
    private final NoticeMessageService noticeMessageService;

    @Override
    @Transactional
    public ImprovePlanDetailVO createPlan(ImprovePlanSaveRequest request) {
        // 先做完整性校验：主表日期、行动项日期、责任人、编码唯一性都要检查
        // 这一步可以把大多数前端填报问题拦截在持久化之前。
        validatePlanRequest(request, null);
        releaseDeletedPlanCodes(request.getPlanCode().trim(), null);

        // 创建计划主表。
        ImprovePlan plan = new ImprovePlan();
        applyPlanFields(plan, request);
        plan.setStatus(PLAN_PENDING);
        plan.setEffectReview(null);
        plan.setClosedAt(null);
        EntityAuditSupport.touchCreate(plan);
        save(plan);

        // 行动项不是内嵌 JSON，而是独立子表，便于后续记录和进度更新
        // 行动项保存后，还需要同步刷新一次计划整体状态。
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

        // 先更新主表，再重建行动项。
        applyPlanFields(plan, request);
        EntityAuditSupport.touchUpdate(plan);
        updateById(plan);

        // 更新时也采用整组替换，避免残留旧行动项
        // 这可以避免“前端删掉一条行动项，但数据库里还留着”的问题。
        replaceActions(id, request.getActions());
        syncPlanStatusFromActions(id, false);
        return getDetail(id);
    }

    @Override
    @Transactional
    public ImprovePlan startPlan(Long id) {
        ImprovePlan plan = getRequiredPlan(id);
        if (PLAN_VERIFIED.equals(plan.getStatus())) {
            throw new IllegalStateException("Verified plan cannot be restarted");
        }
        // 启动后状态切换为执行中，关闭时间清空。
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
        // 只有全部行动项完成，计划整体才允许完成。
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
    public ImprovePlanAction updateActionProgress(Long actionId, ImprovePlanActionProgressRequest request) {
        // 行动项进度变更后，自动回推计划状态，保证列表页和详情页一致
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
            // 如果前端显式传了状态，则优先采用前端传入的状态。
            action.setStatus(validateActionStatus(request.getStatus()));
        }
        if (action.getProgressPercent() != null
                && action.getProgressPercent().compareTo(new BigDecimal("100")) >= 0
                && !ACTION_VERIFIED.equals(action.getStatus())) {
            // 进度达到 100% 时，系统自动把状态推进为已完成。
            action.setStatus(ACTION_COMPLETED);
        }
        EntityAuditSupport.touchUpdate(action);
        improvePlanActionService.updateById(action);
        // 行动项变更完成后，同步刷新计划状态。
        syncPlanStatusFromActions(action.getPlanId(), false);
        return action;
    }

    @Override
    @Transactional
    public ImprovePlanRecord addRecord(Long actionId, ImprovePlanRecordSaveRequest request) {
        // 记录新增后，不只落记录表，还要反向更新行动项进度
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

        // 附件绑定到文件表，方便后续下载和审计追踪
        // 这样文件记录与业务记录之间就建立了明确关联。
        bindAttachment(record.getAttachmentFileId(), record.getId());
        // 记录里如果带了进度或状态，还会同步更新行动项。
        applyRecordActionUpdate(action, request);
        return record;
    }

    // 主表校验是整个改进计划最关键的入口，直接决定前端能不能保存
    // 本项目中很多前端报错，其实都来源于这里的字段完整性校验。
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
        // 主表校验通过后，还要继续检查行动项集合。
        validateActionRequests(request.getActions());
    }

    // 行动项必须有起止日期，否则后面“提醒、逾期、完成”都无法计算
    // 同时还要保证编码不重复、责任人合法、进度值范围正确。
    private void validateActionRequests(List<ImprovePlanActionRequest> actions) {
        if (actions == null || actions.isEmpty()) {
            throw new IllegalArgumentException("At least one action is required");
        }
        LinkedHashSet<String> uniqueCodes = new LinkedHashSet<>();
        for (ImprovePlanActionRequest action : actions) {
            // 每个行动项都逐条做校验，避免脏数据进入子表。
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

    // 计划主信息统一赋值，避免 create/update 两条链路的字段逻辑不一致
    // 这种字段装配方法在业务项目里很常见，能降低重复代码。
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

    // 先删旧行动项，再批量重建，保持排序号和状态都可控
    // 由于行动项下还有记录表，所以先删前要把记录一并软删除处理。
    private void replaceActions(Long planId, List<ImprovePlanActionRequest> actionRequests) {
        List<ImprovePlanAction> existingActions = listActiveActions(planId);
        if (!existingActions.isEmpty()) {
            // 先处理旧行动项对应的记录数据。
            markRecordsDeleted(existingActions.stream().map(ImprovePlanAction::getId).toList());
            existingActions.forEach(action -> {
                action.setActionCode(releaseUniqueCode(action.getActionCode(), action.getId()));
                EntityAuditSupport.touchDelete(action);
            });
            improvePlanActionService.updateBatchById(existingActions);
        }
        int index = 1;
        for (ImprovePlanActionRequest request : actionRequests) {
            // 再按请求内容重建新的行动项。
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

    // 基于行动项状态自动同步计划状态，保证“计划-行动项”始终一致
    // 这是该模块最关键的状态联动逻辑之一。
    private void syncPlanStatusFromActions(Long planId, boolean preserveVerified) {
        ImprovePlan plan = getRequiredPlan(planId);
        if (preserveVerified && PLAN_VERIFIED.equals(plan.getStatus())) {
            return;
        }
        List<ImprovePlanAction> actions = listActiveActions(planId);
        if (actions.isEmpty()) {
            // 没有行动项时，计划只能回到待执行。
            plan.setStatus(PLAN_PENDING);
            plan.setClosedAt(null);
        } else if (actions.stream().allMatch(this::isActionDone)) {
            // 全部完成时，计划进入已完成。
            if (!PLAN_VERIFIED.equals(plan.getStatus())) {
                plan.setStatus(PLAN_COMPLETED);
            }
            if (!PLAN_VERIFIED.equals(plan.getStatus())) {
                plan.setClosedAt(LocalDateTime.now());
            }
        } else if (actions.stream().anyMatch(action -> ACTION_IN_PROGRESS.equals(action.getStatus())
                || (action.getProgressPercent() != null && action.getProgressPercent().compareTo(BigDecimal.ZERO) > 0))) {
            // 只要任一行动项开始推进，计划整体就标记为执行中。
            plan.setStatus(PLAN_IN_PROGRESS);
            plan.setClosedAt(null);
        } else {
            // 否则说明行动项都还没真正开始。
            plan.setStatus(PLAN_PENDING);
            plan.setClosedAt(null);
        }
        EntityAuditSupport.touchUpdate(plan);
        updateById(plan);
    }
}
```

### 3.3 Mapper

```xml
<mapper namespace="com.educationcertificationsystem.improve.mapper.ImprovePlanMapper">

    <!-- 改进计划主表映射 -->
    <resultMap id="BaseResultMap" type="com.educationcertificationsystem.model.entity.ImprovePlan">
        <id property="id" column="id" />
        <result property="planCode" column="plan_code" />
        <result property="planName" column="plan_name" />
        <result property="sourceType" column="source_type" />
        <result property="sourceId" column="source_id" />
        <result property="targetType" column="target_type" />
        <result property="targetId" column="target_id" />
        <result property="ownerUserId" column="owner_user_id" />
        <result property="startDate" column="start_date" />
        <result property="dueDate" column="due_date" />
        <result property="status" column="status" />
        <result property="priority" column="priority" />
        <result property="remark" column="remark" />
    </resultMap>

    <sql id="Condition_Where">
        where p.is_deleted = 0
        <if test="status != null and status != ''">
            and p.status = #{status}
        </if>
        <if test="sourceType != null and sourceType != ''">
            and p.source_type = #{sourceType}
        </if>
        <if test="targetType != null and targetType != ''">
            and p.target_type = #{targetType}
        </if>
        <if test="ownerUserId != null">
            and p.owner_user_id = #{ownerUserId}
        </if>
        <if test="priority != null">
            and p.priority = #{priority}
        </if>
        <if test="responsibleUserId != null">
            and exists (
                select 1
                from improve_plan_action a
                where a.plan_id = p.id
                  and a.is_deleted = 0
                  and a.responsible_user_id = #{responsibleUserId}
            )
        </if>
        <if test="overdueOnly != null and overdueOnly == 1">
            and p.due_date &lt; current_date
            and p.status not in ('COMPLETED', 'VERIFIED')
        </if>
        <if test="keyword != null and keyword != ''">
            and (
                p.plan_code like concat('%', #{keyword}, '%')
                or p.plan_name like concat('%', #{keyword}, '%')
                or p.remark like concat('%', #{keyword}, '%')
            )
        </if>
    </sql>

    <select id="selectPageByCondition" resultType="com.educationcertificationsystem.model.vo.improve.ImprovePlanPageVO">
        select p.id,
               p.plan_code as planCode,
               p.plan_name as planName,
               p.source_type as sourceType,
               p.source_id as sourceId,
               p.target_type as targetType,
               p.target_id as targetId,
               p.owner_user_id as ownerUserId,
               u.real_name as ownerUserName,
               p.start_date as startDate,
               p.due_date as dueDate,
               p.status,
               p.priority,
               (
                   select count(1)
                   from improve_plan_action a
                   where a.plan_id = p.id and a.is_deleted = 0
               ) as actionCount,
               p.remark
        from improve_plan p
        left join sys_user u on u.id = p.owner_user_id and (u.is_deleted = 0 or u.is_deleted is null)
        <include refid="Condition_Where"/>
        order by p.updated_at desc, p.id desc
        limit #{offset}, #{size}
    </select>
</mapper>
```

## 4. 完整流程三：F24 自评报告任务与协同撰写

### 4.1 Controller

```java
@RestController
@RequiredArgsConstructor
@Tag(name = "Report Project")
@RequestMapping("/api/reports/projects")
public class ReportProjectController {

    // 报告项目控制器负责组织“项目、章节、任务、草稿、进度”的入口接口。
    private final ReportProjectService reportProjectService;

    // 项目列表接口。
    // 支持按状态、负责人和关键字检索。
    @GetMapping
    public Result<Page<ReportProjectPageVO>> list(@RequestParam(defaultValue = "1") long pageNum,
                                                  @RequestParam(defaultValue = "10") long pageSize,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) Long ownerUserId,
                                                  @RequestParam(required = false) Long viewerUserId,
                                                  @RequestParam(required = false) String keyword) {
        return Result.success(reportProjectService.pageByCondition(pageNum, pageSize, status, ownerUserId, viewerUserId, keyword));
    }

    // 创建报告项目。
    // 可以理解为自评报告协同写作的总入口。
    @PostMapping
    @Transactional
    public Result<ReportProjectDetailVO> create(@RequestBody ReportProjectSaveRequest request) {
        try {
            return Result.success(reportProjectService.createProject(request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Create report project failed");
        }
    }

    // 保存章节树接口。
    // 章节树是报告目录结构的核心。
    @PutMapping("/{id}/chapters")
    @Transactional
    public Result<ReportProjectDetailVO> saveChapters(@PathVariable Long id,
                                                      @RequestBody List<ReportChapterSaveRequest> requests) {
        try {
            return Result.success(reportProjectService.saveChapterTree(id, requests));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Save report chapter tree failed");
        }
    }

    // 保存任务分配接口。
    // 用于把某个章节分给某位教师或负责人。
    @PutMapping("/{id}/assignments")
    @Transactional
    public Result<List<ReportTaskAssignment>> saveAssignments(@PathVariable Long id,
                                                              @RequestBody List<ReportTaskAssignmentSaveRequest> requests) {
        try {
            return Result.success(reportProjectService.saveAssignments(id, requests));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Save report assignments failed");
        }
    }

    // 草稿保存接口。
    // 该接口适用于在线编辑场景。
    @PostMapping("/chapters/{chapterId}/drafts")
    @Transactional
    public Result<ReportDraft> saveDraft(@PathVariable Long chapterId,
                                         @RequestBody ReportDraftSaveRequest request) {
        try {
            return Result.success(reportProjectService.saveDraft(chapterId, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Save report draft failed");
        }
    }

    // 草稿上传接口。
    // 该接口适用于先离线写好，再上传文本文件的场景。
    @PostMapping("/chapters/{chapterId}/drafts/upload")
    @Transactional
    public Result<ReportDraft> uploadDraft(@PathVariable Long chapterId,
                                           @ModelAttribute ReportDraftUploadRequest request,
                                           @RequestParam("file") MultipartFile file) {
        try {
            return Result.success(reportProjectService.uploadDraft(chapterId, request, file));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            return Result.error("Upload report draft failed");
        }
    }
}
```

### 4.2 Service

```java
@Service
@RequiredArgsConstructor
public class ReportProjectServiceImpl extends ServiceImpl<ReportProjectMapper, ReportProject>
        implements ReportProjectService {

    // 项目级状态。
    private static final String PROJECT_DRAFT = "DRAFT";
    private static final String PROJECT_IN_PROGRESS = "IN_PROGRESS";
    private static final String PROJECT_COMPLETED = "COMPLETED";
    // 章节级状态。
    private static final String CHAPTER_TODO = "TODO";
    private static final String CHAPTER_IN_PROGRESS = "IN_PROGRESS";
    private static final String CHAPTER_COMPLETED = "COMPLETED";

    // 依赖的服务分别负责章节、任务、草稿和进度日志。
    private final ReportChapterService reportChapterService;
    private final ReportTaskAssignmentService reportTaskAssignmentService;
    private final ReportDraftService reportDraftService;
    private final ReportProgressLogService reportProgressLogService;
    private final SysUserService sysUserService;
    private final NoticeMessageService noticeMessageService;

    @Override
    @Transactional
    public ReportProjectDetailVO createProject(ReportProjectSaveRequest request) {
        // 报告项目创建时，可以顺带带入章节树
        // 这使得用户在第一次创建项目时就能同时初始化目录骨架。
        validateProjectRequest(request, null);
        ReportProject project = new ReportProject();
        applyProjectFields(project, request);
        project.setStatus(PROJECT_DRAFT);
        project.setTotalChapters(0);
        project.setLockedFlag(0);
        project.setExportedAt(null);
        EntityAuditSupport.touchCreate(project);
        save(project);
        if (request.getChapters() != null && !request.getChapters().isEmpty()) {
            saveChapterTree(project.getId(), request.getChapters());
        }
        return buildDetail(getRequiredProject(project.getId()), null);
    }

    @Override
    @Transactional
    public ReportProjectDetailVO saveChapterTree(Long projectId, List<ReportChapterSaveRequest> requests) {
        // 章节树是一个递归结构，因此保存时先校验，再按树形逐层写入
        // 这里体现了自评报告“章节目录树”的典型管理方式。
        getRequiredProject(projectId);
        List<ReportChapter> existing = listActiveChapters(projectId);
        Map<Long, ReportChapter> existingMap = existing.stream()
                .collect(Collectors.toMap(ReportChapter::getId, chapter -> chapter, (left, right) -> left, LinkedHashMap::new));
        validateChapterRequests(requests, existingMap, new HashSet<>(), new HashSet<>());

        LinkedHashSet<Long> retainedIds = new LinkedHashSet<>();
        int sortNo = 1;
        if (requests != null) {
            for (ReportChapterSaveRequest request : requests) {
                // 递归插入或更新章节节点。
                sortNo = upsertChapterNode(projectId, null, request, sortNo, existingMap, retainedIds);
            }
        }

        // 没有保留下来的章节直接软删，避免目录树出现“幽灵章节”
        // 如果不做这一步，前端删掉的章节在数据库里会一直残留。
        List<ReportChapter> removable = existing.stream()
                .filter(chapter -> !retainedIds.contains(chapter.getId()))
                .toList();
        if (!removable.isEmpty()) {
            ensureChaptersRemovable(removable.stream().map(ReportChapter::getId).toList());
            removable.forEach(EntityAuditSupport::touchDelete);
            reportChapterService.updateBatchById(removable);
        }

        syncProjectSummary(projectId);
        return buildDetail(getRequiredProject(projectId), null);
    }

    @Override
    @Transactional
    public List<ReportTaskAssignment> saveAssignments(Long projectId, List<ReportTaskAssignmentSaveRequest> requests) {
        ReportProject project = getRequiredProject(projectId);
        Map<Long, ReportChapter> chapterMap = listActiveChapters(projectId).stream()
                .collect(Collectors.toMap(ReportChapter::getId, chapter -> chapter, (left, right) -> left, LinkedHashMap::new));
        validateAssignmentRequests(requests, chapterMap);

        // existingMap 用于判断当前分配是新增还是更新。
        List<ReportTaskAssignment> existing = listActiveAssignmentsByProject(projectId);
        Map<String, ReportTaskAssignment> existingMap = existing.stream()
                .collect(Collectors.toMap(this::assignmentKey, assignment -> assignment, (left, right) -> left, LinkedHashMap::new));
        LinkedHashSet<String> retainedKeys = new LinkedHashSet<>();
        List<ReportTaskAssignment> saved = new ArrayList<>();

        if (requests != null) {
            for (ReportTaskAssignmentSaveRequest request : requests) {
                String key = assignmentKey(request.getChapterId(), request.getAssigneeUserId());
                retainedKeys.add(key);
                ReportTaskAssignment assignment = existingMap.get(key);
                boolean created = assignment == null;
                if (created) {
                    // 新建任务分配。
                    assignment = new ReportTaskAssignment();
                    assignment.setProjectId(projectId);
                    assignment.setChapterId(request.getChapterId());
                    assignment.setAssigneeUserId(request.getAssigneeUserId());
                    EntityAuditSupport.touchCreate(assignment);
                } else {
                    // 修改已有任务分配。
                    EntityAuditSupport.touchUpdate(assignment);
                }
                assignment.setRoleType(request.getRoleType().trim());
                assignment.setDueDate(request.getDueDate());
                assignment.setAssignmentStatus(normalizeAssignmentStatus(request.getAssignmentStatus()));
                assignment.setCompletedAt("COMPLETED".equals(assignment.getAssignmentStatus()) ? LocalDateTime.now() : null);
                assignment.setRemark(normalizeText(request.getRemark()));
                if (created) {
                    reportTaskAssignmentService.save(assignment);
                } else {
                    reportTaskAssignmentService.updateById(assignment);
                }
                saved.add(assignment);
            }
        }

        // 发送分配通知，让责任人能第一时间收到任务
        // 这是任务分发闭环里很关键的一环。
        sendAssignmentNotice(project, saved, chapterMap);
        syncProjectSummary(projectId);
        return saved;
    }

    @Override
    @Transactional
    public ReportDraft saveDraft(Long chapterId, ReportDraftSaveRequest request) {
        // 草稿保存会同时回写章节内容和进度日志
        // 因此它不只是“存一段文本”，而是会联动多个业务对象。
        ReportChapter chapter = getRequiredChapter(chapterId);
        validateDraftRequest(request, chapter);
        ReportProject project = getRequiredProject(chapter.getProjectId());

        // 每保存一次草稿，就生成一个新版本。
        ReportDraft draft = new ReportDraft();
        draft.setChapterId(chapterId);
        draft.setVersionNo(nextDraftVersion(chapterId));
        draft.setDraftContent(request.getDraftContent().trim());
        draft.setEditedBy(request.getEditedBy());
        draft.setEditedAt(LocalDateTime.now());
        draft.setLockFlag(chapter.getLockedFlag() == null ? 0 : chapter.getLockedFlag());
        draft.setRemark(normalizeText(request.getRemark()));
        EntityAuditSupport.touchCreate(draft);
        reportDraftService.save(draft);

        // 最新草稿内容直接写回章节正文
        // 这样章节详情页可以直接显示当前版本正文。
        chapter.setContentText(draft.getDraftContent());
        chapter.setChapterStatus(resolveDraftChapterStatus(request.getChapterStatus(), request.getProgressPercent()));
        EntityAuditSupport.touchUpdate(chapter);
        reportChapterService.updateById(chapter);

        BigDecimal effectiveProgress = resolveEffectiveProgress(request.getProgressPercent(), chapter.getChapterStatus());
        if (effectiveProgress != null || StringUtils.hasText(request.getComment())) {
            // 进度和评论会写入进度日志，形成过程留痕。
            createProgressLog(project.getId(), chapter.getId(), request.getEditedBy(), effectiveProgress, request.getComment(), request.getRemark());
            // 任务状态也会随章节进度推进而联动更新。
            syncAssignmentsByProgress(chapter.getId(), request.getEditedBy(), effectiveProgress, chapter.getChapterStatus());
        }

        syncProjectSummary(project.getId());
        return draft;
    }

    @Override
    @Transactional
    public ReportDraft uploadDraft(Long chapterId, ReportDraftUploadRequest request, MultipartFile file) {
        // 上传型草稿只允许 txt / md / csv，避免把二进制文件直接塞进正文
        // 这里的限制能显著减少编码解析和存储格式混乱问题。
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Draft file is required");
        }
        String extension = fileExtension(file.getOriginalFilename());
        if (!Set.of("txt", "md", "csv").contains(extension)) {
            throw new IllegalArgumentException("Only txt, md and csv draft files are supported");
        }
        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Read draft file failed");
        }
        // 将上传文件内容转成普通草稿保存请求，复用 saveDraft 逻辑。
        ReportDraftSaveRequest draftRequest = new ReportDraftSaveRequest();
        draftRequest.setEditedBy(request == null ? null : request.getEditedBy());
        draftRequest.setDraftContent(content);
        draftRequest.setChapterStatus(request == null ? null : request.getChapterStatus());
        draftRequest.setProgressPercent(request == null ? null : request.getProgressPercent());
        draftRequest.setComment(request == null ? null : request.getComment());
        draftRequest.setRemark(request == null ? null : request.getRemark());
        return saveDraft(chapterId, draftRequest);
    }

    @Override
    @Transactional
    public ReportChapter lockChapter(Long chapterId, ReportChapterLockRequest request) {
        // 章节锁定后，所有草稿同步锁定，避免多人并行覆盖
        // 这能保证最终定稿阶段的文档一致性。
        ReportChapter chapter = getRequiredChapter(chapterId);
        int lockedFlag = request == null || request.getLockedFlag() == null ? 1 : request.getLockedFlag();
        if (lockedFlag != 0 && lockedFlag != 1) {
            throw new IllegalArgumentException("Locked flag must be 0 or 1");
        }
        chapter.setLockedFlag(lockedFlag);
        EntityAuditSupport.touchUpdate(chapter);
        reportChapterService.updateById(chapter);
        List<ReportDraft> drafts = reportDraftService.list(new LambdaQueryWrapper<ReportDraft>()
                .eq(ReportDraft::getChapterId, chapterId)
                .eq(ReportDraft::getIsDeleted, 0));
        drafts.forEach(draft -> {
            draft.setLockFlag(lockedFlag);
            EntityAuditSupport.touchUpdate(draft);
        });
        reportDraftService.updateBatchById(drafts);
        syncProjectSummary(chapter.getProjectId());
        return chapter;
    }

    @Override
    public String buildMergedReport(Long projectId) {
        // 合并导出时按章节树层级拼接 Markdown
        // 这里的导出内容可以直接作为最终报告预览或下载文本。
        ReportProject project = getRequiredProject(projectId);
        List<ReportChapter> chapters = listActiveChapters(projectId);
        Map<Long, List<ReportChapter>> childrenMap = chapters.stream()
                .collect(Collectors.groupingBy(chapter -> chapter.getParentId() == null ? 0L : chapter.getParentId(),
                        LinkedHashMap::new, Collectors.toList()));
        StringBuilder builder = new StringBuilder();
        builder.append("# ").append(project.getProjectName()).append(System.lineSeparator()).append(System.lineSeparator());
        appendChapterMarkdown(builder, childrenMap, 0L, 2);
        return builder.toString();
    }
}
```

### 4.3 Mapper

```xml
<mapper namespace="com.educationcertificationsystem.report.mapper.ReportProjectMapper">

    <!-- 报告项目主表分页条件 -->
    <sql id="Condition_Clause">
        rp.is_deleted = 0
        <if test="status != null and status != ''">
            AND rp.status = #{status}
        </if>
        <if test="ownerUserId != null">
            AND rp.owner_user_id = #{ownerUserId}
        </if>
        <if test="viewerUserId != null">
            AND (
                rp.owner_user_id = #{viewerUserId}
                OR EXISTS (
                    SELECT 1
                    FROM report_task_assignment rta
                    WHERE rta.project_id = rp.id
                      AND rta.assignee_user_id = #{viewerUserId}
                      AND rta.is_deleted = 0
                )
            )
        </if>
        <if test="keyword != null and keyword != ''">
            AND (
                rp.report_code LIKE CONCAT('%', #{keyword}, '%')
                OR rp.project_name LIKE CONCAT('%', #{keyword}, '%')
                OR rp.academic_year LIKE CONCAT('%', #{keyword}, '%')
            )
        </if>
    </sql>

    <!-- 报告项目列表：直接带出项目负责人和分页展示字段 -->
    <select id="selectPageByCondition" resultMap="BaseResultMap">
        SELECT rp.id,
               rp.report_code,
               rp.project_name,
               rp.academic_year,
               rp.semester_id,
               rp.owner_user_id,
               rp.generation_mode,
               rp.status,
               rp.total_chapters,
               rp.locked_flag,
               rp.exported_at,
               rp.created_at,
               rp.updated_at,
               rp.is_deleted,
               rp.remark
        FROM report_project rp
        WHERE <include refid="Condition_Clause"/>
        ORDER BY rp.updated_at DESC, rp.id DESC
        LIMIT #{offset}, #{size}
    </select>
</mapper>
```

## 5. 补充流程：F25 AI 辅助分析与结果写回

### 5.1 Controller 与 Service 入口

```java
// 这里只保留成员 E 最核心的两个改进建议接口
// 第一个接口负责“让 AI 生成建议”。
// 第二个接口负责“把用户确认后的 AI 建议写回正式业务数据”。
@PostMapping("/improve-suggestions/generate")
public Result<AiAnalysisResultVO> generateImprove(@RequestBody AiImproveSuggestionGenerateRequest request) {
    try {
        return Result.success(aiAssistantService.generateImproveSuggestion(request));
    } catch (IllegalArgumentException | IllegalStateException ex) {
        return Result.error(ex.getMessage());
    } catch (Exception ex) {
        return Result.error("Generate AI improve suggestion failed");
    }
}

@PostMapping("/improve-suggestions/confirm")
public Result<AiAnalysisResultVO> confirmImprove(@RequestBody AiImproveSuggestionConfirmRequest request) {
    try {
        return Result.success(aiAssistantService.confirmImproveSuggestion(request));
    } catch (IllegalArgumentException | IllegalStateException ex) {
        return Result.error(ex.getMessage());
    } catch (Exception ex) {
        return Result.error("Confirm AI improve suggestion failed");
    }
}
```

### 5.2 Service 核心确认逻辑

```java
@Override
@Transactional
public AiAnalysisResultVO confirmImproveSuggestion(AiImproveSuggestionConfirmRequest request) {
    // 1. 先确认请求和用户合法
    // 没有 requestId 或确认人时，系统无法知道要确认哪一条 AI 结果。
    if (request == null || request.getRequestId() == null) {
        throw new IllegalArgumentException("Request id is required");
    }
    validateUserId(request.getConfirmedBy(), "Confirm user is invalid");

    // 2. 读取 AI 请求和结果，必须是成功状态才允许确认回写
    // 这样可以避免把失败、空结果或错误场景的 AI 输出写入正式业务表。
    AiAnalysisRequest analysisRequest = getRequiredRequest(request.getRequestId());
    if (!SCENARIO_IMPROVE.equals(analysisRequest.getScenarioType())) {
        throw new IllegalArgumentException("AI request scenario is invalid");
    }
    AiAnalysisResult result = getRequiredResult(analysisRequest.getId());
    if (!STATUS_SUCCESS.equals(analysisRequest.getRequestStatus())) {
        throw new IllegalStateException("Only successful AI result can be confirmed");
    }

    // 3. 把 AI 输出拼成改进计划，再写回 ImprovePlanService
    Map<String, Object> metadata = parseJsonMap(analysisRequest.getRemark());
    JsonNode resultNode = readJsonNode(result.getResultJson());
    ImprovePlanSaveRequest saveRequest = new ImprovePlanSaveRequest();
    saveRequest.setPlanCode(StringUtils.hasText(request.getPlanCode())
            ? request.getPlanCode().trim()
            : "AIIP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
    saveRequest.setPlanName(StringUtils.hasText(request.getPlanName())
            ? request.getPlanName().trim()
            : readText(resultNode, "planName", "AI Improve Plan"));
    saveRequest.setSourceType(analysisRequest.getSourceType());
    saveRequest.setSourceId(analysisRequest.getSourceId());
    saveRequest.setTargetType(asString(metadata.get("targetType")));
    saveRequest.setTargetId(asLong(metadata.get("targetId")));
    saveRequest.setOwnerUserId(request.getOwnerUserId() != null
            // 如果前端明确传了负责人，则使用前端值；
            // 否则从 AI 请求上下文或确认人信息中兜底推断。
            ? request.getOwnerUserId()
            : fallbackOwnerUserId(metadata, request.getConfirmedBy()));
    saveRequest.setStartDate(request.getStartDate() == null ? LocalDate.now() : request.getStartDate());
    saveRequest.setDueDate(resolveImproveDueDate(request, metadata, resultNode));
    saveRequest.setPriority(requestPriority(metadata, resultNode));
    saveRequest.setRemark(StringUtils.hasText(request.getRemark())
            ? request.getRemark().trim()
            : readText(resultNode, "planSummary", "Generated by AI assistant"));
    saveRequest.setActions(buildActionRequests(resultNode, request, saveRequest));

    // 4. 这里并不是直接手写 SQL 落库，而是复用 ImprovePlanService 正式创建计划。
    // 这样 AI 写回与人工创建走的是同一套业务规则，数据更一致。
    ImprovePlanDetailVO created = improvePlanService.createPlan(saveRequest);
    // 5. 回写成功后，将 AI 请求标记为“已确认并写回”。
    markConfirmed(analysisRequest, result, request.getConfirmedBy(), "IMPROVE_PLAN:" + created.getId());
    return getDetail(analysisRequest.getId());
}
```

## 6. 结论

成员 E 的代码链路可以概括为：

- 问卷链路负责“设计 - 发布 - 异步投递 - 回收”
- 持续改进链路负责“计划 - 行动项 - 记录 - 状态同步”
- 自评报告链路负责“项目 - 章节 - 分配 - 草稿 - 合并导出”
- AI 链路负责“建议生成 - 确认回写 - 业务落库”

这四条链路都能形成完整的 `controller -> service -> impl -> mapper` 闭环，适合直接作为成员 E 的代码材料。
