# 成员E报告数据库表整理

本文档用于辅助撰写实习报告中“数据库设计/核心数据表”相关内容，范围只保留成员 E 负责的 F21-F25 功能，不展开过多字段，按报告中常见的简洁写法整理。

## 1. 成员E负责功能对应的核心表

| 功能编号 | 功能名称 | 核心数据表 |
|---|---|---|
| F21 | 问卷设计、发布与异步推送 | `survey_questionnaire`、`survey_question`、`survey_question_option`、`survey_publish_task` |
| F22 | 问卷填报与回收统计 | `survey_response`、`survey_response_answer` |
| F23 | 持续改进计划与改进记录 | `improve_plan`、`improve_plan_action`、`improve_plan_record` |
| F24 | 自评报告任务与协同撰写 | `report_project`、`report_chapter`、`report_task_assignment`、`report_draft` |
| F25 | AI 智能分析与报告辅助 | `ai_prompt_template`、`ai_analysis_request`、`ai_analysis_result` |

## 2. 问卷模块相关表

### 2.1 `survey_questionnaire` 问卷主表

| 项目 | 内容 |
|---|---|
| 表名 | `survey_questionnaire` |
| 作用 | 保存问卷基本信息，是问卷设计、发布、撤回、结束等业务的主表 |
| 关键字段 | `questionnaire_code`、`title`、`questionnaire_type`、`target_object_type`、`anonymous_flag`、`publish_status`、`start_time`、`end_time` |
| 对应功能 | F21、F22 |

### 2.2 `survey_question` 问题表

| 项目 | 内容 |
|---|---|
| 表名 | `survey_question` |
| 作用 | 保存问卷中的题目定义，支持单选、多选、量表、填空、矩阵题 |
| 关键字段 | `questionnaire_id`、`question_code`、`question_text`、`question_type`、`is_required`、`sort_no` |
| 对应功能 | F21、F22 |

### 2.3 `survey_question_option` 选项表

| 项目 | 内容 |
|---|---|
| 表名 | `survey_question_option` |
| 作用 | 保存单选、多选、量表题的选项内容 |
| 关键字段 | `question_id`、`option_code`、`option_text`、`option_value`、`sort_no` |
| 对应功能 | F21、F22 |

### 2.4 `survey_publish_task` 问卷发布任务表

| 项目 | 内容 |
|---|---|
| 表名 | `survey_publish_task` |
| 作用 | 记录问卷发布批次、异步投递状态、失败重试信息，是 MQ 异步发布的重要支撑表 |
| 关键字段 | `questionnaire_id`、`publish_batch_no`、`publish_status`、`mq_status`、`retry_count`、`published_at`、`error_message` |
| 对应功能 | F21 |

### 2.5 `survey_response` 问卷作答表

| 项目 | 内容 |
|---|---|
| 表名 | `survey_response` |
| 作用 | 保存一次完整答卷的头信息，用于提交、回收统计、重复提交校验和答卷导出 |
| 关键字段 | `questionnaire_id`、`respondent_user_id`、`respondent_name`、`respondent_type`、`response_token`、`submit_status`、`submitted_at` |
| 对应功能 | F22 |

### 2.6 `survey_response_answer` 作答明细表

| 项目 | 内容 |
|---|---|
| 表名 | `survey_response_answer` |
| 作用 | 保存每道题的具体作答结果，支持选项、文本、矩阵、分值等多种答案结构 |
| 关键字段 | `response_id`、`question_id`、`option_id`、`row_id`、`column_id`、`answer_text`、`answer_number`、`answer_json` |
| 对应功能 | F22 |

## 3. 持续改进模块相关表

### 3.1 `improve_plan` 改进计划表

| 项目 | 内容 |
|---|---|
| 表名 | `improve_plan` |
| 作用 | 保存改进计划主信息，用于承接达成度问题项或问卷结论，形成闭环改进任务 |
| 关键字段 | `plan_code`、`plan_name`、`source_type`、`source_id`、`target_type`、`target_id`、`owner_user_id`、`due_date`、`status` |
| 对应功能 | F23 |

### 3.2 `improve_plan_action` 改进行动表

| 项目 | 内容 |
|---|---|
| 表名 | `improve_plan_action` |
| 作用 | 保存改进计划下的具体措施与责任分工，是进度跟踪的核心表 |
| 关键字段 | `plan_id`、`action_code`、`action_title`、`responsible_user_id`、`start_date`、`due_date`、`progress_percent`、`status` |
| 对应功能 | F23 |

### 3.3 `improve_plan_record` 改进记录表

| 项目 | 内容 |
|---|---|
| 表名 | `improve_plan_record` |
| 作用 | 记录改进行动执行过程中的阶段性说明、处理结果和附件信息 |
| 关键字段 | `action_id`、`record_type`、`record_content`、`record_time`、`recorder_user_id`、`attachment_file_id` |
| 对应功能 | F23 |

## 4. 自评报告模块相关表

### 4.1 `report_project` 自评报告项目表

| 项目 | 内容 |
|---|---|
| 表名 | `report_project` |
| 作用 | 作为整份自评报告的项目主表，记录项目名称、学年学期、负责人和整体状态 |
| 关键字段 | `report_code`、`project_name`、`academic_year`、`semester_id`、`owner_user_id`、`generation_mode`、`status` |
| 对应功能 | F24 |

### 4.2 `report_chapter` 自评章节表

| 项目 | 内容 |
|---|---|
| 表名 | `report_chapter` |
| 作用 | 保存报告章节树结构及正文内容，可关联培养目标、课程、问卷等来源数据 |
| 关键字段 | `project_id`、`parent_id`、`chapter_code`、`chapter_title`、`source_type`、`source_ref_id`、`content_text`、`chapter_status` |
| 对应功能 | F24、F25 |

### 4.3 `report_task_assignment` 章节任务分配表

| 项目 | 内容 |
|---|---|
| 表名 | `report_task_assignment` |
| 作用 | 记录章节的协同撰写任务分配情况，支持责任人、截止时间和完成状态管理 |
| 关键字段 | `project_id`、`chapter_id`、`assignee_user_id`、`role_type`、`due_date`、`assignment_status`、`completed_at` |
| 对应功能 | F24 |

### 4.4 `report_draft` 报告草稿表

| 项目 | 内容 |
|---|---|
| 表名 | `report_draft` |
| 作用 | 保存章节草稿内容和版本信息，支撑多人协同编辑与版本留痕 |
| 关键字段 | `chapter_id`、`version_no`、`draft_content`、`edited_by`、`edited_at`、`lock_flag` |
| 对应功能 | F24、F25 |

## 5. AI 模块相关表

### 5.1 `ai_prompt_template` AI 提示词模板表

| 项目 | 内容 |
|---|---|
| 表名 | `ai_prompt_template` |
| 作用 | 保存不同 AI 场景下的提示词模板，是报告扩写、润色、改进建议生成的基础配置表 |
| 关键字段 | `template_code`、`template_name`、`scenario_type`、`system_prompt`、`user_prompt`、`enabled` |
| 对应功能 | F25 |

### 5.2 `ai_analysis_request` AI 分析请求表

| 项目 | 内容 |
|---|---|
| 表名 | `ai_analysis_request` |
| 作用 | 记录每一次 AI 调用请求，包括来源对象、请求人、模型、状态和提示词快照 |
| 关键字段 | `request_no`、`scenario_type`、`source_type`、`source_id`、`template_id`、`requester_user_id`、`model_name`、`request_status` |
| 对应功能 | F25 |

### 5.3 `ai_analysis_result` AI 分析结果表

| 项目 | 内容 |
|---|---|
| 表名 | `ai_analysis_result` |
| 作用 | 保存 AI 返回的文本结果或结构化结果，并支持人工确认后回写业务数据 |
| 关键字段 | `request_id`、`result_type`、`result_text`、`result_json`、`confidence_score`、`human_confirmed_flag`、`confirmed_by`、`confirmed_at` |
| 对应功能 | F25 |

## 6. 可在报告中补充说明的支撑表

如果报告需要体现“E 模块并非孤立实现，而是与系统其他能力联动”，可以补一句说明以下支撑表参与了业务闭环，但不必展开写字段：

| 表名 | 用途 |
|---|---|
| `sys_user` | 问卷填写人、改进责任人、报告任务负责人、AI 请求人等都依赖用户表 |
| `sys_file` | 改进记录附件、报告草稿上传文件、导出文件等依赖文件表 |
| `notice_message` / `notice_recipient` / `notice_push_log` | 问卷发布提醒、改进到期提醒、自评任务提醒等通过通知中心完成 |

## 7. 可直接写进报告的简短表述

可直接参考下面这段写法：

> 在数据库设计方面，我负责的成员 E 模块主要涉及问卷、持续改进、自评报告和 AI 辅助四类核心数据表。其中，问卷模块以 `survey_questionnaire`、`survey_question`、`survey_response`、`survey_response_answer` 为主，支撑问卷设计、发布、填报和统计导出；持续改进模块以 `improve_plan`、`improve_plan_action`、`improve_plan_record` 为核心，实现从问题来源到措施执行再到过程记录的闭环管理；自评报告模块以 `report_project`、`report_chapter`、`report_task_assignment`、`report_draft` 为核心，实现章节树管理、任务分配和协同撰写；AI 模块以 `ai_prompt_template`、`ai_analysis_request`、`ai_analysis_result` 为核心，实现提示词配置、请求留痕、结果确认和业务回写。数据库表结构的设计充分支撑了成员 E 模块的完整业务闭环。

## 8. 说明

- 本文档依据 `数据库说明.md` 中的表结构整理。
- 只保留成员 E 报告最常用、最适合写入正文的表。
- 如果你还需要，我可以继续给你补一版“更像学校实习报告最终成稿”的表述版本，直接可粘贴到正文。
