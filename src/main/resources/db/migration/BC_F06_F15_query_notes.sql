-- Reference only for F06-F15 data inspection.
-- This file is intentionally not versioned, so Flyway will ignore it.
-- Use it to review the queries that were executed during local verification.

-- 1) Core row counts for the B/C feature chain
SELECT 'org_college' AS tb, COUNT(*) AS cnt FROM org_college
UNION ALL SELECT 'org_major', COUNT(*) FROM org_major
UNION ALL SELECT 'org_grade', COUNT(*) FROM org_grade
UNION ALL SELECT 'org_class', COUNT(*) FROM org_class
UNION ALL SELECT 'edu_teacher', COUNT(*) FROM edu_teacher
UNION ALL SELECT 'edu_student', COUNT(*) FROM edu_student
UNION ALL SELECT 'edu_semester', COUNT(*) FROM edu_semester
UNION ALL SELECT 'edu_course', COUNT(*) FROM edu_course
UNION ALL SELECT 'sys_user', COUNT(*) FROM sys_user
UNION ALL SELECT 'sys_file', COUNT(*) FROM sys_file
UNION ALL SELECT 'tr_program_version', COUNT(*) FROM tr_program_version
UNION ALL SELECT 'tr_program_apply_grade', COUNT(*) FROM tr_program_apply_grade
UNION ALL SELECT 'tr_program_target', COUNT(*) FROM tr_program_target
UNION ALL SELECT 'tr_graduation_requirement', COUNT(*) FROM tr_graduation_requirement
UNION ALL SELECT 'tr_requirement_indicator_point', COUNT(*) FROM tr_requirement_indicator_point
UNION ALL SELECT 'tr_target_requirement_support', COUNT(*) FROM tr_target_requirement_support
UNION ALL SELECT 'tr_requirement_indicator_support', COUNT(*) FROM tr_requirement_indicator_support
UNION ALL SELECT 'tr_program_course', COUNT(*) FROM tr_program_course
UNION ALL SELECT 'tr_course_requirement_support', COUNT(*) FROM tr_course_requirement_support
UNION ALL SELECT 'edu_course_objective', COUNT(*) FROM edu_course_objective
UNION ALL SELECT 'edu_course_objective_indicator_point', COUNT(*) FROM edu_course_objective_indicator_point
UNION ALL SELECT 'edu_course_content', COUNT(*) FROM edu_course_content
UNION ALL SELECT 'edu_course_content_objective_rel', COUNT(*) FROM edu_course_content_objective_rel
UNION ALL SELECT 'edu_course_assessment_method', COUNT(*) FROM edu_course_assessment_method
UNION ALL SELECT 'edu_course_assessment_standard', COUNT(*) FROM edu_course_assessment_standard
UNION ALL SELECT 'teaching_task', COUNT(*) FROM teaching_task
UNION ALL SELECT 'course_resource', COUNT(*) FROM course_resource
UNION ALL SELECT 'course_evidence_material', COUNT(*) FROM course_evidence_material
UNION ALL SELECT 'course_score_batch', COUNT(*) FROM course_score_batch
UNION ALL SELECT 'course_score_detail', COUNT(*) FROM course_score_detail;

-- 2) Lookup/base data used by the frontend "请选择" dropdowns
SELECT id, college_code, college_name FROM org_college ORDER BY id;
SELECT id, major_code, major_name, college_id FROM org_major ORDER BY id;
SELECT id, grade_year, admission_year, expected_graduation_year FROM org_grade ORDER BY id;
SELECT id, class_code, class_name, major_id, grade_id FROM org_class ORDER BY id;
SELECT t.id, u.real_name, t.teacher_no, t.college_id, t.major_id, t.status
FROM edu_teacher t
LEFT JOIN sys_user u ON u.id = t.user_id
ORDER BY t.id;
SELECT s.id, u.real_name, s.student_no, s.class_id, s.admission_year, s.status
FROM edu_student s
LEFT JOIN sys_user u ON u.id = s.user_id
ORDER BY s.id;
SELECT id, semester_code, semester_name, start_date, end_date, active_flag, is_deleted
FROM edu_semester
ORDER BY id;
SELECT id, course_code, course_name, course_type, credit, total_hours, offering_unit_id, status
FROM edu_course
ORDER BY id;

-- 3) Program design data for F06-F08
SELECT id, version_no, version_name, major_id, status, effective_date, copy_from_version_id
FROM tr_program_version
ORDER BY id;
SELECT id, program_version_id, grade_id, is_deleted
FROM tr_program_apply_grade
ORDER BY id;
SELECT id, program_version_id, target_code, target_name, enabled, is_deleted
FROM tr_program_target
ORDER BY id;
SELECT id, program_version_id, requirement_code, requirement_name, enabled, is_deleted
FROM tr_graduation_requirement
ORDER BY id;
SELECT id, graduation_requirement_id, indicator_code, indicator_name, enabled, is_deleted
FROM tr_requirement_indicator_point
ORDER BY id;
SELECT id, program_target_id, graduation_requirement_id, support_level, support_weight, is_deleted
FROM tr_target_requirement_support
ORDER BY id;
SELECT id, graduation_requirement_id, indicator_point_id, support_level, support_weight, is_deleted
FROM tr_requirement_indicator_support
ORDER BY id;
SELECT id, program_version_id, course_id, semester_recommend, course_category, is_required, is_deleted
FROM tr_program_course
ORDER BY id;
SELECT id, program_version_id, course_id, graduation_requirement_id, support_level, support_weight, is_deleted
FROM tr_course_requirement_support
ORDER BY id;

-- 4) Course setup data for F09-F11
SELECT id, course_id, objective_code, objective_name, enabled, is_deleted
FROM edu_course_objective
ORDER BY id;
SELECT id, course_objective_id, indicator_point_id, support_weight, is_deleted
FROM edu_course_objective_indicator_point
ORDER BY id;
SELECT id, course_id, content_code, content_title, hours, enabled, is_deleted
FROM edu_course_content
ORDER BY id;
SELECT id, content_id, objective_id, support_strength, is_deleted
FROM edu_course_content_objective_rel
ORDER BY id;
SELECT id, course_id, method_code, method_name, ratio_percent, enabled, is_deleted
FROM edu_course_assessment_method
ORDER BY id;
SELECT id, method_id, standard_name, score_min, score_max, sort_no, is_deleted
FROM edu_course_assessment_standard
ORDER BY id;

-- 5) Teaching/resource/evidence/score data for F12-F15
SELECT id, task_code, semester_id, course_id, class_id, teacher_id, program_version_id, task_status, total_hours, is_deleted
FROM teaching_task
ORDER BY id;
SELECT id, course_id, task_id, resource_type, resource_name, file_id, visible_scope_type, publish_status, is_deleted
FROM course_resource
ORDER BY id;
SELECT id, task_id, method_id, material_type, file_id, source_student_id, review_status, review_user_id, is_deleted
FROM course_evidence_material
ORDER BY id;
SELECT id, batch_no, task_id, objective_id, method_id, calc_status, locked_flag, is_deleted
FROM course_score_batch
ORDER BY id;
SELECT id, batch_id, student_id, raw_score, weighted_score, total_score, submit_status, locked_flag, is_deleted
FROM course_score_detail
ORDER BY id;

-- 6) File metadata verification used by F13-F14 preview/download
SELECT id, biz_type, biz_id, original_name, stored_name, storage_path, upload_user_id, visibility_scope, file_status, is_deleted
FROM sys_file
ORDER BY id;
