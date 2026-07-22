-- 成员E相关建表脚本
-- 范围：F21 问卷、F22 回收统计、F23 持续改进、F24 自评报告、F25 AI 辅助
-- 为保证外键可直接执行，已包含必要支撑表：sys_user、sys_file、edu_semester、notice_*
-- 来源：数据库建表.sql

CREATE DATABASE IF NOT EXISTS education_certification_system
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE education_certification_system;

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  real_name VARCHAR(50) NOT NULL,
  phone VARCHAR(20) DEFAULT NULL,
  email VARCHAR(100) DEFAULT NULL,
  user_status TINYINT NOT NULL DEFAULT 1,
  last_login_at DATETIME DEFAULT NULL,
  last_login_ip VARCHAR(45) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_username (username),
  UNIQUE KEY uk_sys_user_phone (phone),
  UNIQUE KEY uk_sys_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS sys_file (
  id BIGINT NOT NULL AUTO_INCREMENT,
  biz_type VARCHAR(50) NOT NULL,
  biz_id BIGINT DEFAULT NULL,
  original_name VARCHAR(255) NOT NULL,
  stored_name VARCHAR(255) NOT NULL,
  file_ext VARCHAR(20) DEFAULT NULL,
  file_size BIGINT NOT NULL,
  mime_type VARCHAR(100) DEFAULT NULL,
  storage_type VARCHAR(20) NOT NULL,
  storage_path VARCHAR(500) NOT NULL,
  md5 VARCHAR(64) DEFAULT NULL,
  upload_user_id BIGINT NOT NULL,
  visibility_scope VARCHAR(20) NOT NULL,
  file_status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_file_stored_name (stored_name),
  KEY idx_sys_file_upload_user_id (upload_user_id),
  KEY idx_sys_file_biz (biz_type, biz_id),
  CONSTRAINT fk_sys_file_user FOREIGN KEY (upload_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件表';

CREATE TABLE IF NOT EXISTS edu_semester (
  id BIGINT NOT NULL AUTO_INCREMENT,
  semester_code VARCHAR(50) NOT NULL,
  semester_name VARCHAR(100) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  active_flag TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_edu_semester_code (semester_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学期表';

CREATE TABLE IF NOT EXISTS notice_message (
  id BIGINT NOT NULL AUTO_INCREMENT,
  notice_type VARCHAR(50) NOT NULL,
  title VARCHAR(200) NOT NULL,
  content LONGTEXT NOT NULL,
  sender_user_id BIGINT DEFAULT NULL,
  biz_type VARCHAR(50) DEFAULT NULL,
  biz_id BIGINT DEFAULT NULL,
  channel_type VARCHAR(20) NOT NULL,
  priority_level INT NOT NULL DEFAULT 0,
  publish_status VARCHAR(20) NOT NULL,
  send_at DATETIME DEFAULT NULL,
  expire_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_notice_message_sender_user_id (sender_user_id),
  KEY idx_notice_message_publish_status (publish_status),
  CONSTRAINT fk_notice_message_sender FOREIGN KEY (sender_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知消息表';

CREATE TABLE IF NOT EXISTS notice_recipient (
  id BIGINT NOT NULL AUTO_INCREMENT,
  notice_id BIGINT NOT NULL,
  recipient_user_id BIGINT NOT NULL,
  read_status TINYINT NOT NULL DEFAULT 0,
  read_at DATETIME DEFAULT NULL,
  deleted_flag TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notice_recipient (notice_id, recipient_user_id),
  KEY idx_notice_recipient_notice_id (notice_id),
  KEY idx_notice_recipient_user_id (recipient_user_id),
  CONSTRAINT fk_notice_recipient_notice FOREIGN KEY (notice_id) REFERENCES notice_message (id),
  CONSTRAINT fk_notice_recipient_user FOREIGN KEY (recipient_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知接收人表';

CREATE TABLE IF NOT EXISTS notice_push_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  notice_id BIGINT NOT NULL,
  mq_topic VARCHAR(100) DEFAULT NULL,
  mq_key VARCHAR(100) DEFAULT NULL,
  retry_count INT NOT NULL DEFAULT 0,
  send_status VARCHAR(20) NOT NULL,
  error_message VARCHAR(1000) DEFAULT NULL,
  sent_at DATETIME DEFAULT NULL,
  acked_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_notice_push_log_notice_id (notice_id),
  CONSTRAINT fk_notice_push_log_notice FOREIGN KEY (notice_id) REFERENCES notice_message (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='通知推送日志表';

CREATE TABLE IF NOT EXISTS survey_questionnaire (
  id BIGINT NOT NULL AUTO_INCREMENT,
  questionnaire_code VARCHAR(50) NOT NULL,
  title VARCHAR(200) NOT NULL,
  subtitle VARCHAR(255) DEFAULT NULL,
  questionnaire_type VARCHAR(50) NOT NULL,
  target_object_type VARCHAR(50) NOT NULL,
  target_object_id BIGINT DEFAULT NULL,
  anonymous_flag TINYINT NOT NULL DEFAULT 0,
  publish_status VARCHAR(20) NOT NULL,
  start_time DATETIME DEFAULT NULL,
  end_time DATETIME DEFAULT NULL,
  mq_status VARCHAR(20) NOT NULL DEFAULT 'NONE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_survey_questionnaire_code (questionnaire_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='问卷主表';

CREATE TABLE IF NOT EXISTS survey_questionnaire_scope (
  id BIGINT NOT NULL AUTO_INCREMENT,
  questionnaire_id BIGINT NOT NULL,
  scope_type VARCHAR(50) NOT NULL,
  scope_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_survey_questionnaire_scope (questionnaire_id, scope_type, scope_id),
  KEY idx_survey_questionnaire_scope_questionnaire_id (questionnaire_id),
  CONSTRAINT fk_survey_questionnaire_scope_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES survey_questionnaire (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='问卷投放范围表';

CREATE TABLE IF NOT EXISTS survey_question (
  id BIGINT NOT NULL AUTO_INCREMENT,
  questionnaire_id BIGINT NOT NULL,
  question_code VARCHAR(50) NOT NULL,
  question_text LONGTEXT NOT NULL,
  question_type VARCHAR(20) NOT NULL,
  is_required TINYINT NOT NULL DEFAULT 1,
  sort_no INT NOT NULL DEFAULT 0,
  min_select INT DEFAULT NULL,
  max_select INT DEFAULT NULL,
  score_weight DECIMAL(5,2) DEFAULT NULL,
  matrix_type VARCHAR(20) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_survey_question (questionnaire_id, question_code),
  KEY idx_survey_question_questionnaire_id (questionnaire_id),
  CONSTRAINT fk_survey_question_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES survey_questionnaire (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='问卷题目表';

CREATE TABLE IF NOT EXISTS survey_question_option (
  id BIGINT NOT NULL AUTO_INCREMENT,
  question_id BIGINT NOT NULL,
  option_code VARCHAR(50) NOT NULL,
  option_text VARCHAR(255) NOT NULL,
  option_value VARCHAR(100) NOT NULL,
  option_score DECIMAL(5,2) DEFAULT NULL,
  is_other TINYINT NOT NULL DEFAULT 0,
  sort_no INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_survey_question_option (question_id, option_code),
  KEY idx_survey_question_option_question_id (question_id),
  CONSTRAINT fk_survey_question_option_question FOREIGN KEY (question_id) REFERENCES survey_question (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='问卷选项表';

CREATE TABLE IF NOT EXISTS survey_question_matrix_row (
  id BIGINT NOT NULL AUTO_INCREMENT,
  question_id BIGINT NOT NULL,
  row_code VARCHAR(50) NOT NULL,
  row_text VARCHAR(255) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_survey_question_matrix_row (question_id, row_code),
  KEY idx_survey_question_matrix_row_question_id (question_id),
  CONSTRAINT fk_survey_question_matrix_row_question FOREIGN KEY (question_id) REFERENCES survey_question (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='矩阵行表';

CREATE TABLE IF NOT EXISTS survey_question_matrix_column (
  id BIGINT NOT NULL AUTO_INCREMENT,
  question_id BIGINT NOT NULL,
  col_code VARCHAR(50) NOT NULL,
  col_text VARCHAR(255) NOT NULL,
  col_value VARCHAR(100) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_survey_question_matrix_column (question_id, col_code),
  KEY idx_survey_question_matrix_column_question_id (question_id),
  CONSTRAINT fk_survey_question_matrix_column_question FOREIGN KEY (question_id) REFERENCES survey_question (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='矩阵列表';

CREATE TABLE IF NOT EXISTS survey_publish_task (
  id BIGINT NOT NULL AUTO_INCREMENT,
  questionnaire_id BIGINT NOT NULL,
  publish_batch_no VARCHAR(50) NOT NULL,
  publish_status VARCHAR(20) NOT NULL,
  mq_status VARCHAR(20) NOT NULL DEFAULT 'NONE',
  retry_count INT NOT NULL DEFAULT 0,
  published_at DATETIME DEFAULT NULL,
  revoked_at DATETIME DEFAULT NULL,
  error_message VARCHAR(1000) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_survey_publish_task_batch_no (publish_batch_no),
  KEY idx_survey_publish_task_questionnaire_id (questionnaire_id),
  CONSTRAINT fk_survey_publish_task_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES survey_questionnaire (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='问卷发布任务表';

CREATE TABLE IF NOT EXISTS survey_response (
  id BIGINT NOT NULL AUTO_INCREMENT,
  questionnaire_id BIGINT NOT NULL,
  respondent_user_id BIGINT DEFAULT NULL,
  respondent_name VARCHAR(100) DEFAULT NULL,
  respondent_type VARCHAR(50) NOT NULL,
  response_token VARCHAR(100) NOT NULL,
  submit_status VARCHAR(20) NOT NULL,
  submitted_at DATETIME DEFAULT NULL,
  ip_address VARCHAR(45) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_survey_response_token (response_token),
  KEY idx_survey_response_questionnaire_id (questionnaire_id),
  KEY idx_survey_response_respondent_user_id (respondent_user_id),
  CONSTRAINT fk_survey_response_questionnaire FOREIGN KEY (questionnaire_id) REFERENCES survey_questionnaire (id),
  CONSTRAINT fk_survey_response_user FOREIGN KEY (respondent_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='问卷作答表';

CREATE TABLE IF NOT EXISTS survey_response_answer (
  id BIGINT NOT NULL AUTO_INCREMENT,
  response_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  option_id BIGINT DEFAULT NULL,
  row_id BIGINT DEFAULT NULL,
  column_id BIGINT DEFAULT NULL,
  answer_text LONGTEXT DEFAULT NULL,
  answer_number DECIMAL(10,2) DEFAULT NULL,
  answer_json JSON DEFAULT NULL,
  score_value DECIMAL(5,2) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_survey_response_answer_response_id (response_id),
  KEY idx_survey_response_answer_question_id (question_id),
  KEY idx_survey_response_answer_option_id (option_id),
  CONSTRAINT fk_survey_response_answer_response FOREIGN KEY (response_id) REFERENCES survey_response (id),
  CONSTRAINT fk_survey_response_answer_question FOREIGN KEY (question_id) REFERENCES survey_question (id),
  CONSTRAINT fk_survey_response_answer_option FOREIGN KEY (option_id) REFERENCES survey_question_option (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='问卷答案表';

CREATE TABLE IF NOT EXISTS improve_plan (
  id BIGINT NOT NULL AUTO_INCREMENT,
  plan_code VARCHAR(50) NOT NULL,
  plan_name VARCHAR(200) NOT NULL,
  source_type VARCHAR(50) NOT NULL,
  source_id BIGINT NOT NULL,
  target_type VARCHAR(50) NOT NULL,
  target_id BIGINT NOT NULL,
  owner_user_id BIGINT NOT NULL,
  start_date DATE NOT NULL,
  due_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL,
  priority INT NOT NULL DEFAULT 0,
  effect_review VARCHAR(1000) DEFAULT NULL,
  closed_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_improve_plan_code (plan_code),
  KEY idx_improve_plan_owner_user_id (owner_user_id),
  CONSTRAINT fk_improve_plan_owner FOREIGN KEY (owner_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='改进计划表';

CREATE TABLE IF NOT EXISTS improve_plan_action (
  id BIGINT NOT NULL AUTO_INCREMENT,
  plan_id BIGINT NOT NULL,
  action_code VARCHAR(50) NOT NULL,
  action_title VARCHAR(200) NOT NULL,
  action_desc LONGTEXT NOT NULL,
  responsible_user_id BIGINT NOT NULL,
  start_date DATE NOT NULL,
  due_date DATE NOT NULL,
  progress_percent DECIMAL(5,2) NOT NULL DEFAULT 0,
  status VARCHAR(20) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_improve_plan_action (plan_id, action_code),
  KEY idx_improve_plan_action_plan_id (plan_id),
  KEY idx_improve_plan_action_responsible_user_id (responsible_user_id),
  CONSTRAINT fk_improve_plan_action_plan FOREIGN KEY (plan_id) REFERENCES improve_plan (id),
  CONSTRAINT fk_improve_plan_action_responsible FOREIGN KEY (responsible_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='改进行动表';

CREATE TABLE IF NOT EXISTS improve_plan_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  action_id BIGINT NOT NULL,
  record_type VARCHAR(50) NOT NULL,
  record_content LONGTEXT NOT NULL,
  record_time DATETIME NOT NULL,
  recorder_user_id BIGINT NOT NULL,
  attachment_file_id BIGINT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_improve_plan_record_action_id (action_id),
  KEY idx_improve_plan_record_recorder_user_id (recorder_user_id),
  KEY idx_improve_plan_record_attachment_file_id (attachment_file_id),
  CONSTRAINT fk_improve_plan_record_action FOREIGN KEY (action_id) REFERENCES improve_plan_action (id),
  CONSTRAINT fk_improve_plan_record_recorder FOREIGN KEY (recorder_user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_improve_plan_record_file FOREIGN KEY (attachment_file_id) REFERENCES sys_file (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='改进记录表';

CREATE TABLE IF NOT EXISTS report_project (
  id BIGINT NOT NULL AUTO_INCREMENT,
  report_code VARCHAR(50) NOT NULL,
  project_name VARCHAR(200) NOT NULL,
  academic_year VARCHAR(20) NOT NULL,
  semester_id BIGINT NOT NULL,
  owner_user_id BIGINT NOT NULL,
  generation_mode VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  total_chapters INT NOT NULL DEFAULT 0,
  locked_flag TINYINT NOT NULL DEFAULT 0,
  exported_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_report_project_code (report_code),
  KEY idx_report_project_semester_id (semester_id),
  KEY idx_report_project_owner_user_id (owner_user_id),
  CONSTRAINT fk_report_project_semester FOREIGN KEY (semester_id) REFERENCES edu_semester (id),
  CONSTRAINT fk_report_project_owner FOREIGN KEY (owner_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='自评报告项目表';

CREATE TABLE IF NOT EXISTS report_chapter (
  id BIGINT NOT NULL AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  parent_id BIGINT DEFAULT NULL,
  chapter_code VARCHAR(50) NOT NULL,
  chapter_title VARCHAR(200) NOT NULL,
  source_type VARCHAR(50) DEFAULT NULL,
  source_ref_id BIGINT DEFAULT NULL,
  content_text LONGTEXT DEFAULT NULL,
  chapter_status VARCHAR(20) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  locked_flag TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_report_chapter (project_id, chapter_code),
  KEY idx_report_chapter_project_id (project_id),
  KEY idx_report_chapter_parent_id (parent_id),
  CONSTRAINT fk_report_chapter_project FOREIGN KEY (project_id) REFERENCES report_project (id),
  CONSTRAINT fk_report_chapter_parent FOREIGN KEY (parent_id) REFERENCES report_chapter (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='自评章节表';

CREATE TABLE IF NOT EXISTS report_task_assignment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  chapter_id BIGINT NOT NULL,
  assignee_user_id BIGINT NOT NULL,
  role_type VARCHAR(50) NOT NULL,
  due_date DATE NOT NULL,
  assignment_status VARCHAR(20) NOT NULL,
  completed_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_report_task_assignment (project_id, chapter_id, assignee_user_id),
  KEY idx_report_task_assignment_project_id (project_id),
  KEY idx_report_task_assignment_chapter_id (chapter_id),
  KEY idx_report_task_assignment_assignee_user_id (assignee_user_id),
  CONSTRAINT fk_report_task_assignment_project FOREIGN KEY (project_id) REFERENCES report_project (id),
  CONSTRAINT fk_report_task_assignment_chapter FOREIGN KEY (chapter_id) REFERENCES report_chapter (id),
  CONSTRAINT fk_report_task_assignment_assignee FOREIGN KEY (assignee_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='章节任务分配表';

CREATE TABLE IF NOT EXISTS report_draft (
  id BIGINT NOT NULL AUTO_INCREMENT,
  chapter_id BIGINT NOT NULL,
  version_no INT NOT NULL,
  draft_content LONGTEXT NOT NULL,
  edited_by BIGINT NOT NULL,
  edited_at DATETIME NOT NULL,
  lock_flag TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_report_draft (chapter_id, version_no),
  KEY idx_report_draft_chapter_id (chapter_id),
  KEY idx_report_draft_edited_by (edited_by),
  CONSTRAINT fk_report_draft_chapter FOREIGN KEY (chapter_id) REFERENCES report_chapter (id),
  CONSTRAINT fk_report_draft_edited_by FOREIGN KEY (edited_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报告草稿表';

CREATE TABLE IF NOT EXISTS report_progress_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  chapter_id BIGINT DEFAULT NULL,
  user_id BIGINT NOT NULL,
  progress_percent DECIMAL(5,2) NOT NULL,
  comment VARCHAR(1000) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_report_progress_log_project_id (project_id),
  KEY idx_report_progress_log_chapter_id (chapter_id),
  KEY idx_report_progress_log_user_id (user_id),
  CONSTRAINT fk_report_progress_log_project FOREIGN KEY (project_id) REFERENCES report_project (id),
  CONSTRAINT fk_report_progress_log_chapter FOREIGN KEY (chapter_id) REFERENCES report_chapter (id),
  CONSTRAINT fk_report_progress_log_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报告进度日志表';

CREATE TABLE IF NOT EXISTS report_export_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  export_type VARCHAR(20) NOT NULL,
  file_id BIGINT NOT NULL,
  exported_by BIGINT NOT NULL,
  exported_at DATETIME NOT NULL,
  export_status VARCHAR(20) NOT NULL,
  error_message VARCHAR(1000) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_report_export_log_project_id (project_id),
  KEY idx_report_export_log_file_id (file_id),
  KEY idx_report_export_log_exported_by (exported_by),
  CONSTRAINT fk_report_export_log_project FOREIGN KEY (project_id) REFERENCES report_project (id),
  CONSTRAINT fk_report_export_log_file FOREIGN KEY (file_id) REFERENCES sys_file (id),
  CONSTRAINT fk_report_export_log_exported_by FOREIGN KEY (exported_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报告导出日志表';

CREATE TABLE IF NOT EXISTS ai_prompt_template (
  id BIGINT NOT NULL AUTO_INCREMENT,
  template_code VARCHAR(50) NOT NULL,
  template_name VARCHAR(200) NOT NULL,
  scenario_type VARCHAR(50) NOT NULL,
  system_prompt LONGTEXT NOT NULL,
  user_prompt LONGTEXT NOT NULL,
  input_schema_json JSON DEFAULT NULL,
  output_schema_json JSON DEFAULT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_prompt_template_code (template_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI提示词模板表';

CREATE TABLE IF NOT EXISTS ai_analysis_request (
  id BIGINT NOT NULL AUTO_INCREMENT,
  request_no VARCHAR(50) NOT NULL,
  scenario_type VARCHAR(50) NOT NULL,
  source_type VARCHAR(50) NOT NULL,
  source_id BIGINT NOT NULL,
  template_id BIGINT NOT NULL,
  requester_user_id BIGINT NOT NULL,
  model_name VARCHAR(100) DEFAULT NULL,
  request_status VARCHAR(20) NOT NULL,
  retry_count INT NOT NULL DEFAULT 0,
  requested_at DATETIME NOT NULL,
  finished_at DATETIME DEFAULT NULL,
  prompt_snapshot LONGTEXT DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_analysis_request_no (request_no),
  KEY idx_ai_analysis_request_template_id (template_id),
  KEY idx_ai_analysis_request_requester_user_id (requester_user_id),
  CONSTRAINT fk_ai_analysis_request_template FOREIGN KEY (template_id) REFERENCES ai_prompt_template (id),
  CONSTRAINT fk_ai_analysis_request_requester FOREIGN KEY (requester_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI分析请求表';

CREATE TABLE IF NOT EXISTS ai_analysis_result (
  id BIGINT NOT NULL AUTO_INCREMENT,
  request_id BIGINT NOT NULL,
  result_type VARCHAR(50) NOT NULL,
  result_text LONGTEXT NOT NULL,
  result_json JSON DEFAULT NULL,
  confidence_score DECIMAL(5,2) DEFAULT NULL,
  human_confirmed_flag TINYINT NOT NULL DEFAULT 0,
  confirmed_by BIGINT DEFAULT NULL,
  confirmed_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_analysis_result_request_id (request_id),
  KEY idx_ai_analysis_result_confirmed_by (confirmed_by),
  CONSTRAINT fk_ai_analysis_result_request FOREIGN KEY (request_id) REFERENCES ai_analysis_request (id),
  CONSTRAINT fk_ai_analysis_result_confirmed_by FOREIGN KEY (confirmed_by) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI分析结果表';

SET FOREIGN_KEY_CHECKS = 1;
