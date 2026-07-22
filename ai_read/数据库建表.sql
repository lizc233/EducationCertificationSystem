-- 工程教育专业认证智能服务系统
-- MySQL 8.0 + utf8mb4

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

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  role_code VARCHAR(50) NOT NULL,
  role_name VARCHAR(50) NOT NULL,
  role_type VARCHAR(20) NOT NULL,
  data_scope VARCHAR(20) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_code (role_code),
  UNIQUE KEY uk_sys_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_user_role (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_user_role (user_id, role_id),
  KEY idx_sys_user_role_user_id (user_id),
  KEY idx_sys_user_role_role_id (role_id),
  CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS sys_menu (
  id BIGINT NOT NULL AUTO_INCREMENT,
  parent_id BIGINT DEFAULT NULL,
  menu_type VARCHAR(20) NOT NULL,
  menu_name VARCHAR(50) NOT NULL,
  route_path VARCHAR(255) DEFAULT NULL,
  component_path VARCHAR(255) DEFAULT NULL,
  permission_code VARCHAR(100) DEFAULT NULL,
  icon VARCHAR(50) DEFAULT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  visible TINYINT NOT NULL DEFAULT 1,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_menu_permission_code (permission_code),
  KEY idx_sys_menu_parent_id (parent_id),
  CONSTRAINT fk_sys_menu_parent FOREIGN KEY (parent_id) REFERENCES sys_menu (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';

CREATE TABLE IF NOT EXISTS sys_role_menu (
  id BIGINT NOT NULL AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_role_menu (role_id, menu_id),
  KEY idx_sys_role_menu_role_id (role_id),
  KEY idx_sys_role_menu_menu_id (menu_id),
  CONSTRAINT fk_sys_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
  CONSTRAINT fk_sys_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关联表';

CREATE TABLE IF NOT EXISTS sys_dict_type (
  id BIGINT NOT NULL AUTO_INCREMENT,
  dict_type VARCHAR(50) NOT NULL,
  dict_name VARCHAR(100) NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_dict_type_code (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

CREATE TABLE IF NOT EXISTS sys_dict_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  dict_type_id BIGINT NOT NULL,
  item_label VARCHAR(100) NOT NULL,
  item_value VARCHAR(100) NOT NULL,
  item_sort INT NOT NULL DEFAULT 0,
  is_default TINYINT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_dict_item (dict_type_id, item_value),
  KEY idx_sys_dict_item_type_id (dict_type_id),
  CONSTRAINT fk_sys_dict_item_type FOREIGN KEY (dict_type_id) REFERENCES sys_dict_type (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典项表';

CREATE TABLE IF NOT EXISTS sys_param (
  id BIGINT NOT NULL AUTO_INCREMENT,
  param_key VARCHAR(100) NOT NULL,
  param_value VARCHAR(1000) NOT NULL,
  param_type VARCHAR(50) NOT NULL,
  is_system TINYINT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_param_key (param_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统参数表';

CREATE TABLE IF NOT EXISTS sys_operation_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  operator_user_id BIGINT DEFAULT NULL,
  operator_name VARCHAR(50) DEFAULT NULL,
  log_type VARCHAR(20) NOT NULL,
  module_name VARCHAR(100) NOT NULL,
  biz_type VARCHAR(50) DEFAULT NULL,
  biz_id BIGINT DEFAULT NULL,
  request_uri VARCHAR(255) DEFAULT NULL,
  request_method VARCHAR(20) DEFAULT NULL,
  request_params TEXT,
  response_result LONGTEXT,
  success_flag TINYINT NOT NULL DEFAULT 1,
  error_message VARCHAR(1000) DEFAULT NULL,
  duration_ms INT DEFAULT NULL,
  ip_address VARCHAR(45) DEFAULT NULL,
  user_agent VARCHAR(500) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_sys_operation_log_user_id (operator_user_id),
  KEY idx_sys_operation_log_module_name (module_name),
  KEY idx_sys_operation_log_created_at (created_at),
  CONSTRAINT fk_sys_operation_log_user FOREIGN KEY (operator_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS sys_login_session (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  access_token_hash VARCHAR(255) NOT NULL,
  refresh_token_hash VARCHAR(255) DEFAULT NULL,
  login_time DATETIME NOT NULL,
  expire_time DATETIME NOT NULL,
  last_active_time DATETIME DEFAULT NULL,
  logout_time DATETIME DEFAULT NULL,
  revoked_flag TINYINT NOT NULL DEFAULT 0,
  login_ip VARCHAR(45) DEFAULT NULL,
  client_type VARCHAR(20) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_sys_login_session_access_token (access_token_hash),
  UNIQUE KEY uk_sys_login_session_refresh_token (refresh_token_hash),
  KEY idx_sys_login_session_user_id (user_id),
  CONSTRAINT fk_sys_login_session_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录会话表';

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

CREATE TABLE IF NOT EXISTS org_college (
  id BIGINT NOT NULL AUTO_INCREMENT,
  college_code VARCHAR(50) NOT NULL,
  college_name VARCHAR(100) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_org_college_code (college_code),
  UNIQUE KEY uk_org_college_name (college_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学院表';

CREATE TABLE IF NOT EXISTS org_major (
  id BIGINT NOT NULL AUTO_INCREMENT,
  college_id BIGINT NOT NULL,
  major_code VARCHAR(50) NOT NULL,
  major_name VARCHAR(100) NOT NULL,
  degree_type VARCHAR(20) NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_org_major_code (major_code),
  KEY idx_org_major_college_id (college_id),
  CONSTRAINT fk_org_major_college FOREIGN KEY (college_id) REFERENCES org_college (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专业表';

CREATE TABLE IF NOT EXISTS org_grade (
  id BIGINT NOT NULL AUTO_INCREMENT,
  grade_year INT NOT NULL,
  admission_year INT NOT NULL,
  expected_graduation_year INT NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_org_grade_year (grade_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='年级表';

CREATE TABLE IF NOT EXISTS org_class (
  id BIGINT NOT NULL AUTO_INCREMENT,
  major_id BIGINT NOT NULL,
  grade_id BIGINT NOT NULL,
  class_code VARCHAR(50) NOT NULL,
  class_name VARCHAR(100) NOT NULL,
  head_teacher_id BIGINT DEFAULT NULL,
  student_count INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_org_class_code (class_code),
  KEY idx_org_class_major_id (major_id),
  KEY idx_org_class_grade_id (grade_id),
  KEY idx_org_class_head_teacher_id (head_teacher_id),
  CONSTRAINT fk_org_class_major FOREIGN KEY (major_id) REFERENCES org_major (id),
  CONSTRAINT fk_org_class_grade FOREIGN KEY (grade_id) REFERENCES org_grade (id),
  CONSTRAINT fk_org_class_head_teacher FOREIGN KEY (head_teacher_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='班级表';

CREATE TABLE IF NOT EXISTS edu_teacher (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  teacher_no VARCHAR(50) NOT NULL,
  college_id BIGINT NOT NULL,
  major_id BIGINT DEFAULT NULL,
  title VARCHAR(50) DEFAULT NULL,
  job_title VARCHAR(50) DEFAULT NULL,
  phone VARCHAR(20) DEFAULT NULL,
  email VARCHAR(100) DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_edu_teacher_user_id (user_id),
  UNIQUE KEY uk_edu_teacher_no (teacher_no),
  KEY idx_edu_teacher_college_id (college_id),
  KEY idx_edu_teacher_major_id (major_id),
  CONSTRAINT fk_edu_teacher_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_edu_teacher_college FOREIGN KEY (college_id) REFERENCES org_college (id),
  CONSTRAINT fk_edu_teacher_major FOREIGN KEY (major_id) REFERENCES org_major (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教师表';

CREATE TABLE IF NOT EXISTS edu_student (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  student_no VARCHAR(50) NOT NULL,
  class_id BIGINT NOT NULL,
  admission_year INT NOT NULL,
  gender VARCHAR(10) DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  graduation_status TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_edu_student_user_id (user_id),
  UNIQUE KEY uk_edu_student_no (student_no),
  KEY idx_edu_student_class_id (class_id),
  CONSTRAINT fk_edu_student_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
  CONSTRAINT fk_edu_student_class FOREIGN KEY (class_id) REFERENCES org_class (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生表';

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

CREATE TABLE IF NOT EXISTS tr_program_version (
  id BIGINT NOT NULL AUTO_INCREMENT,
  major_id BIGINT NOT NULL,
  version_no VARCHAR(50) NOT NULL,
  version_name VARCHAR(100) NOT NULL,
  effective_date DATE NOT NULL,
  status VARCHAR(20) NOT NULL,
  copy_from_version_id BIGINT DEFAULT NULL,
  released_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tr_program_version (major_id, version_no),
  KEY idx_tr_program_version_major_id (major_id),
  KEY idx_tr_program_version_copy_from (copy_from_version_id),
  CONSTRAINT fk_tr_program_version_major FOREIGN KEY (major_id) REFERENCES org_major (id),
  CONSTRAINT fk_tr_program_version_copy_from FOREIGN KEY (copy_from_version_id) REFERENCES tr_program_version (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='培养方案版本表';

CREATE TABLE IF NOT EXISTS tr_program_apply_grade (
  id BIGINT NOT NULL AUTO_INCREMENT,
  program_version_id BIGINT NOT NULL,
  grade_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tr_program_apply_grade (program_version_id, grade_id),
  KEY idx_tr_program_apply_grade_version_id (program_version_id),
  KEY idx_tr_program_apply_grade_grade_id (grade_id),
  CONSTRAINT fk_tr_program_apply_grade_version FOREIGN KEY (program_version_id) REFERENCES tr_program_version (id),
  CONSTRAINT fk_tr_program_apply_grade_grade FOREIGN KEY (grade_id) REFERENCES org_grade (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='方案应用年级表';

CREATE TABLE IF NOT EXISTS tr_program_target (
  id BIGINT NOT NULL AUTO_INCREMENT,
  program_version_id BIGINT NOT NULL,
  target_code VARCHAR(50) NOT NULL,
  target_name VARCHAR(100) NOT NULL,
  target_desc TEXT NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tr_program_target (program_version_id, target_code),
  KEY idx_tr_program_target_version_id (program_version_id),
  CONSTRAINT fk_tr_program_target_version FOREIGN KEY (program_version_id) REFERENCES tr_program_version (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='培养目标表';

CREATE TABLE IF NOT EXISTS tr_graduation_requirement (
  id BIGINT NOT NULL AUTO_INCREMENT,
  program_version_id BIGINT NOT NULL,
  requirement_code VARCHAR(50) NOT NULL,
  requirement_name VARCHAR(100) NOT NULL,
  requirement_desc TEXT NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tr_graduation_requirement (program_version_id, requirement_code),
  KEY idx_tr_graduation_requirement_version_id (program_version_id),
  CONSTRAINT fk_tr_graduation_requirement_version FOREIGN KEY (program_version_id) REFERENCES tr_program_version (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='毕业要求表';

CREATE TABLE IF NOT EXISTS tr_requirement_indicator_point (
  id BIGINT NOT NULL AUTO_INCREMENT,
  graduation_requirement_id BIGINT NOT NULL,
  indicator_code VARCHAR(50) NOT NULL,
  indicator_name VARCHAR(100) NOT NULL,
  indicator_desc TEXT NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tr_requirement_indicator_point (graduation_requirement_id, indicator_code),
  KEY idx_tr_requirement_indicator_point_req_id (graduation_requirement_id),
  CONSTRAINT fk_tr_requirement_indicator_point_req FOREIGN KEY (graduation_requirement_id) REFERENCES tr_graduation_requirement (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='指标点表';

CREATE TABLE IF NOT EXISTS tr_target_requirement_support (
  id BIGINT NOT NULL AUTO_INCREMENT,
  program_target_id BIGINT NOT NULL,
  graduation_requirement_id BIGINT NOT NULL,
  support_level VARCHAR(10) NOT NULL,
  support_weight DECIMAL(5,2) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tr_target_requirement_support (program_target_id, graduation_requirement_id),
  KEY idx_tr_target_requirement_support_target_id (program_target_id),
  KEY idx_tr_target_requirement_support_req_id (graduation_requirement_id),
  CONSTRAINT fk_tr_target_requirement_support_target FOREIGN KEY (program_target_id) REFERENCES tr_program_target (id),
  CONSTRAINT fk_tr_target_requirement_support_req FOREIGN KEY (graduation_requirement_id) REFERENCES tr_graduation_requirement (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='培养目标支撑毕业要求表';

CREATE TABLE IF NOT EXISTS tr_requirement_indicator_support (
  id BIGINT NOT NULL AUTO_INCREMENT,
  graduation_requirement_id BIGINT NOT NULL,
  indicator_point_id BIGINT NOT NULL,
  support_level VARCHAR(10) NOT NULL,
  support_weight DECIMAL(5,2) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tr_requirement_indicator_support (graduation_requirement_id, indicator_point_id),
  KEY idx_tr_requirement_indicator_support_req_id (graduation_requirement_id),
  KEY idx_tr_requirement_indicator_support_indicator_id (indicator_point_id),
  CONSTRAINT fk_tr_requirement_indicator_support_req FOREIGN KEY (graduation_requirement_id) REFERENCES tr_graduation_requirement (id),
  CONSTRAINT fk_tr_requirement_indicator_support_indicator FOREIGN KEY (indicator_point_id) REFERENCES tr_requirement_indicator_point (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='毕业要求支撑指标点表';

CREATE TABLE IF NOT EXISTS tr_program_course (
  id BIGINT NOT NULL AUTO_INCREMENT,
  program_version_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  semester_recommend VARCHAR(50) DEFAULT NULL,
  course_category VARCHAR(50) DEFAULT NULL,
  is_required TINYINT NOT NULL DEFAULT 1,
  sort_no INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tr_program_course (program_version_id, course_id),
  KEY idx_tr_program_course_version_id (program_version_id),
  KEY idx_tr_program_course_course_id (course_id),
  CONSTRAINT fk_tr_program_course_version FOREIGN KEY (program_version_id) REFERENCES tr_program_version (id),
  CONSTRAINT fk_tr_program_course_course FOREIGN KEY (course_id) REFERENCES edu_course (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='方案课程表';

CREATE TABLE IF NOT EXISTS tr_course_requirement_support (
  id BIGINT NOT NULL AUTO_INCREMENT,
  program_version_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  graduation_requirement_id BIGINT NOT NULL,
  support_level VARCHAR(10) NOT NULL,
  support_weight DECIMAL(5,2) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tr_course_requirement_support (program_version_id, course_id, graduation_requirement_id),
  KEY idx_tr_course_requirement_support_version_id (program_version_id),
  KEY idx_tr_course_requirement_support_course_id (course_id),
  KEY idx_tr_course_requirement_support_req_id (graduation_requirement_id),
  CONSTRAINT fk_tr_course_requirement_support_version FOREIGN KEY (program_version_id) REFERENCES tr_program_version (id),
  CONSTRAINT fk_tr_course_requirement_support_course FOREIGN KEY (course_id) REFERENCES edu_course (id),
  CONSTRAINT fk_tr_course_requirement_support_req FOREIGN KEY (graduation_requirement_id) REFERENCES tr_graduation_requirement (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程支撑毕业要求表';

CREATE TABLE IF NOT EXISTS edu_course (
  id BIGINT NOT NULL AUTO_INCREMENT,
  course_code VARCHAR(50) NOT NULL,
  course_name VARCHAR(100) NOT NULL,
  course_type VARCHAR(50) NOT NULL,
  credit DECIMAL(4,1) NOT NULL,
  total_hours INT NOT NULL,
  theory_hours INT NOT NULL DEFAULT 0,
  practice_hours INT NOT NULL DEFAULT 0,
  offering_unit_id BIGINT DEFAULT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_edu_course_code (course_code),
  KEY idx_edu_course_offering_unit_id (offering_unit_id),
  CONSTRAINT fk_edu_course_offering_unit FOREIGN KEY (offering_unit_id) REFERENCES org_college (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程表';

CREATE TABLE IF NOT EXISTS edu_course_objective (
  id BIGINT NOT NULL AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  objective_code VARCHAR(50) NOT NULL,
  objective_name VARCHAR(100) NOT NULL,
  objective_desc TEXT NOT NULL,
  achievement_standard TEXT NOT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_edu_course_objective (course_id, objective_code),
  KEY idx_edu_course_objective_course_id (course_id),
  CONSTRAINT fk_edu_course_objective_course FOREIGN KEY (course_id) REFERENCES edu_course (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程目标表';

CREATE TABLE IF NOT EXISTS edu_course_objective_indicator_point (
  id BIGINT NOT NULL AUTO_INCREMENT,
  course_objective_id BIGINT NOT NULL,
  indicator_point_id BIGINT NOT NULL,
  support_weight DECIMAL(5,2) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_edu_course_objective_indicator_point (course_objective_id, indicator_point_id),
  KEY idx_edu_course_objective_indicator_point_obj_id (course_objective_id),
  KEY idx_edu_course_objective_indicator_point_indicator_id (indicator_point_id),
  CONSTRAINT fk_edu_course_objective_indicator_point_obj FOREIGN KEY (course_objective_id) REFERENCES edu_course_objective (id),
  CONSTRAINT fk_edu_course_objective_indicator_point_indicator FOREIGN KEY (indicator_point_id) REFERENCES tr_requirement_indicator_point (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程目标映射指标点表';

CREATE TABLE IF NOT EXISTS edu_course_content (
  id BIGINT NOT NULL AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  content_code VARCHAR(50) NOT NULL,
  content_title VARCHAR(100) NOT NULL,
  content_desc TEXT NOT NULL,
  hours INT NOT NULL DEFAULT 0,
  sort_no INT NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_edu_course_content (course_id, content_code),
  KEY idx_edu_course_content_course_id (course_id),
  CONSTRAINT fk_edu_course_content_course FOREIGN KEY (course_id) REFERENCES edu_course (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程教学内容表';

CREATE TABLE IF NOT EXISTS edu_course_content_objective_rel (
  id BIGINT NOT NULL AUTO_INCREMENT,
  content_id BIGINT NOT NULL,
  objective_id BIGINT NOT NULL,
  support_strength VARCHAR(10) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_edu_course_content_objective_rel (content_id, objective_id),
  KEY idx_edu_course_content_objective_rel_content_id (content_id),
  KEY idx_edu_course_content_objective_rel_objective_id (objective_id),
  CONSTRAINT fk_edu_course_content_objective_rel_content FOREIGN KEY (content_id) REFERENCES edu_course_content (id),
  CONSTRAINT fk_edu_course_content_objective_rel_objective FOREIGN KEY (objective_id) REFERENCES edu_course_objective (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='教学内容与课程目标关系表';

CREATE TABLE IF NOT EXISTS edu_course_assessment_method (
  id BIGINT NOT NULL AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  method_code VARCHAR(50) NOT NULL,
  method_name VARCHAR(100) NOT NULL,
  ratio_percent DECIMAL(5,2) NOT NULL,
  due_rule VARCHAR(255) DEFAULT NULL,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_edu_course_assessment_method (course_id, method_code),
  KEY idx_edu_course_assessment_method_course_id (course_id),
  CONSTRAINT fk_edu_course_assessment_method_course FOREIGN KEY (course_id) REFERENCES edu_course (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程考核方式表';

CREATE TABLE IF NOT EXISTS edu_course_assessment_standard (
  id BIGINT NOT NULL AUTO_INCREMENT,
  method_id BIGINT NOT NULL,
  standard_name VARCHAR(100) NOT NULL,
  standard_desc TEXT NOT NULL,
  score_min DECIMAL(5,2) DEFAULT NULL,
  score_max DECIMAL(5,2) DEFAULT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_edu_course_assessment_standard_method_id (method_id),
  CONSTRAINT fk_edu_course_assessment_standard_method FOREIGN KEY (method_id) REFERENCES edu_course_assessment_method (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程考核标准表';

CREATE TABLE IF NOT EXISTS teaching_task (
  id BIGINT NOT NULL AUTO_INCREMENT,
  task_code VARCHAR(50) NOT NULL,
  semester_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  class_id BIGINT NOT NULL,
  teacher_id BIGINT NOT NULL,
  program_version_id BIGINT NOT NULL,
  task_status VARCHAR(20) NOT NULL,
  total_hours INT NOT NULL,
  schedule_desc VARCHAR(500) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_teaching_task_code (task_code),
  KEY idx_teaching_task_semester_id (semester_id),
  KEY idx_teaching_task_course_id (course_id),
  KEY idx_teaching_task_class_id (class_id),
  KEY idx_teaching_task_teacher_id (teacher_id),
  KEY idx_teaching_task_program_version_id (program_version_id),
  CONSTRAINT fk_teaching_task_semester FOREIGN KEY (semester_id) REFERENCES edu_semester (id),
  CONSTRAINT fk_teaching_task_course FOREIGN KEY (course_id) REFERENCES edu_course (id),
  CONSTRAINT fk_teaching_task_class FOREIGN KEY (class_id) REFERENCES org_class (id),
  CONSTRAINT fk_teaching_task_teacher FOREIGN KEY (teacher_id) REFERENCES edu_teacher (id),
  CONSTRAINT fk_teaching_task_program_version FOREIGN KEY (program_version_id) REFERENCES tr_program_version (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='授课任务表';

CREATE TABLE IF NOT EXISTS course_resource (
  id BIGINT NOT NULL AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  task_id BIGINT DEFAULT NULL,
  resource_type VARCHAR(50) NOT NULL,
  resource_name VARCHAR(255) NOT NULL,
  file_id BIGINT NOT NULL,
  resource_desc VARCHAR(500) DEFAULT NULL,
  visible_scope_type VARCHAR(20) NOT NULL,
  visible_scope_id BIGINT DEFAULT NULL,
  publish_status TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_course_resource_course_id (course_id),
  KEY idx_course_resource_task_id (task_id),
  KEY idx_course_resource_file_id (file_id),
  CONSTRAINT fk_course_resource_course FOREIGN KEY (course_id) REFERENCES edu_course (id),
  CONSTRAINT fk_course_resource_task FOREIGN KEY (task_id) REFERENCES teaching_task (id),
  CONSTRAINT fk_course_resource_file FOREIGN KEY (file_id) REFERENCES sys_file (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程资源表';

CREATE TABLE IF NOT EXISTS course_evidence_material (
  id BIGINT NOT NULL AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  method_id BIGINT NOT NULL,
  material_type VARCHAR(50) NOT NULL,
  file_id BIGINT NOT NULL,
  source_student_id BIGINT DEFAULT NULL,
  review_status VARCHAR(20) NOT NULL,
  review_user_id BIGINT DEFAULT NULL,
  review_comment VARCHAR(1000) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_course_evidence_material_task_id (task_id),
  KEY idx_course_evidence_material_method_id (method_id),
  KEY idx_course_evidence_material_file_id (file_id),
  KEY idx_course_evidence_material_source_student_id (source_student_id),
  KEY idx_course_evidence_material_review_user_id (review_user_id),
  CONSTRAINT fk_course_evidence_material_task FOREIGN KEY (task_id) REFERENCES teaching_task (id),
  CONSTRAINT fk_course_evidence_material_method FOREIGN KEY (method_id) REFERENCES edu_course_assessment_method (id),
  CONSTRAINT fk_course_evidence_material_file FOREIGN KEY (file_id) REFERENCES sys_file (id),
  CONSTRAINT fk_course_evidence_material_student FOREIGN KEY (source_student_id) REFERENCES edu_student (id),
  CONSTRAINT fk_course_evidence_material_review_user FOREIGN KEY (review_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考核证据材料表';

CREATE TABLE IF NOT EXISTS course_score_batch (
  id BIGINT NOT NULL AUTO_INCREMENT,
  batch_no VARCHAR(50) NOT NULL,
  task_id BIGINT NOT NULL,
  objective_id BIGINT NOT NULL,
  method_id BIGINT DEFAULT NULL,
  calc_status VARCHAR(20) NOT NULL,
  locked_flag TINYINT NOT NULL DEFAULT 0,
  imported_at DATETIME DEFAULT NULL,
  calculated_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_course_score_batch_no (batch_no),
  KEY idx_course_score_batch_task_id (task_id),
  KEY idx_course_score_batch_objective_id (objective_id),
  KEY idx_course_score_batch_method_id (method_id),
  CONSTRAINT fk_course_score_batch_task FOREIGN KEY (task_id) REFERENCES teaching_task (id),
  CONSTRAINT fk_course_score_batch_objective FOREIGN KEY (objective_id) REFERENCES edu_course_objective (id),
  CONSTRAINT fk_course_score_batch_method FOREIGN KEY (method_id) REFERENCES edu_course_assessment_method (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成绩批次表';

CREATE TABLE IF NOT EXISTS course_score_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  batch_id BIGINT NOT NULL,
  student_id BIGINT NOT NULL,
  raw_score DECIMAL(6,2) NOT NULL,
  weighted_score DECIMAL(6,2) NOT NULL,
  total_score DECIMAL(6,2) NOT NULL,
  source_type VARCHAR(50) DEFAULT NULL,
  source_ref_id BIGINT DEFAULT NULL,
  submit_status VARCHAR(20) NOT NULL,
  locked_flag TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_course_score_detail (batch_id, student_id),
  KEY idx_course_score_detail_batch_id (batch_id),
  KEY idx_course_score_detail_student_id (student_id),
  CONSTRAINT fk_course_score_detail_batch FOREIGN KEY (batch_id) REFERENCES course_score_batch (id),
  CONSTRAINT fk_course_score_detail_student FOREIGN KEY (student_id) REFERENCES edu_student (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成绩明细表';

CREATE TABLE IF NOT EXISTS eval_model (
  id BIGINT NOT NULL AUTO_INCREMENT,
  model_code VARCHAR(50) NOT NULL,
  model_name VARCHAR(100) NOT NULL,
  model_type VARCHAR(50) NOT NULL,
  scope_type VARCHAR(50) NOT NULL,
  formula_expression VARCHAR(1000) DEFAULT NULL,
  threshold_value DECIMAL(5,2) DEFAULT NULL,
  include_questionnaire_flag TINYINT NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_eval_model_code (model_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='达成度评价模型表';

CREATE TABLE IF NOT EXISTS eval_model_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  model_id BIGINT NOT NULL,
  item_code VARCHAR(50) NOT NULL,
  item_name VARCHAR(100) NOT NULL,
  item_type VARCHAR(50) NOT NULL,
  weight_percent DECIMAL(5,2) NOT NULL,
  threshold_value DECIMAL(5,2) DEFAULT NULL,
  calc_rule VARCHAR(500) DEFAULT NULL,
  sort_no INT NOT NULL DEFAULT 0,
  enabled TINYINT NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_eval_model_item (model_id, item_code),
  KEY idx_eval_model_item_model_id (model_id),
  CONSTRAINT fk_eval_model_item_model FOREIGN KEY (model_id) REFERENCES eval_model (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模型明细项表';

CREATE TABLE IF NOT EXISTS eval_model_scope (
  id BIGINT NOT NULL AUTO_INCREMENT,
  model_id BIGINT NOT NULL,
  scope_type VARCHAR(50) NOT NULL,
  scope_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_eval_model_scope (model_id, scope_type, scope_id),
  KEY idx_eval_model_scope_model_id (model_id),
  CONSTRAINT fk_eval_model_scope_model FOREIGN KEY (model_id) REFERENCES eval_model (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模型适用范围表';

CREATE TABLE IF NOT EXISTS eval_course_target_result (
  id BIGINT NOT NULL AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  objective_id BIGINT NOT NULL,
  model_id BIGINT NOT NULL,
  attainment_rate DECIMAL(5,2) NOT NULL,
  attainment_value DECIMAL(6,2) NOT NULL,
  target_value DECIMAL(6,2) NOT NULL,
  result_level VARCHAR(20) DEFAULT NULL,
  calc_time DATETIME NOT NULL,
  recalculation_count INT NOT NULL DEFAULT 0,
  locked_flag TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_eval_course_target_result (task_id, objective_id, model_id),
  KEY idx_eval_course_target_result_task_id (task_id),
  KEY idx_eval_course_target_result_objective_id (objective_id),
  KEY idx_eval_course_target_result_model_id (model_id),
  CONSTRAINT fk_eval_course_target_result_task FOREIGN KEY (task_id) REFERENCES teaching_task (id),
  CONSTRAINT fk_eval_course_target_result_objective FOREIGN KEY (objective_id) REFERENCES edu_course_objective (id),
  CONSTRAINT fk_eval_course_target_result_model FOREIGN KEY (model_id) REFERENCES eval_model (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程目标达成结果表';

CREATE TABLE IF NOT EXISTS eval_graduation_requirement_result (
  id BIGINT NOT NULL AUTO_INCREMENT,
  program_version_id BIGINT NOT NULL,
  requirement_id BIGINT NOT NULL,
  model_id BIGINT NOT NULL,
  attainment_rate DECIMAL(5,2) NOT NULL,
  attainment_value DECIMAL(6,2) NOT NULL,
  threshold_value DECIMAL(6,2) NOT NULL,
  warning_flag TINYINT NOT NULL DEFAULT 0,
  calc_time DATETIME NOT NULL,
  lock_flag TINYINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_eval_graduation_requirement_result (program_version_id, requirement_id, model_id),
  KEY idx_eval_graduation_requirement_result_version_id (program_version_id),
  KEY idx_eval_graduation_requirement_result_requirement_id (requirement_id),
  KEY idx_eval_graduation_requirement_result_model_id (model_id),
  CONSTRAINT fk_eval_graduation_requirement_result_version FOREIGN KEY (program_version_id) REFERENCES tr_program_version (id),
  CONSTRAINT fk_eval_graduation_requirement_result_requirement FOREIGN KEY (requirement_id) REFERENCES tr_graduation_requirement (id),
  CONSTRAINT fk_eval_graduation_requirement_result_model FOREIGN KEY (model_id) REFERENCES eval_model (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='毕业要求达成结果表';

CREATE TABLE IF NOT EXISTS eval_result_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  result_type VARCHAR(20) NOT NULL,
  result_id BIGINT NOT NULL,
  source_type VARCHAR(50) NOT NULL,
  source_id BIGINT NOT NULL,
  weight_percent DECIMAL(5,2) NOT NULL,
  source_value DECIMAL(6,2) NOT NULL,
  contribution_value DECIMAL(6,2) NOT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_eval_result_detail_result (result_type, result_id),
  KEY idx_eval_result_detail_source (source_type, source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='达成度明细表';

CREATE TABLE IF NOT EXISTS eval_recalc_job (
  id BIGINT NOT NULL AUTO_INCREMENT,
  job_no VARCHAR(50) NOT NULL,
  job_type VARCHAR(50) NOT NULL,
  relation_type VARCHAR(50) NOT NULL,
  relation_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  retry_count INT NOT NULL DEFAULT 0,
  error_message VARCHAR(1000) DEFAULT NULL,
  queued_at DATETIME NOT NULL,
  started_at DATETIME DEFAULT NULL,
  finished_at DATETIME DEFAULT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted TINYINT NOT NULL DEFAULT 0,
  remark VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_eval_recalc_job_no (job_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='重算任务表';

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
