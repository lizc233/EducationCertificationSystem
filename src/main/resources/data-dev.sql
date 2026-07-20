INSERT INTO sys_role (id, role_code, role_name, role_type, data_scope, sort_no, status, is_deleted, remark)
VALUES
    (1, 'ROLE_SUPER_ADMIN', 'Administrator', 'SYSTEM', 'ALL', 1, 1, 0, 'System administrator'),
    (2, 'ROLE_TEACHER', 'Teacher', 'SYSTEM', 'COLLEGE', 2, 1, 0, 'Academic staff'),
    (3, 'ROLE_STUDENT', 'Student', 'SYSTEM', 'SELF', 3, 1, 0, 'Student account');

INSERT INTO sys_user (id, username, password_hash, real_name, phone, email, user_status, is_deleted, remark)
VALUES
    (1, 'A2026001', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Admin Zhang', '13800000001', 'admin@school.edu', 1, 0, 'Academic Affairs Office'),
    (2, 'T2026001', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Teacher Li', '13800000002', 'teacher@school.edu', 1, 0, 'School of Computer Science'),
    (3, 'S2026001', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Student Wang', '13800000003', 'student@school.edu', 1, 0, 'CS 2501'),
    (4, 'S2026002', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Student Zhao', '13800000004', 'zhao@school.edu', 1, 0, 'CS 2502'),
    (5, 'T2026002', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Teacher Chen', '13800000005', 'chen@school.edu', 0, 0, 'School of Automation'),
    (6, 'S2026003', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Student Zhou', '13800000006', 'zhou@school.edu', 1, 0, 'CS 2501'),
    (7, 'T2026003', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Teacher Huang', '13800000008', 'huang@school.edu', 1, 0, 'Software Engineering'),
    (8, 'A2026002', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Director Sun', '13800000009', 'sun@school.edu', 1, 0, 'Academic Affairs Office'),
    (9, 'T2026004', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 'Teacher He', '13800000011', 'he@school.edu', 1, 0, 'School of Electronic Engineering');

INSERT INTO sys_user_role (id, user_id, role_id, is_deleted)
VALUES
    (1, 1, 1, 0),
    (2, 2, 2, 0),
    (3, 3, 3, 0),
    (4, 4, 3, 0),
    (5, 5, 2, 0),
    (6, 6, 3, 0),
    (7, 7, 2, 0),
    (8, 8, 1, 0),
    (9, 9, 2, 0);

INSERT INTO org_college (id, college_code, college_name, sort_no, status, is_deleted, remark)
VALUES
    (1, 'AAO', 'Academic Affairs Office', 1, 1, 0, 'Management department'),
    (2, 'CS', 'School of Computer Science', 2, 1, 0, 'College sample'),
    (3, 'EE', 'School of Electronic Engineering', 3, 1, 0, 'College sample'),
    (4, 'AE', 'School of Automation', 4, 1, 0, 'College sample');

INSERT INTO org_major (id, college_id, major_code, major_name, degree_type, sort_no, status, is_deleted, remark)
VALUES
    (1, 2, 'CS01', 'Computer Science and Technology', 'Bachelor', 1, 1, 0, 'Major sample'),
    (2, 2, 'SE01', 'Software Engineering', 'Bachelor', 2, 1, 0, 'Major sample'),
    (3, 3, 'EE01', 'Electronic Information Engineering', 'Bachelor', 3, 1, 0, 'Major sample'),
    (4, 4, 'AE01', 'Automation', 'Bachelor', 4, 1, 0, 'Major sample');

INSERT INTO org_grade (id, grade_year, admission_year, expected_graduation_year, status, is_deleted, remark)
VALUES
    (1, 2023, 2023, 2027, 1, 0, 'Grade 2023'),
    (2, 2024, 2024, 2028, 1, 0, 'Grade 2024'),
    (3, 2025, 2025, 2029, 1, 0, 'Grade 2025');

INSERT INTO org_class (id, major_id, grade_id, class_code, class_name, head_teacher_id, student_count, status, is_deleted, remark)
VALUES
    (1, 1, 3, 'CS2501', 'CS 2501', 2, 42, 1, 0, 'Class sample'),
    (2, 1, 3, 'CS2502', 'CS 2502', 9, 41, 1, 0, 'Class sample'),
    (3, 2, 2, 'SE2401', 'SE 2401', 7, 39, 1, 0, 'Class sample'),
    (4, 4, 2, 'AE2401', 'AE 2401', 5, 38, 1, 0, 'Class sample');

INSERT INTO edu_teacher (id, user_id, teacher_no, college_id, major_id, title, job_title, phone, email, status, is_deleted, remark)
VALUES
    (1, 2, 'T2026001', 2, 1, 'Lecturer', 'Course Leader', '13800000002', 'teacher@school.edu', 1, 0, 'Teacher profile'),
    (2, 5, 'T2026002', 4, 4, 'Associate Professor', 'Teacher', '13800000005', 'chen@school.edu', 0, 0, 'Teacher profile'),
    (3, 7, 'T2026003', 2, 2, 'Lecturer', 'Teacher', '13800000008', 'huang@school.edu', 1, 0, 'Teacher profile'),
    (4, 9, 'T2026004', 3, 3, 'Professor', 'Program Leader', '13800000011', 'he@school.edu', 1, 0, 'Teacher profile');

INSERT INTO edu_student (id, user_id, student_no, class_id, admission_year, gender, status, graduation_status, is_deleted, remark)
VALUES
    (1, 3, 'S2026001', 1, 2025, 'Male', 1, 0, 0, 'Student profile'),
    (2, 4, 'S2026002', 2, 2025, 'Female', 1, 0, 0, 'Student profile'),
    (3, 6, 'S2026003', 1, 2025, 'Male', 1, 0, 0, 'Student profile');

INSERT INTO edu_manager (id, user_id, admin_no, department_name, position_name, status, is_deleted, remark)
VALUES
    (1, 1, 'A2026001', 'Academic Affairs Office', 'System Administrator', 1, 0, 'Administrator profile'),
    (2, 8, 'A2026002', 'Academic Affairs Office', 'Director', 1, 0, 'Administrator profile');

INSERT INTO sys_param (id, param_key, param_value, param_type, is_system, status, is_deleted, remark)
VALUES
    (1, 'system.name', 'Engineering Education Certification System', 'STRING', 1, 1, 0, 'System name'),
    (2, 'achievement.threshold', '0.75', 'NUMBER', 1, 1, 0, 'Achievement threshold'),
    (3, 'mail.notice.sender', 'noreply@school.edu', 'STRING', 0, 1, 0, 'Mail sender'),
    (4, 'questionnaire.remind.days', '3', 'NUMBER', 0, 1, 0, 'Reminder interval');

INSERT INTO sys_dict_type (id, dict_type, dict_name, status, is_deleted, remark)
VALUES
    (1, 'user_status', 'User Status', 1, 0, 'System dict'),
    (2, 'degree_type', 'Degree Type', 1, 0, 'System dict'),
    (3, 'log_type', 'Log Type', 1, 0, 'System dict');

INSERT INTO sys_dict_item (id, dict_type_id, item_label, item_value, item_sort, is_default, status, is_deleted, remark)
VALUES
    (1, 1, 'Enabled', '1', 1, 1, 1, 0, 'Enabled'),
    (2, 1, 'Disabled', '0', 2, 0, 1, 0, 'Disabled'),
    (3, 2, 'Bachelor', 'Bachelor', 1, 1, 1, 0, 'Bachelor'),
    (4, 2, 'Associate', 'Associate', 2, 0, 1, 0, 'Associate'),
    (5, 3, 'Login', 'LOGIN', 1, 0, 1, 0, 'Login log'),
    (6, 3, 'Query', 'QUERY', 2, 0, 1, 0, 'Query log'),
    (7, 3, 'Create', 'CREATE', 3, 0, 1, 0, 'Create log'),
    (8, 3, 'Update', 'UPDATE', 4, 0, 1, 0, 'Update log');

INSERT INTO sys_operation_log (id, operator_user_id, operator_name, log_type, module_name, biz_type, request_uri, request_method, request_params, success_flag, duration_ms, ip_address, user_agent, is_deleted, remark)
VALUES
    (1, 1, 'Admin Zhang', 'LOGIN', 'Authentication', 'POST', '/api/auth/login', 'POST', 'account=A2026001', 1, 36, '127.0.0.1', 'seed', 0, 'Initial log'),
    (2, 1, 'Admin Zhang', 'CREATE', 'User Management', 'POST', '/api/user', 'POST', 'create teacher', 1, 58, '127.0.0.1', 'seed', 0, 'Initial log'),
    (3, 2, 'Teacher Li', 'QUERY', 'Organization', 'GET', '/api/org/tree', 'GET', 'tree', 1, 21, '127.0.0.1', 'seed', 0, 'Initial log'),
    (4, 1, 'Admin Zhang', 'UPDATE', 'System Params', 'PUT', '/api/system/params/2', 'PUT', 'achievement.threshold', 1, 43, '127.0.0.1', 'seed', 0, 'Initial log');
