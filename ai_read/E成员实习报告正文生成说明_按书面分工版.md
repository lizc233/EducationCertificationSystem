# E成员实习报告正文生成说明（按书面分工版）

## 1. 文档用途

这是一份专门给其他 AI 使用的说明文档。  
用途是：让其他 AI 在不了解项目背景的情况下，也能根据这份材料生成一篇**只围绕成员 E 书面分工**的本科实习报告正文。

注意：

- 这份文档以**书面分工**为准，不以实际临时分工为准。
- 本文只对应成员 E 的 5 项功能。
- 不修改你之前那份 D+E 合并版说明文档。

## 2. 本次必须采用的分工范围

成员 E 的书面分工是以下 5 项：

1. F21 问卷设计、发布与异步推送
2. F22 问卷填报与回收统计
3. F23 持续改进计划与改进记录
4. F24 自评报告任务与协同撰写
5. F25 AI 智能分析与报告辅助

生成报告正文时，必须明确写出：

- 本人在项目中负责的是**成员 E 模块**
- 负责范围是 **F21-F25**
- 不要把 D 模块的消息中心、达成度评价模型、课程目标达成度、毕业要求达成度、看板统计写成“本人主要负责”

可以提到这些模块与 E 模块存在接口依赖，但不能写成自己负责实现。

## 3. 生成正文时必须遵守的规则

1. 只生成“正文”，不要生成封面、目录、致谢、承诺书。
2. 不要虚构实习单位、实习地点、起止时间、指导教师、学号、班级、联系电话。
3. 个人信息和时间地点如果学校模板已有固定内容，必须以模板为准。
4. 报告风格要正式，适合本科实习报告，不要写成聊天口吻。
5. 不要把本人工作写成“只是做了几个页面”，必须写成完整业务闭环。
6. 不要编造没有实现的功能。
7. 可以写“完成了前后端联调、接口验证、问题修复和功能优化”。
8. 允许写与其他成员模块的协作关系，但主线必须是 F21-F25。

## 4. 项目背景

### 4.1 项目名称

工程教育专业认证智能服务系统

### 4.2 项目业务背景

该项目面向工程教育专业认证场景，依据工程教育认证相关标准，建设一套覆盖培养方案管理、课程管理、教学运行、课程考核评价、达成度评价、调查与评价、持续改进、自评报告和 AI 辅助的综合业务系统。

### 4.3 原始八大业务模块

1. 人才培养方案管理
2. 课程管理
3. 教学运行管理
4. 课程考核评价
5. 达成度评价
6. 调查与评价
7. 持续改进
8. 自评报告

### 4.4 小组书面分工

- 成员 A：F01-F05
- 成员 B：F06-F10
- 成员 C：F11-F15
- 成员 D：F16-F20
- 成员 E：F21-F25

本说明文档只服务于**成员 E 的书面分工报告写作**。

## 5. 技术栈与开发环境

### 5.1 后端技术

- Java 21
- Spring Boot 4.1.0
- Spring Web
- MyBatis-Plus 3.5.7
- MySQL
- Flyway
- RabbitMQ
- Jackson
- Hutool

### 5.2 AI 相关技术

- DeepSeek API
- Qdrant 向量数据库
- RAG 检索增强生成
- 本地向量生成服务

### 5.3 前后端联调事实

成员 E 对应的前端页面包括：

- `/survey`
- `/survey/fill`
- `/improve`
- `/report`
- `/ai`

这些页面不是纯静态展示页，而是与后端接口完成了真实联调。

## 6. 成员 E 五项功能的真实材料

---

## 6.1 F21 问卷设计、发布与异步推送

### 业务目标

面向毕业生、在校生、教师、用人单位等对象设计调查问卷，并支持配置范围、起止时间、发布、撤回、结束和提醒等操作。  
同时采用 MQ 异步投递方式完成批量通知，避免同步发布时阻塞主业务。

### 已实现的核心能力

- 问卷分页查询
- 问卷详情查看
- 问卷预览
- 发布任务记录分页
- 新建问卷
- 修改问卷
- 删除问卷
- 发布问卷
- 重试发布
- 撤回问卷
- 结束问卷
- 截止提醒

### 支持的题型

- `SINGLE` 单选题
- `MULTIPLE` 多选题
- `SCALE` 量表题
- `TEXT` 填空题
- `MATRIX` 矩阵题

### 支持的投放范围

- `ROLE` 角色
- `GRADE` 年级
- `CLASS` 班级
- `MAJOR` 专业
- `USER` 用户

### 支持的目标对象

- `STUDENT`
- `IN_SCHOOL_STUDENT`
- `GRADUATE`
- `TEACHER`
- `EMPLOYER`
- `ALL`

### 关键接口

- `GET /api/surveys/questionnaires`
- `GET /api/surveys/questionnaires/{id}`
- `GET /api/surveys/questionnaires/{id}/preview`
- `GET /api/surveys/questionnaires/{id}/publish-tasks`
- `POST /api/surveys/questionnaires`
- `PUT /api/surveys/questionnaires/{id}`
- `DELETE /api/surveys/questionnaires/{id}`
- `POST /api/surveys/questionnaires/{id}/publish`
- `POST /api/surveys/questionnaires/{id}/retry-publish`
- `POST /api/surveys/questionnaires/{id}/revoke`
- `POST /api/surveys/questionnaires/{id}/end`
- `POST /api/surveys/questionnaires/{id}/deadline-reminder`

### 技术实现要点

1. 保存问卷时，不只保存主表，还会同时保存：
   - 问卷范围
   - 题目
   - 选项
   - 矩阵行
   - 矩阵列
2. 更新问卷时，为了避免旧子项残留，采用“清理旧子项后重建”的方式保持结构一致。
3. 发布前会校验：
   - 问卷状态是否允许发布
   - 问卷是否包含题目
   - 范围是否合法
4. 发布时不会直接同步逐个通知用户，而是：
   - 创建发布任务
   - 事务提交后发送 MQ 事件
   - 由 `SurveyPublishListener` 消费消息
   - 解析目标接收用户
   - 调用通知服务发送站内通知
5. 若范围内无匹配接收用户，不再让整个发布流程硬失败，而是记录状态并正常结束当前任务。

### 报告里可强调的点

- 该模块实现的是“问卷设计 + 问卷结构维护 + 发布任务 + MQ 异步通知”的完整闭环。
- 不是简单做一个表单录入页面。

---

## 6.2 F22 问卷填报与回收统计

### 业务目标

支持用户在线填写问卷，并提供回收率统计、题目统计、答卷分页、答卷详情和 Excel 导出功能，为调查与评价模块提供完整的数据回收闭环。

### 已实现的核心能力

- 获取填报视图
- 提交问卷答卷
- 答卷分页
- 答卷详情
- 回收概览统计
- 按题统计
- 导出答卷数据
- 下载 Excel

### 关键接口

- `GET /api/surveys/questionnaires/{questionnaireId}/fill`
- `POST /api/surveys/questionnaires/{questionnaireId}/responses/submit`
- `GET /api/surveys/questionnaires/{questionnaireId}/responses`
- `GET /api/surveys/questionnaires/{questionnaireId}/responses/{responseId}`
- `GET /api/surveys/questionnaires/{questionnaireId}/response-stats/overview`
- `GET /api/surveys/questionnaires/{questionnaireId}/response-stats/questions`
- `GET /api/surveys/questionnaires/{questionnaireId}/export/responses`
- `GET /api/surveys/questionnaires/{questionnaireId}/download/responses`

### 填报校验逻辑

在提交前会校验：

1. 问卷必须存在
2. 当前问卷状态必须是 `PUBLISHED`
3. 当前时间必须在问卷开始时间和结束时间之间
4. 当前用户必须在问卷投放范围内
5. 同一用户不能重复提交

### 不同题型的答案校验

- 文本题：必填时必须填写文本或数值
- 单选题：只能选择 1 个选项
- 多选题：要满足最少/最多选择数
- 矩阵题：必须校验行列组合合法

### 统计实现能力

- 统计目标人数、已提交人数、待提交人数
- 计算回收率
- 单选/多选/量表题按选项统计人数和比例
- 矩阵题按单元格统计次数和比例
- 文本题汇总文本答案

### 导出实现能力

- 把一份答卷展开成一行
- 每一列对应一个问题
- 支持匿名问卷脱敏

### 联调中发现并修复的真实问题

- 问卷未到开始时间时，前端不应继续发提交请求
- 已通过 `canSubmit` 和 `submitMessage` 与前端联动
- 提交接口增加了 `respondentUserId` 和用户有效性校验
- 问卷提交错误信息从底层异常改为更清晰的业务提示

### 报告里可强调的点

- 该模块不仅实现了答卷提交，还完成了统计分析和结果导出。
- 形成了“发布 -> 填报 -> 回收 -> 统计 -> 导出”的完整业务闭环。

---

## 6.3 F23 持续改进计划与改进记录

### 业务目标

针对问卷结论、课程目标结果或毕业要求问题，建立持续改进计划，并对执行、完成、验证、提醒和记录等全过程进行管理。

### 已实现的核心能力

- 改进计划分页
- 超期计划分页
- 计划详情
- 新建计划
- 修改计划
- 删除计划
- 启动计划
- 完成计划
- 验证计划
- 发送提醒
- 更新措施进度
- 新增记录
- 修改记录
- 删除记录

### 关键接口

- `GET /api/improve/plans`
- `GET /api/improve/plans/overdue`
- `GET /api/improve/plans/{id}`
- `POST /api/improve/plans`
- `PUT /api/improve/plans/{id}`
- `DELETE /api/improve/plans/{id}`
- `POST /api/improve/plans/{id}/start`
- `POST /api/improve/plans/{id}/complete`
- `POST /api/improve/plans/{id}/verify`
- `POST /api/improve/plans/{id}/remind`
- `POST /api/improve/plans/actions/{actionId}/progress`
- `POST /api/improve/plans/actions/{actionId}/records`
- `PUT /api/improve/plans/records/{recordId}`
- `DELETE /api/improve/plans/records/{recordId}`

### 模块特点

1. 改进计划不是单一表结构，而是包含：
   - 计划主表
   - 改进行动
   - 改进记录
2. 支持按状态流转：
   - `PENDING`
   - `IN_PROGRESS`
   - `COMPLETED`
   - `VERIFIED`
3. 支持超期数据筛选，便于追踪执行情况。
4. 支持提醒功能，用于推动责任人及时处理改进事项。

### 报告里可强调的点

- 这个模块体现了工程教育认证中的“持续改进闭环”思想。
- 把问题分析结果真正转化为后续可执行、可跟踪、可验证的改进方案。

---

## 6.4 F24 自评报告任务与协同撰写

### 业务目标

围绕专业认证自评报告的章节协同写作需求，实现报告项目管理、章节树管理、任务分配、草稿维护、进度看板、章节锁定和合并导出。

### 已实现的核心能力

- 报告项目分页
- 项目详情
- 新建项目
- 修改项目
- 删除项目
- 保存章节树
- 保存任务分配
- 查看章节草稿列表
- 保存章节草稿
- 上传草稿文件
- 锁定/解锁章节
- 保存进度日志
- 获取进度看板
- 生成章节初稿
- 预览合并报告
- 下载合并 Markdown

### 关键接口

- `GET /api/reports/projects`
- `GET /api/reports/projects/{id}`
- `POST /api/reports/projects`
- `PUT /api/reports/projects/{id}`
- `DELETE /api/reports/projects/{id}`
- `PUT /api/reports/projects/{id}/chapters`
- `PUT /api/reports/projects/{id}/assignments`
- `GET /api/reports/projects/chapters/{chapterId}/drafts`
- `POST /api/reports/projects/chapters/{chapterId}/drafts`
- `POST /api/reports/projects/chapters/{chapterId}/drafts/upload`
- `POST /api/reports/projects/chapters/{chapterId}/lock`
- `POST /api/reports/projects/{id}/progress-logs`
- `GET /api/reports/projects/{id}/progress-board`
- `POST /api/reports/projects/{id}/generate-drafts`
- `GET /api/reports/projects/{id}/export/preview`
- `GET /api/reports/projects/{id}/download/merged`

### 技术实现要点

1. 报告章节采用树形结构，而不是简单平铺。
2. 任务分配以章节为粒度，支持不同负责人协同撰写。
3. 草稿采用版本化保存，便于跟踪多次编辑过程。
4. 锁定章节后，会同步更新相关草稿的锁定状态。
5. 进度日志和任务状态联动，支持项目级进度板查看。
6. 合并导出时，会按章节层级自动拼接 Markdown 内容。

### 章节初稿自动生成支持的来源类型

- 培养目标
- 毕业要求
- 课程
- 问卷
- 通用来源

### 报告里可强调的点

- 该模块不是普通文档上传模块，而是“章节树 + 协同分工 + 草稿版本 + 进度跟踪 + 合并导出”的完整协同撰写系统。

---

## 6.5 F25 AI 智能分析与报告辅助

### 业务目标

把 AI 能力嵌入业务闭环，支持报告章节智能扩写/润色，以及持续改进建议的智能生成与确认回写。

### 本项目已落地的两个 AI 场景

1. 报告章节 AI 扩写/润色
2. 持续改进建议 AI 生成与回写

### 已实现的接口能力

- 报告章节生成
  - `POST /api/ai/reports/chapters/{chapterId}/generate`
- 报告章节确认回写
  - `POST /api/ai/reports/chapters/{chapterId}/confirm`
- 改进建议生成
  - `POST /api/ai/improve-suggestions/generate`
- 改进建议确认回写
  - `POST /api/ai/improve-suggestions/confirm`
- AI 请求重试
  - `POST /api/ai/requests/{requestId}/retry`
- AI 请求历史分页
  - `GET /api/ai/requests`
- AI 请求详情
  - `GET /api/ai/requests/{requestId}`
- 知识库重建
  - `POST /api/ai/knowledge/rebuild`

### AI 场景一：报告章节扩写/润色

完整流程：

1. 用户选择报告章节
2. 发起 AI 生成请求
3. 后端读取章节内容，构造业务上下文
4. 自动重建报告项目知识索引
5. 对上下文生成向量
6. 从 Qdrant 检索相关知识片段
7. 将业务上下文与检索片段填入 Prompt 模板
8. 调用 DeepSeek 生成中文内容
9. 保存 AI 请求与 AI 结果
10. 用户确认后把结果回写到报告草稿

### AI 场景二：持续改进建议生成

完整流程：

1. 用户选择问题来源
2. 发起 AI 建议生成请求
3. 后端构造来源、目标、优先级、时限等上下文
4. 检索已有改进计划与报告知识
5. 调用大模型生成 JSON 结构化建议
6. 用户确认后，自动落地为改进计划与行动项

### RAG 知识库实现事实

新增知识库表：

- `ai_knowledge_document`
- `ai_knowledge_chunk`

Flyway 脚本中加入默认 Prompt 模板：

- `REPORT_CHAPTER_EXPAND`
- `REPORT_CHAPTER_POLISH`
- `IMPROVE_PLAN_SUGGEST`

### 知识索引实现要点

1. 将报告章节或改进计划整理为结构化文本。
2. 计算内容校验码，避免重复索引。
3. 将文本分块为多个 chunk。
4. 为每个 chunk 生成向量。
5. 写入 Qdrant。
6. 保存 chunk 元数据，用于检索结果溯源。

### AI 接入过程中解决的问题

- RabbitMQ 在本机和 Docker 下地址不同，需要拆分配置
- Qdrant 在本机和 Docker 下访问地址不同
- AI 模块缺少 `ObjectMapper` Bean 导致服务装配失败
- `application-dev.yml` 存在重复 `app:` 段，导致配置覆盖问题
- Flyway 依赖方式需要调整，才能让 AI 相关迁移脚本正常参与启动

### 报告里可强调的点

- F25 不是一个“聊天框功能”
- 而是“知识索引 -> 向量检索 -> Prompt 组装 -> 大模型生成 -> 人工确认 -> 业务回写”的完整闭环

## 7. 与其他模块的协作关系

报告中可以写清楚以下协作关系，但不要把对方模块写成自己负责：

1. F21 问卷发布和 F23/F24 提醒，依赖通知能力实现站内消息推送。
2. F23 持续改进计划可引用问卷结论、达成度问题等作为来源。
3. F24 自评报告的部分章节可引用系统内已有业务数据生成初稿。
4. F25 AI 场景与 F23、F24 高度联动：
   - 为报告章节扩写或润色
   - 为持续改进自动生成建议

## 8. 前后端联调与运行验证事实

可以写入报告的真实内容包括：

1. 已完成成员 E 对应页面与后端接口联调。
2. 问卷管理、问卷填报、持续改进、自评报告、AI 助手页面都接入了真实后端。
3. 联调中发现并修复了问卷保存、问卷提交、前端响应判断、时间窗口校验等问题。
4. AI 请求历史、AI 结果详情、确认回写流程已形成闭环。
5. 前端构建曾通过 `npm run build` 验证。

## 9. 报告正文建议结构

如果学校模板标题不同，优先套学校模板，但内容逻辑建议按下面展开。

### 第一部分：实习背景与项目简介

建议写：

- 实习目的
- 项目背景
- 系统建设目标
- 本人在项目中的职责范围：成员 E，负责 F21-F25

### 第二部分：需求分析与任务分解

建议写：

- 项目整体模块介绍
- 小组书面分工
- 成员 E 的业务边界
- 为什么调查评价、持续改进、自评报告、AI 辅助属于一个连续闭环

### 第三部分：系统设计与技术方案

建议写：

- 前后端分离架构
- Spring Boot + MyBatis-Plus + MySQL
- RabbitMQ 异步发布与提醒
- DeepSeek + Qdrant + RAG
- Markdown 合并导出

### 第四部分：本人负责模块的实现

这是正文重点，建议按 5 节写：

1. F21 问卷设计、发布与异步推送
2. F22 问卷填报与回收统计
3. F23 持续改进计划与改进记录
4. F24 自评报告任务与协同撰写
5. F25 AI 智能分析与报告辅助

每一节建议写：

1. 业务目标
2. 核心数据对象
3. 接口设计
4. 实现逻辑
5. 难点与优化

### 第五部分：关键问题与解决过程

建议重点写：

1. 问卷发布异步化问题
2. 问卷填报时间窗口和重复提交控制问题
3. 报告章节树、草稿版本和任务协同问题
4. AI 接入、知识重建、向量检索和配置问题

### 第六部分：测试与联调

建议写：

- 接口测试
- 页面联调
- 正常流程验证
- 异常分支验证
- 导出验证
- AI 确认回写验证

### 第七部分：实习总结

建议写：

- 对工程教育认证业务理解更深
- 对问卷、持续改进、自评报告、AI 业务闭环认识更完整
- 掌握了 RabbitMQ、RAG、AI 回写、协同文档管理等综合能力
- 提升了需求分析、接口设计、异常处理和联调排错能力

## 10. 图怎么画

这里只告诉你画什么，不直接出图。

### 图 1：成员 E 模块总体架构图

#### 画法

从左到右画四层：

1. 前端层
   - 问卷管理
   - 问卷填报
   - 持续改进
   - 报告协同
   - AI 助手
2. 控制层
   - Survey Controller
   - Improve Controller
   - Report Controller
   - AI Controller
3. 服务层
   - SurveyQuestionnaireService
   - SurveyResponseService
   - ImprovePlanService
   - ReportProjectService
   - AiAssistantService
   - AiKnowledgeIndexService
4. 基础设施层
   - MySQL
   - RabbitMQ
   - Qdrant
   - DeepSeek API

#### 图中重点标注

- F21 通过 RabbitMQ 异步发布
- F24 与 F25 联动
- F25 使用 Qdrant 与 DeepSeek

---

### 图 2：问卷发布异步流程图

#### 画法

1. 管理员设计问卷
2. 保存问卷结构
3. 点击发布
4. 创建发布任务
5. 事务提交后发送 MQ 事件
6. `SurveyPublishListener` 消费
7. 解析用户范围
8. 发送站内通知
9. 更新任务状态

适合画泳道图。

---

### 图 3：问卷填报与回收统计流程图

#### 画法

1. 用户进入填报页
2. 查询问卷可提交状态
3. 校验时间与用户范围
4. 提交答案
5. 保存答卷
6. 管理员查看回收概览
7. 查看按题统计
8. 导出 Excel

---

### 图 4：持续改进闭环流程图

#### 画法

1. 问题来源
   - 问卷结论
   - 达成度问题
2. 新建改进计划
3. 分解改进行动
4. 执行进度更新
5. 记录执行证据
6. 完成计划
7. 验证效果
8. 形成闭环

---

### 图 5：自评报告协同撰写流程图

#### 画法

1. 创建报告项目
2. 建立章节树
3. 分配章节任务
4. 编写草稿
5. 保存进度日志
6. 锁定章节
7. 合并 Markdown
8. 导出完整报告

---

### 图 6：F25 AI-RAG 业务闭环图

#### 画法

第一段：知识准备

1. 报告章节/改进计划原始内容
2. 生成知识文档
3. 文本分块
4. 向量生成
5. Qdrant 建索引

第二段：AI 生成

1. 用户发起请求
2. 构造业务上下文
3. 检索相关 chunk
4. 组装 Prompt
5. 调用 DeepSeek
6. 保存 AI 请求和结果

第三段：确认回写

1. 用户查看结果
2. 点击确认
3. 回写报告草稿或改进计划
4. 更新状态为已确认

## 11. 建议插入的表格

### 表 1：成员 E 负责功能清单

列建议：

- 功能编号
- 功能名称
- 主要业务内容
- 关键技术

### 表 2：成员 E 模块关键技术用途表

列建议：

- 技术
- 对应功能
- 用途

可写示例：

- RabbitMQ：F21，用于问卷异步发布和提醒
- MyBatis-Plus：F21-F25，用于数据持久化
- Qdrant：F25，用于知识片段向量检索
- DeepSeek：F25，用于报告生成和改进建议生成
- Markdown：F24，用于合并导出报告

## 12. 可直接复制给其他 AI 的生成指令

```text
请根据以下事实材料，为我生成一篇正式的本科实习报告正文，中文输出，风格正式，重点写我在“工程教育专业认证智能服务系统”项目中作为成员 E 负责的书面分工任务。

硬性要求：
1. 只生成正文，不要封面、目录、致谢。
2. 不要虚构实习单位、地点、起止时间、指导教师、学号等未提供信息。
3. 报告重点只能围绕成员 E 的五项功能：F21-F25。
4. 不要把成员 D 的功能写成我主要负责。
5. 要写出完整的软件工程实现过程，包括需求、设计、接口、实现逻辑、联调、问题修复和总结。
6. 必须写清楚问卷发布异步流程、问卷填报统计、持续改进闭环、自评报告协同、AI-RAG 回写闭环。
7. 不要空话套话，尽量写真实实现逻辑。

项目事实材料如下：

[把本说明文档第 4 到第 11 部分贴给 AI]
```

## 13. 最后提醒

1. 这份文档只用于“成员 E 书面分工版”报告生成。
2. 学校模板中的个人信息、时间、地点必须人工核对。
3. 如果学校要求图号，如“图 3-1”，按章节重新编号即可。
4. 如果需要扩写篇幅，优先扩写：
   - F21/F22 问卷完整闭环
   - F24 报告协同写作
   - F25 AI-RAG 场景实现
