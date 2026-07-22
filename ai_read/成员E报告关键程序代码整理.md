# 成员E报告关键程序代码整理

本文档用于辅助撰写实习报告中的“程序代码实现”部分，仅整理成员 E 负责的 F21-F25 功能对应的关键代码文件、核心方法和主要作用，便于在报告中简要说明实现内容。

## 1. 成员E关键程序代码概览

| 功能编号 | 功能名称 | 关键代码文件 |
|---|---|---|
| F21 | 问卷设计、发布与异步推送 | `SurveyQuestionnaireServiceImpl.java`、`SurveyPublishListener.java` |
| F22 | 问卷填报与回收统计 | `SurveyResponseServiceImpl.java` |
| F23 | 持续改进计划与改进记录 | `ImprovePlanServiceImpl.java` |
| F24 | 自评报告任务与协同撰写 | `ReportProjectServiceImpl.java` |
| F25 | AI 智能分析与报告辅助 | `AiAssistantServiceImpl.java`、`AiKnowledgeIndexServiceImpl.java` |

## 2. 问卷模块关键代码

### 2.1 `src/main/java/com/educationcertificationsystem/survey/service/impl/SurveyQuestionnaireServiceImpl.java`

| 核心方法 | 主要作用 | 对应功能 |
|---|---|---|
| `createQuestionnaire` | 创建问卷主信息，同时保存问卷适用范围与题目内容 | F21 |
| `updateQuestionnaire` | 修改问卷配置，更新范围、题目、选项等结构数据 | F21 |
| `publish` | 对问卷执行正式发布，生成发布任务并进入推送流程 | F21 |
| `retryPublish` | 对失败或异常的发布任务进行重新发布处理 | F21 |
| `sendDeadlineReminder` | 对已发布问卷发送截止前提醒通知 | F21 |
| `handlePublishEvent` | 处理 MQ 发布事件，组织接收对象并发送通知 | F21 |
| `resolveTargetUserIds` | 依据问卷发布范围解析实际接收用户列表 | F21 |
| `replaceScopes`、`replaceQuestions` | 重建问卷范围和题目明细，保证问卷结构一致性 | F21 |
| `validateCanPublish` | 校验问卷是否满足发布条件，例如状态、题目完整性等 | F21 |
| `buildNoticeRequest` | 组装通知中心发送所需的消息内容 | F21 |

### 2.2 `src/main/java/com/educationcertificationsystem/mq/SurveyPublishListener.java`

| 核心方法 | 主要作用 | 对应功能 |
|---|---|---|
| `onPublish` | 监听问卷发布消息队列，收到事件后转交问卷服务处理 | F21 |

### 2.3 `src/main/java/com/educationcertificationsystem/survey/service/impl/SurveyResponseServiceImpl.java`

| 核心方法 | 主要作用 | 对应功能 |
|---|---|---|
| `getFillView` | 生成问卷填报页面所需数据，包括题目、选项和填写状态 | F22 |
| `submitResponse` | 提交答卷并保存答题结果，是问卷回收的核心入口 | F22 |
| `pageByCondition` | 分页查询问卷回收记录，支持后台查看填报明细 | F22 |
| `getOverview` | 汇总问卷回收数量、提交状态等统计信息 | F22 |
| `getQuestionStats` | 统计单题作答情况，为图表分析和结果展示提供数据 | F22 |
| `exportResponses` | 导出问卷回收明细数据 | F22 |
| `buildExportRows` | 将答卷数据转换为导出表格行结构 | F22 |
| `validateFillAvailability` | 校验当前用户是否允许填写问卷 | F22 |
| `validateSubmitAnswers` | 校验提交答案是否符合题型和必填规则 | F22 |

## 3. 持续改进模块关键代码

### `src/main/java/com/educationcertificationsystem/improve/service/impl/ImprovePlanServiceImpl.java`

| 核心方法 | 主要作用 | 对应功能 |
|---|---|---|
| `createPlan` | 创建改进计划主信息及行动项 | F23 |
| `updatePlan` | 修改改进计划及其行动项 | F23 |
| `startPlan` | 启动改进计划，推进计划进入执行状态 | F23 |
| `completePlan` | 完成改进计划并更新计划状态 | F23 |
| `verifyPlan` | 对改进结果进行审核确认 | F23 |
| `sendReminder` | 向责任人发送改进计划提醒消息 | F23 |
| `updateActionProgress` | 更新单个行动项进度并同步计划状态 | F23 |
| `addRecord`、`updateRecord` | 维护改进过程记录和说明信息 | F23 |
| `syncPlanStatusFromActions` | 根据行动项完成情况自动同步计划状态 | F23 |
| `bindAttachment` | 将附件与改进记录建立关联 | F23 |
| `buildReminderContent` | 生成改进提醒通知正文 | F23 |

## 4. 自评报告模块关键代码

### `src/main/java/com/educationcertificationsystem/report/service/impl/ReportProjectServiceImpl.java`

| 核心方法 | 主要作用 | 对应功能 |
|---|---|---|
| `createProject` | 创建自评报告项目主信息 | F24 |
| `saveChapterTree` | 保存报告章节树结构 | F24 |
| `saveAssignments` | 保存章节任务分配信息，并向相关人员发送通知 | F24 |
| `saveDraft` | 保存章节草稿内容和进度状态 | F24 |
| `uploadDraft` | 上传附件形式的草稿并回写章节草稿信息 | F24 |
| `lockChapter` | 对章节进行锁定或解锁，支持协同撰写控制 | F24 |
| `saveProgress` | 记录项目推进过程中的进度日志 | F24 |
| `getProgressBoard` | 获取报告项目的整体进度看板数据 | F24 |
| `generateInitialDrafts` | 批量生成初始章节草稿 | F24 |
| `buildMergedReport` | 按章节树合并生成完整报告文本 | F24 |
| `sendAssignmentNotice` | 发送章节任务分配通知 | F24 |
| `buildInitialDraftContent`、`appendChapterMarkdown` | 生成初始内容并按层级拼接成最终文档 | F24 |

## 5. AI 辅助模块关键代码

### 5.1 `src/main/java/com/educationcertificationsystem/ai/service/impl/AiAssistantServiceImpl.java`

| 核心方法 | 主要作用 | 对应功能 |
|---|---|---|
| `generateReportAssistant` | 针对报告章节发起 AI 扩写或润色请求 | F25 |
| `confirmReportAssistant` | 将人工确认后的 AI 结果写入报告草稿 | F25 |
| `generateImproveSuggestion` | 依据业务数据生成 AI 改进建议 | F25 |
| `confirmImproveSuggestion` | 将 AI 建议确认后转化为正式改进计划 | F25 |
| `retry` | 对失败或需重试的 AI 请求再次执行 | F25 |
| `pageHistory` | 查询 AI 请求历史记录 | F25 |
| `getDetail` | 查看单次 AI 分析的详细结果 | F25 |
| `executeReportRequest` | 执行报告辅助 AI 调用，组织上下文、提示词与结果保存 | F25 |
| `executeImproveRequest` | 执行改进建议 AI 调用，并保存结构化建议结果 | F25 |
| `saveSuccessResult`、`saveFailedResult` | 保存 AI 成功或失败的执行结果 | F25 |
| `markConfirmed` | 标记 AI 结果已被人工确认并记录回写目标 | F25 |
| `retrieveReportChunks`、`retrieveImproveChunks` | 从知识库检索与当前请求相关的上下文片段 | F25 |
| `renderTemplate` | 将业务上下文与知识片段填充到提示词模板中 | F25 |

### 5.2 `src/main/java/com/educationcertificationsystem/ai/service/impl/AiKnowledgeIndexServiceImpl.java`

| 核心方法 | 主要作用 | 对应功能 |
|---|---|---|
| `rebuild` | 按范围重建 AI 知识库索引 | F25 |
| `rebuildReportProjectKnowledge` | 将报告项目章节数据重建为知识文档与向量片段 | F25 |
| `rebuildImprovePlanKnowledge` | 将改进计划数据重建为知识文档与向量片段 | F25 |
| `rebuildAll` | 全量重建报告与改进相关知识索引 | F25 |
| `upsertReportChapterDocument` | 将章节内容写入 AI 知识文档 | F25 |
| `upsertImprovePlanDocument` | 将改进计划内容写入 AI 知识文档 | F25 |
| `reindexChunks` | 对知识文档分片、生成向量并写入向量库 | F25 |
| `splitIntoChunks` | 将长文本切分为可检索的知识片段 | F25 |
| `buildReportChapterContent`、`buildImprovePlanContent` | 抽取业务对象文本内容作为知识源 | F25 |

## 6. 可直接写进报告的程序代码说明

可参考如下表述写入“程序代码实现”部分：

> 在程序实现方面，我负责的成员 E 模块主要包括问卷管理、持续改进、自评报告协同编写和 AI 辅助分析四部分。问卷模块在 `SurveyQuestionnaireServiceImpl` 中完成了问卷创建、题目维护、发布校验、异步发布和提醒通知等核心逻辑，并通过 `SurveyPublishListener` 实现消息队列异步推送；问卷填报与统计逻辑主要封装在 `SurveyResponseServiceImpl` 中，完成了填写校验、答卷提交、统计分析和导出功能。持续改进部分在 `ImprovePlanServiceImpl` 中实现了改进计划创建、进度同步、记录维护和提醒通知。自评报告部分在 `ReportProjectServiceImpl` 中实现了章节树维护、任务分配、草稿保存、进度看板和报告合并输出。AI 辅助部分则在 `AiAssistantServiceImpl` 和 `AiKnowledgeIndexServiceImpl` 中实现了提示词组织、知识检索、AI 结果确认回写以及知识索引重建等关键流程。

## 7. 报告中建议重点展示的代码点

如果报告里只打算列出少量“代表性程序代码”，建议优先选下面这些位置：

| 建议展示代码 | 原因 |
|---|---|
| `SurveyQuestionnaireServiceImpl.publish` | 能体现问卷发布主流程和业务校验 |
| `SurveyQuestionnaireServiceImpl.handlePublishEvent` | 能体现异步消息处理逻辑 |
| `SurveyResponseServiceImpl.submitResponse` | 能体现问卷提交与数据落库逻辑 |
| `ImprovePlanServiceImpl.updateActionProgress` | 能体现改进过程的状态同步逻辑 |
| `ReportProjectServiceImpl.saveAssignments` | 能体现任务协同分配逻辑 |
| `ReportProjectServiceImpl.buildMergedReport` | 能体现报告章节合并输出逻辑 |
| `AiAssistantServiceImpl.generateReportAssistant` | 能体现 AI 辅助生成主流程 |
| `AiAssistantServiceImpl.confirmImproveSuggestion` | 能体现 AI 结果确认并回写业务对象 |
| `AiKnowledgeIndexServiceImpl.reindexChunks` | 能体现知识分片与向量索引实现 |

## 8. 可直接放进报告的代码段

### 8.1 问卷发布核心代码

文件位置：`SurveyQuestionnaireServiceImpl.java`

```java
@Override
@Transactional
public SurveyQuestionnaire publish(Long id, SurveyDispatchRequest request) {
    SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(id);
    validateCanPublish(questionnaire);
    return createTaskAndDispatch(questionnaire, SurveyMqConstants.TASK_ACTION_PUBLISH, request);
}
```

说明：该代码体现了问卷发布的主入口，先校验问卷状态和题目完整性，再创建发布任务并进入异步推送流程。

### 8.2 问卷提交核心代码

文件位置：`SurveyResponseServiceImpl.java`

```java
String availabilityError = validateFillAvailability(questionnaire, request.getRespondentUserId(), true);
if (availabilityError != null) {
    throw new IllegalStateException(availabilityError);
}
SurveyQuestionnaireDetailVO detail = surveyQuestionnaireService.getDetail(questionnaireId);
Map<Long, SurveyQuestionDetailVO> questionMap = detail.getQuestions().stream()
        .collect(Collectors.toMap(SurveyQuestionDetailVO::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
Map<Long, SurveySubmitAnswerRequest> requestAnswerMap = normalizeAnswerMap(request.getAnswers());
validateSubmitAnswers(detail.getQuestions(), requestAnswerMap);
```

说明：该代码体现了问卷填报前的数据校验过程，包括填写资格校验、题目映射构造和答案合法性校验。

### 8.3 改进行动进度同步代码

文件位置：`ImprovePlanServiceImpl.java`

```java
if (request.getProgressPercent() != null) {
    validateProgress(request.getProgressPercent());
    action.setProgressPercent(request.getProgressPercent());
}
if (action.getProgressPercent() != null && action.getProgressPercent().compareTo(new BigDecimal("100")) >= 0
        && !ACTION_VERIFIED.equals(action.getStatus())) {
    action.setStatus(ACTION_COMPLETED);
}
improvePlanActionService.updateById(action);
syncPlanStatusFromActions(action.getPlanId(), false);
```

说明：该代码体现了持续改进模块中“行动项进度变化带动计划状态同步”的实现思路。

### 8.4 自评报告合并输出代码

文件位置：`ReportProjectServiceImpl.java`

```java
StringBuilder builder = new StringBuilder();
builder.append("# ").append(project.getProjectName()).append(System.lineSeparator()).append(System.lineSeparator());
builder.append("- Report Code: ").append(project.getReportCode()).append(System.lineSeparator());
builder.append("- Academic Year: ").append(project.getAcademicYear()).append(System.lineSeparator());
builder.append("- Generation Mode: ").append(project.getGenerationMode()).append(System.lineSeparator()).append(System.lineSeparator());
appendChapterMarkdown(builder, childrenMap, 0L, 2);
return builder.toString();
```

说明：该代码体现了系统按章节树结构自动拼接完整自评报告文本的实现方式。

### 8.5 AI 报告辅助生成代码

文件位置：`AiAssistantServiceImpl.java`

```java
ReportChapter chapter = getRequiredChapter(chapterId);
String scenarioType = resolveReportScenario(request == null ? null : request.getOperationType());
String templateCode = StringUtils.hasText(request == null ? null : request.getTemplateCode())
        ? request.getTemplateCode().trim().toUpperCase(Locale.ROOT)
        : scenarioType;
AiAnalysisRequest analysisRequest = initRequest(
        scenarioType, "REPORT_CHAPTER", chapterId,
        getRequiredTemplate(templateCode).getId(),
        request.getRequesterUserId(), buildReportMetadata(chapter, request));
aiAnalysisRequestService.save(analysisRequest);
executeReportRequest(analysisRequest, chapter, request);
```

说明：该代码体现了 AI 报告辅助的主流程，包括场景识别、模板选择、请求初始化和 AI 执行调用。

## 9. 说明

- 本文档只保留成员 E 负责范围内最适合写入报告正文的关键代码。
- 这里强调“代码职责”和“实现位置”，不展开大段源码，便于直接转写为实习报告内容。
- 如果你还需要，我可以继续在这个基础上给你补一版“成员 E 的需求分析 + 软件设计 + 算法流程说明”的报告素材。
