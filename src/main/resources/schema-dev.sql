DROP TABLE IF EXISTS edu_student;
DROP TABLE IF EXISTS edu_teacher;
DROP TABLE IF EXISTS edu_manager;
DROP TABLE IF EXISTS org_class;
DROP TABLE IF EXISTS org_grade;
DROP TABLE IF EXISTS org_major;
DROP TABLE IF EXISTS org_college;
DROP TABLE IF EXISTS sys_file;
DROP TABLE IF EXISTS sys_login_session;
DROP TABLE IF EXISTS sys_operation_log;
DROP TABLE IF EXISTS notice_push_log;
DROP TABLE IF EXISTS notice_recipient;
DROP TABLE IF EXISTS notice_message;
DROP TABLE IF EXISTS sys_dict_item;
DROP TABLE IF EXISTS sys_dict_type;
DROP TABLE IF EXISTS sys_param;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    user_status TINYINT NOT NULL DEFAULT 1,
    last_login_at TIMESTAMP NULL,
    last_login_ip VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    CONSTRAINT uk_sys_user_phone UNIQUE (phone),
    CONSTRAINT uk_sys_user_email UNIQUE (email)
);

CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    role_type VARCHAR(20) NOT NULL,
    data_scope VARCHAR(20) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500)
);

CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    CONSTRAINT uk_sys_user_role UNIQUE (user_id, role_id),
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role(id)
);

CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT,
    menu_type VARCHAR(20) NOT NULL,
    menu_name VARCHAR(50) NOT NULL,
    route_path VARCHAR(255),
    component_path VARCHAR(255),
    permission_code VARCHAR(100),
    icon VARCHAR(50),
    sort_no INT NOT NULL DEFAULT 0,
    visible TINYINT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    CONSTRAINT uk_sys_menu_permission_code UNIQUE (permission_code),
    CONSTRAINT fk_sys_menu_parent FOREIGN KEY (parent_id) REFERENCES sys_menu(id)
);

CREATE TABLE sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    CONSTRAINT uk_sys_role_menu UNIQUE (role_id, menu_id),
    CONSTRAINT fk_sys_role_menu_role FOREIGN KEY (role_id) REFERENCES sys_role(id),
    CONSTRAINT fk_sys_role_menu_menu FOREIGN KEY (menu_id) REFERENCES sys_menu(id)
);

CREATE TABLE sys_param (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    param_key VARCHAR(100) NOT NULL UNIQUE,
    param_value VARCHAR(1000) NOT NULL,
    param_type VARCHAR(50) NOT NULL,
    is_system TINYINT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500)
);

CREATE TABLE sys_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type VARCHAR(50) NOT NULL UNIQUE,
    dict_name VARCHAR(100) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500)
);

CREATE TABLE sys_dict_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type_id BIGINT NOT NULL,
    item_label VARCHAR(100) NOT NULL,
    item_value VARCHAR(100) NOT NULL,
    item_sort INT NOT NULL DEFAULT 0,
    is_default TINYINT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    CONSTRAINT uk_sys_dict_item UNIQUE (dict_type_id, item_value),
    CONSTRAINT fk_sys_dict_item_type FOREIGN KEY (dict_type_id) REFERENCES sys_dict_type(id)
);

CREATE TABLE sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator_user_id BIGINT,
    operator_name VARCHAR(50),
    log_type VARCHAR(20) NOT NULL,
    module_name VARCHAR(100) NOT NULL,
    biz_type VARCHAR(50),
    biz_id BIGINT,
    request_uri VARCHAR(255),
    request_method VARCHAR(20),
    request_params CLOB,
    response_result CLOB,
    success_flag TINYINT NOT NULL DEFAULT 1,
    error_message VARCHAR(1000),
    duration_ms INT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500)
);

CREATE TABLE sys_login_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    access_token_hash VARCHAR(255) NOT NULL UNIQUE,
    refresh_token_hash VARCHAR(255),
    login_time TIMESTAMP NOT NULL,
    expire_time TIMESTAMP NOT NULL,
    last_active_time TIMESTAMP,
    logout_time TIMESTAMP,
    revoked_flag TINYINT NOT NULL DEFAULT 0,
    login_ip VARCHAR(45),
    client_type VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    CONSTRAINT fk_sys_login_session_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

CREATE TABLE sys_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    storage_key VARCHAR(255) NOT NULL UNIQUE,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT NOT NULL DEFAULT 0,
    uploader_user_id BIGINT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    CONSTRAINT fk_sys_file_uploader FOREIGN KEY (uploader_user_id) REFERENCES sys_user(id)
);

CREATE TABLE org_college (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    college_code VARCHAR(50) NOT NULL UNIQUE,
    college_name VARCHAR(100) NOT NULL UNIQUE,
    sort_no INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500)
);

CREATE TABLE org_major (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    college_id BIGINT NOT NULL,
    major_code VARCHAR(50) NOT NULL UNIQUE,
    major_name VARCHAR(100) NOT NULL,
    degree_type VARCHAR(20) NOT NULL,
    sort_no INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    CONSTRAINT fk_org_major_college FOREIGN KEY (college_id) REFERENCES org_college(id)
);

CREATE TABLE org_grade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    grade_year INT NOT NULL UNIQUE,
    admission_year INT NOT NULL,
    expected_graduation_year INT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500)
);

CREATE TABLE org_class (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    major_id BIGINT NOT NULL,
    grade_id BIGINT NOT NULL,
    class_code VARCHAR(50) NOT NULL UNIQUE,
    class_name VARCHAR(100) NOT NULL,
    head_teacher_id BIGINT,
    student_count INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    CONSTRAINT fk_org_class_major FOREIGN KEY (major_id) REFERENCES org_major(id),
    CONSTRAINT fk_org_class_grade FOREIGN KEY (grade_id) REFERENCES org_grade(id),
    CONSTRAINT fk_org_class_head_teacher FOREIGN KEY (head_teacher_id) REFERENCES sys_user(id)
);

CREATE TABLE edu_teacher (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    teacher_no VARCHAR(50) NOT NULL UNIQUE,
    college_id BIGINT NOT NULL,
    major_id BIGINT,
    title VARCHAR(50),
    job_title VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    CONSTRAINT fk_edu_teacher_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_edu_teacher_college FOREIGN KEY (college_id) REFERENCES org_college(id),
    CONSTRAINT fk_edu_teacher_major FOREIGN KEY (major_id) REFERENCES org_major(id)
);

CREATE TABLE edu_student (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    student_no VARCHAR(50) NOT NULL UNIQUE,
    class_id BIGINT NOT NULL,
    admission_year INT NOT NULL,
    gender VARCHAR(10),
    status TINYINT NOT NULL DEFAULT 1,
    graduation_status TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    CONSTRAINT fk_edu_student_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_edu_student_class FOREIGN KEY (class_id) REFERENCES org_class(id)
);

CREATE TABLE edu_manager (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    admin_no VARCHAR(50) NOT NULL UNIQUE,
    department_name VARCHAR(100),
    position_name VARCHAR(50),
    status TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    CONSTRAINT fk_edu_manager_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
);
