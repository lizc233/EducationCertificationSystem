-- attainment_demo_seed.sql
-- Purpose:
-- 1. Fill the missing prerequisite data for course-target and graduation-requirement attainment.
-- 2. Expand sample data so /achievement/dashboard is no longer sparse.
-- 3. Keep existing data intact as much as possible and only insert/update the minimum required demo rows.
--
-- Expected usage after import:
-- 1. On /achievement/course, use model code CT_DEMO_PV_20260722 and calculate each seeded task.
-- 2. On /achievement/graduate, use model code GR_DEMO_PV_20260722 and calculate program version 1.
-- 3. On /achievement/dashboard, you should then see multiple semesters, multiple courses, and non-empty graduation sections.

SET NAMES utf8mb4;

START TRANSACTION;

SET @program_version_id := (
    SELECT id
    FROM tr_program_version
    WHERE is_deleted = 0
    ORDER BY id
    LIMIT 1
);

SET @major_id := (
    SELECT major_id
    FROM tr_program_version
    WHERE id = @program_version_id
    LIMIT 1
);

SET @offering_unit_id := COALESCE((
    SELECT offering_unit_id
    FROM edu_course
    WHERE is_deleted = 0
    ORDER BY id
    LIMIT 1
), 1);

SET @teacher_id := COALESCE((
    SELECT id
    FROM edu_teacher
    WHERE is_deleted = 0
      AND (major_id = @major_id OR major_id IS NULL)
    ORDER BY id
    LIMIT 1
), (
    SELECT id
    FROM edu_teacher
    WHERE is_deleted = 0
    ORDER BY id
    LIMIT 1
));

SET @class_id_1 := COALESCE((
    SELECT id
    FROM org_class
    WHERE is_deleted = 0
      AND major_id = @major_id
    ORDER BY id
    LIMIT 1
), (
    SELECT id
    FROM org_class
    WHERE is_deleted = 0
    ORDER BY id
    LIMIT 1
));

SET @class_id_2 := COALESCE((
    SELECT id
    FROM org_class
    WHERE is_deleted = 0
      AND major_id = @major_id
      AND id <> @class_id_1
    ORDER BY id
    LIMIT 1
), @class_id_1);

SET @student_a := (
    SELECT id
    FROM edu_student
    WHERE is_deleted = 0
      AND class_id = @class_id_1
    ORDER BY id
    LIMIT 1
);

SET @student_b := COALESCE((
    SELECT id
    FROM edu_student
    WHERE is_deleted = 0
      AND class_id = @class_id_1
      AND id <> @student_a
    ORDER BY id
    LIMIT 1
), @student_a);

SET @student_c := COALESCE((
    SELECT id
    FROM edu_student
    WHERE is_deleted = 0
      AND class_id = @class_id_2
    ORDER BY id
    LIMIT 1
), @student_a);

-- ---------------------------------------------------------------------------
-- Seed extra semesters for dashboard trend
-- ---------------------------------------------------------------------------
INSERT INTO edu_semester (
    semester_code, semester_name, start_date, end_date, active_flag,
    created_at, updated_at, is_deleted, remark
)
SELECT '2026-FALL-DEMO', '2026 Fall Demo', '2026-09-01', '2027-01-15', 0,
       NOW(), NOW(), 0, 'Demo semester for attainment trend'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_semester
    WHERE semester_code = '2026-FALL-DEMO'
);

INSERT INTO edu_semester (
    semester_code, semester_name, start_date, end_date, active_flag,
    created_at, updated_at, is_deleted, remark
)
SELECT '2027-SPRING-DEMO', '2027 Spring Demo', '2027-02-20', '2027-07-10', 0,
       NOW(), NOW(), 0, 'Demo semester for attainment trend'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_semester
    WHERE semester_code = '2027-SPRING-DEMO'
);

SET @semester_id_1 := (
    SELECT id
    FROM edu_semester
    WHERE is_deleted = 0
    ORDER BY id
    LIMIT 1
);

SET @semester_id_2 := (
    SELECT id
    FROM edu_semester
    WHERE semester_code = '2026-FALL-DEMO'
      AND is_deleted = 0
    LIMIT 1
);

SET @semester_id_3 := (
    SELECT id
    FROM edu_semester
    WHERE semester_code = '2027-SPRING-DEMO'
      AND is_deleted = 0
    LIMIT 1
);

-- ---------------------------------------------------------------------------
-- Seed two more courses for dashboard course distribution
-- ---------------------------------------------------------------------------
INSERT INTO edu_course (
    course_code, course_name, course_type, credit, total_hours, theory_hours,
    practice_hours, offering_unit_id, status, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-CS-102', 'Software Design Practice', 'Core Course', 3.0, 48, 32,
       16, @offering_unit_id, 1, NOW(), NOW(), 0, 'Demo course for attainment calculation'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course
    WHERE course_code = 'DEMO-CS-102'
);

INSERT INTO edu_course (
    course_code, course_name, course_type, credit, total_hours, theory_hours,
    practice_hours, offering_unit_id, status, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-CS-103', 'Engineering Project Workshop', 'Core Course', 2.5, 40, 20,
       20, @offering_unit_id, 1, NOW(), NOW(), 0, 'Demo course for attainment calculation'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course
    WHERE course_code = 'DEMO-CS-103'
);

SET @course_id_1 := (
    SELECT id
    FROM edu_course
    WHERE is_deleted = 0
    ORDER BY id
    LIMIT 1
);

SET @course_id_2 := (
    SELECT id
    FROM edu_course
    WHERE course_code = 'DEMO-CS-102'
      AND is_deleted = 0
    LIMIT 1
);

SET @course_id_3 := (
    SELECT id
    FROM edu_course
    WHERE course_code = 'DEMO-CS-103'
      AND is_deleted = 0
    LIMIT 1
);

-- ---------------------------------------------------------------------------
-- Seed objectives for the new courses
-- ---------------------------------------------------------------------------
INSERT INTO edu_course_objective (
    course_id, objective_code, objective_name, objective_desc, achievement_standard,
    sort_no, enabled, created_at, updated_at, is_deleted, remark
)
SELECT @course_id_2, 'OBJ1', 'System Analysis', 'Students can analyze software requirements.',
       'Attainment target 80', 1, 1, NOW(), NOW(), 0, 'Demo objective'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective
    WHERE course_id = @course_id_2
      AND objective_code = 'OBJ1'
);

INSERT INTO edu_course_objective (
    course_id, objective_code, objective_name, objective_desc, achievement_standard,
    sort_no, enabled, created_at, updated_at, is_deleted, remark
)
SELECT @course_id_2, 'OBJ2', 'Design Delivery', 'Students can complete design and implementation work.',
       'Attainment target 75', 2, 1, NOW(), NOW(), 0, 'Demo objective'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective
    WHERE course_id = @course_id_2
      AND objective_code = 'OBJ2'
);

INSERT INTO edu_course_objective (
    course_id, objective_code, objective_name, objective_desc, achievement_standard,
    sort_no, enabled, created_at, updated_at, is_deleted, remark
)
SELECT @course_id_3, 'OBJ1', 'Project Planning', 'Students can plan engineering project milestones.',
       'Attainment target 78', 1, 1, NOW(), NOW(), 0, 'Demo objective'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective
    WHERE course_id = @course_id_3
      AND objective_code = 'OBJ1'
);

INSERT INTO edu_course_objective (
    course_id, objective_code, objective_name, objective_desc, achievement_standard,
    sort_no, enabled, created_at, updated_at, is_deleted, remark
)
SELECT @course_id_3, 'OBJ2', 'Project Execution', 'Students can execute and deliver engineering artifacts.',
       'Attainment target 82', 2, 1, NOW(), NOW(), 0, 'Demo objective'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective
    WHERE course_id = @course_id_3
      AND objective_code = 'OBJ2'
);

SET @c1_obj1 := (
    SELECT id
    FROM edu_course_objective
    WHERE course_id = @course_id_1
      AND is_deleted = 0
    ORDER BY sort_no, id
    LIMIT 1
);

SET @c1_obj2 := (
    SELECT id
    FROM edu_course_objective
    WHERE course_id = @course_id_1
      AND is_deleted = 0
    ORDER BY sort_no, id
    LIMIT 1 OFFSET 1
);

SET @c2_obj1 := (
    SELECT id
    FROM edu_course_objective
    WHERE course_id = @course_id_2
      AND objective_code = 'OBJ1'
      AND is_deleted = 0
    LIMIT 1
);

SET @c2_obj2 := (
    SELECT id
    FROM edu_course_objective
    WHERE course_id = @course_id_2
      AND objective_code = 'OBJ2'
      AND is_deleted = 0
    LIMIT 1
);

SET @c3_obj1 := (
    SELECT id
    FROM edu_course_objective
    WHERE course_id = @course_id_3
      AND objective_code = 'OBJ1'
      AND is_deleted = 0
    LIMIT 1
);

SET @c3_obj2 := (
    SELECT id
    FROM edu_course_objective
    WHERE course_id = @course_id_3
      AND objective_code = 'OBJ2'
      AND is_deleted = 0
    LIMIT 1
);

-- ---------------------------------------------------------------------------
-- Seed standardized assessment methods that can be reused by one shared model
-- ---------------------------------------------------------------------------
INSERT INTO edu_course_assessment_method (
    course_id, method_code, method_name, ratio_percent, due_rule, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT @course_id_1, 'EXAM', 'Final Exam', 60.00, 'Week 16', 1,
       NOW(), NOW(), 0, 'Demo method for shared course-target model'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_assessment_method
    WHERE course_id = @course_id_1
      AND method_code = 'EXAM'
);

INSERT INTO edu_course_assessment_method (
    course_id, method_code, method_name, ratio_percent, due_rule, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT @course_id_1, 'PROJECT', 'Course Project', 40.00, 'Week 14', 1,
       NOW(), NOW(), 0, 'Demo method for shared course-target model'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_assessment_method
    WHERE course_id = @course_id_1
      AND method_code = 'PROJECT'
);

INSERT INTO edu_course_assessment_method (
    course_id, method_code, method_name, ratio_percent, due_rule, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT @course_id_2, 'EXAM', 'Final Exam', 60.00, 'Week 16', 1,
       NOW(), NOW(), 0, 'Demo method for shared course-target model'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_assessment_method
    WHERE course_id = @course_id_2
      AND method_code = 'EXAM'
);

INSERT INTO edu_course_assessment_method (
    course_id, method_code, method_name, ratio_percent, due_rule, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT @course_id_2, 'PROJECT', 'Course Project', 40.00, 'Week 14', 1,
       NOW(), NOW(), 0, 'Demo method for shared course-target model'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_assessment_method
    WHERE course_id = @course_id_2
      AND method_code = 'PROJECT'
);

INSERT INTO edu_course_assessment_method (
    course_id, method_code, method_name, ratio_percent, due_rule, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT @course_id_3, 'EXAM', 'Final Exam', 60.00, 'Week 16', 1,
       NOW(), NOW(), 0, 'Demo method for shared course-target model'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_assessment_method
    WHERE course_id = @course_id_3
      AND method_code = 'EXAM'
);

INSERT INTO edu_course_assessment_method (
    course_id, method_code, method_name, ratio_percent, due_rule, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT @course_id_3, 'PROJECT', 'Course Project', 40.00, 'Week 14', 1,
       NOW(), NOW(), 0, 'Demo method for shared course-target model'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_assessment_method
    WHERE course_id = @course_id_3
      AND method_code = 'PROJECT'
);

SET @c1_exam_method := (
    SELECT id
    FROM edu_course_assessment_method
    WHERE course_id = @course_id_1
      AND method_code = 'EXAM'
      AND is_deleted = 0
    LIMIT 1
);

SET @c1_project_method := (
    SELECT id
    FROM edu_course_assessment_method
    WHERE course_id = @course_id_1
      AND method_code = 'PROJECT'
      AND is_deleted = 0
    LIMIT 1
);

SET @c2_exam_method := (
    SELECT id
    FROM edu_course_assessment_method
    WHERE course_id = @course_id_2
      AND method_code = 'EXAM'
      AND is_deleted = 0
    LIMIT 1
);

SET @c2_project_method := (
    SELECT id
    FROM edu_course_assessment_method
    WHERE course_id = @course_id_2
      AND method_code = 'PROJECT'
      AND is_deleted = 0
    LIMIT 1
);

SET @c3_exam_method := (
    SELECT id
    FROM edu_course_assessment_method
    WHERE course_id = @course_id_3
      AND method_code = 'EXAM'
      AND is_deleted = 0
    LIMIT 1
);

SET @c3_project_method := (
    SELECT id
    FROM edu_course_assessment_method
    WHERE course_id = @course_id_3
      AND method_code = 'PROJECT'
      AND is_deleted = 0
    LIMIT 1
);

-- ---------------------------------------------------------------------------
-- Seed a shared program-version-scoped course target model
-- ---------------------------------------------------------------------------
INSERT INTO eval_model (
    model_code, model_name, model_type, scope_type, formula_expression,
    threshold_value, include_questionnaire_flag, enabled, status,
    created_at, updated_at, is_deleted, remark
)
SELECT 'CT_DEMO_PV_20260722', 'Course Target Demo Model', 'COURSE_TARGET', 'PROGRAM_VERSION',
       'weighted_average(EXAM,PROJECT)', 75.00, 0, 1, 'DRAFT',
       NOW(), NOW(), 0, 'Shared model for seeded attainment demo data'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM eval_model
    WHERE model_code = 'CT_DEMO_PV_20260722'
);

SET @course_target_model_id := (
    SELECT id
    FROM eval_model
    WHERE model_code = 'CT_DEMO_PV_20260722'
      AND is_deleted = 0
    LIMIT 1
);

INSERT INTO eval_model_item (
    model_id, item_code, item_name, item_type, weight_percent, threshold_value,
    calc_rule, sort_no, enabled, created_at, updated_at, is_deleted, remark
)
SELECT @course_target_model_id, 'EXAM', 'Final Exam', 'ASSESSMENT_METHOD', 60.00, NULL,
       'average(total_score)', 1, 1, NOW(), NOW(), 0, 'Shared demo weight'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM eval_model_item
    WHERE model_id = @course_target_model_id
      AND item_code = 'EXAM'
);

INSERT INTO eval_model_item (
    model_id, item_code, item_name, item_type, weight_percent, threshold_value,
    calc_rule, sort_no, enabled, created_at, updated_at, is_deleted, remark
)
SELECT @course_target_model_id, 'PROJECT', 'Course Project', 'ASSESSMENT_METHOD', 40.00, NULL,
       'average(total_score)', 2, 1, NOW(), NOW(), 0, 'Shared demo weight'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM eval_model_item
    WHERE model_id = @course_target_model_id
      AND item_code = 'PROJECT'
);

INSERT INTO eval_model_scope (
    model_id, scope_type, scope_id, created_at, updated_at, is_deleted, remark
)
SELECT @course_target_model_id, 'PROGRAM_VERSION', @program_version_id, NOW(), NOW(), 0,
       'Program-version scope for shared course target demo model'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM eval_model_scope
    WHERE model_id = @course_target_model_id
      AND scope_type = 'PROGRAM_VERSION'
      AND scope_id = @program_version_id
);

-- ---------------------------------------------------------------------------
-- Seed a graduation requirement model that matches the program version flow
-- ---------------------------------------------------------------------------
INSERT INTO eval_model (
    model_code, model_name, model_type, scope_type, formula_expression,
    threshold_value, include_questionnaire_flag, enabled, status,
    created_at, updated_at, is_deleted, remark
)
SELECT 'GR_DEMO_PV_20260722', 'Graduation Requirement Demo Model', 'GRADUATION_REQUIREMENT', 'PROGRAM_VERSION',
       'weighted_average(course_target_result)', 70.00, 0, 1, 'DRAFT',
       NOW(), NOW(), 0, 'Graduation model for seeded attainment demo data'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM eval_model
    WHERE model_code = 'GR_DEMO_PV_20260722'
);

SET @graduation_model_id := (
    SELECT id
    FROM eval_model
    WHERE model_code = 'GR_DEMO_PV_20260722'
      AND is_deleted = 0
    LIMIT 1
);

INSERT INTO eval_model_scope (
    model_id, scope_type, scope_id, created_at, updated_at, is_deleted, remark
)
SELECT @graduation_model_id, 'PROGRAM_VERSION', @program_version_id, NOW(), NOW(), 0,
       'Program-version scope for graduation demo model'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM eval_model_scope
    WHERE model_id = @graduation_model_id
      AND scope_type = 'PROGRAM_VERSION'
      AND scope_id = @program_version_id
);

-- ---------------------------------------------------------------------------
-- Seed more teaching tasks so dashboard has more course and semester coverage
-- ---------------------------------------------------------------------------
SET @task_id_1 := (
    SELECT id
    FROM teaching_task
    WHERE is_deleted = 0
      AND program_version_id = @program_version_id
      AND course_id = @course_id_1
    ORDER BY id
    LIMIT 1
);

INSERT INTO teaching_task (
    task_code, semester_id, course_id, class_id, teacher_id, program_version_id,
    task_status, total_hours, schedule_desc, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-TASK-2026-FALL', @semester_id_2, @course_id_2, @class_id_1, @teacher_id, @program_version_id,
       'DRAFT', 48, 'Demo task for attainment course calculation', NOW(), NOW(), 0, 'Demo teaching task'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM teaching_task
    WHERE task_code = 'DEMO-TASK-2026-FALL'
);

INSERT INTO teaching_task (
    task_code, semester_id, course_id, class_id, teacher_id, program_version_id,
    task_status, total_hours, schedule_desc, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-TASK-2027-SPRING', @semester_id_3, @course_id_3, @class_id_2, @teacher_id, @program_version_id,
       'DRAFT', 40, 'Demo task for attainment course calculation', NOW(), NOW(), 0, 'Demo teaching task'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM teaching_task
    WHERE task_code = 'DEMO-TASK-2027-SPRING'
);

SET @task_id_2 := (
    SELECT id
    FROM teaching_task
    WHERE task_code = 'DEMO-TASK-2026-FALL'
      AND is_deleted = 0
    LIMIT 1
);

SET @task_id_3 := (
    SELECT id
    FROM teaching_task
    WHERE task_code = 'DEMO-TASK-2027-SPRING'
      AND is_deleted = 0
    LIMIT 1
);

-- ---------------------------------------------------------------------------
-- Seed course score batches for each objective and each assessment method
-- ---------------------------------------------------------------------------
INSERT INTO course_score_batch (
    batch_no, task_id, objective_id, method_id, calc_status, locked_flag,
    imported_at, calculated_at, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-T1-OBJ1-EXAM', @task_id_1, @c1_obj1, @c1_exam_method, 'LOCKED', 1,
       NOW(), NOW(), NOW(), NOW(), 0, 'Demo score batch'
FROM dual
WHERE @task_id_1 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM course_score_batch WHERE batch_no = 'DEMO-T1-OBJ1-EXAM');

INSERT INTO course_score_batch (
    batch_no, task_id, objective_id, method_id, calc_status, locked_flag,
    imported_at, calculated_at, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-T1-OBJ1-PROJECT', @task_id_1, @c1_obj1, @c1_project_method, 'LOCKED', 1,
       NOW(), NOW(), NOW(), NOW(), 0, 'Demo score batch'
FROM dual
WHERE @task_id_1 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM course_score_batch WHERE batch_no = 'DEMO-T1-OBJ1-PROJECT');

INSERT INTO course_score_batch (
    batch_no, task_id, objective_id, method_id, calc_status, locked_flag,
    imported_at, calculated_at, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-T1-OBJ2-EXAM', @task_id_1, @c1_obj2, @c1_exam_method, 'LOCKED', 1,
       NOW(), NOW(), NOW(), NOW(), 0, 'Demo score batch'
FROM dual
WHERE @task_id_1 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM course_score_batch WHERE batch_no = 'DEMO-T1-OBJ2-EXAM');

INSERT INTO course_score_batch (
    batch_no, task_id, objective_id, method_id, calc_status, locked_flag,
    imported_at, calculated_at, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-T1-OBJ2-PROJECT', @task_id_1, @c1_obj2, @c1_project_method, 'LOCKED', 1,
       NOW(), NOW(), NOW(), NOW(), 0, 'Demo score batch'
FROM dual
WHERE @task_id_1 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM course_score_batch WHERE batch_no = 'DEMO-T1-OBJ2-PROJECT');

INSERT INTO course_score_batch (
    batch_no, task_id, objective_id, method_id, calc_status, locked_flag,
    imported_at, calculated_at, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-T2-OBJ1-EXAM', @task_id_2, @c2_obj1, @c2_exam_method, 'LOCKED', 1,
       NOW(), NOW(), NOW(), NOW(), 0, 'Demo score batch'
FROM dual
WHERE @task_id_2 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM course_score_batch WHERE batch_no = 'DEMO-T2-OBJ1-EXAM');

INSERT INTO course_score_batch (
    batch_no, task_id, objective_id, method_id, calc_status, locked_flag,
    imported_at, calculated_at, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-T2-OBJ1-PROJECT', @task_id_2, @c2_obj1, @c2_project_method, 'LOCKED', 1,
       NOW(), NOW(), NOW(), NOW(), 0, 'Demo score batch'
FROM dual
WHERE @task_id_2 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM course_score_batch WHERE batch_no = 'DEMO-T2-OBJ1-PROJECT');

INSERT INTO course_score_batch (
    batch_no, task_id, objective_id, method_id, calc_status, locked_flag,
    imported_at, calculated_at, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-T2-OBJ2-EXAM', @task_id_2, @c2_obj2, @c2_exam_method, 'LOCKED', 1,
       NOW(), NOW(), NOW(), NOW(), 0, 'Demo score batch'
FROM dual
WHERE @task_id_2 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM course_score_batch WHERE batch_no = 'DEMO-T2-OBJ2-EXAM');

INSERT INTO course_score_batch (
    batch_no, task_id, objective_id, method_id, calc_status, locked_flag,
    imported_at, calculated_at, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-T2-OBJ2-PROJECT', @task_id_2, @c2_obj2, @c2_project_method, 'LOCKED', 1,
       NOW(), NOW(), NOW(), NOW(), 0, 'Demo score batch'
FROM dual
WHERE @task_id_2 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM course_score_batch WHERE batch_no = 'DEMO-T2-OBJ2-PROJECT');

INSERT INTO course_score_batch (
    batch_no, task_id, objective_id, method_id, calc_status, locked_flag,
    imported_at, calculated_at, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-T3-OBJ1-EXAM', @task_id_3, @c3_obj1, @c3_exam_method, 'LOCKED', 1,
       NOW(), NOW(), NOW(), NOW(), 0, 'Demo score batch'
FROM dual
WHERE @task_id_3 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM course_score_batch WHERE batch_no = 'DEMO-T3-OBJ1-EXAM');

INSERT INTO course_score_batch (
    batch_no, task_id, objective_id, method_id, calc_status, locked_flag,
    imported_at, calculated_at, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-T3-OBJ1-PROJECT', @task_id_3, @c3_obj1, @c3_project_method, 'LOCKED', 1,
       NOW(), NOW(), NOW(), NOW(), 0, 'Demo score batch'
FROM dual
WHERE @task_id_3 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM course_score_batch WHERE batch_no = 'DEMO-T3-OBJ1-PROJECT');

INSERT INTO course_score_batch (
    batch_no, task_id, objective_id, method_id, calc_status, locked_flag,
    imported_at, calculated_at, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-T3-OBJ2-EXAM', @task_id_3, @c3_obj2, @c3_exam_method, 'LOCKED', 1,
       NOW(), NOW(), NOW(), NOW(), 0, 'Demo score batch'
FROM dual
WHERE @task_id_3 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM course_score_batch WHERE batch_no = 'DEMO-T3-OBJ2-EXAM');

INSERT INTO course_score_batch (
    batch_no, task_id, objective_id, method_id, calc_status, locked_flag,
    imported_at, calculated_at, created_at, updated_at, is_deleted, remark
)
SELECT 'DEMO-T3-OBJ2-PROJECT', @task_id_3, @c3_obj2, @c3_project_method, 'LOCKED', 1,
       NOW(), NOW(), NOW(), NOW(), 0, 'Demo score batch'
FROM dual
WHERE @task_id_3 IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM course_score_batch WHERE batch_no = 'DEMO-T3-OBJ2-PROJECT');

-- ---------------------------------------------------------------------------
-- Seed submitted score details
-- ---------------------------------------------------------------------------
INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_a, 82.00, 82.00, 82.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T1-OBJ1-EXAM'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_a);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_b, 88.00, 88.00, 88.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T1-OBJ1-EXAM'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_b);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_a, 76.00, 76.00, 76.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T1-OBJ1-PROJECT'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_a);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_b, 84.00, 84.00, 84.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T1-OBJ1-PROJECT'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_b);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_a, 73.00, 73.00, 73.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T1-OBJ2-EXAM'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_a);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_b, 79.00, 79.00, 79.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T1-OBJ2-EXAM'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_b);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_a, 85.00, 85.00, 85.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T1-OBJ2-PROJECT'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_a);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_b, 91.00, 91.00, 91.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T1-OBJ2-PROJECT'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_b);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_a, 90.00, 90.00, 90.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T2-OBJ1-EXAM'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_a);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_b, 92.00, 92.00, 92.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T2-OBJ1-EXAM'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_b);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_a, 86.00, 86.00, 86.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T2-OBJ1-PROJECT'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_a);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_b, 89.00, 89.00, 89.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T2-OBJ1-PROJECT'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_b);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_a, 81.00, 81.00, 81.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T2-OBJ2-EXAM'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_a);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_b, 84.00, 84.00, 84.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T2-OBJ2-EXAM'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_b);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_a, 78.00, 78.00, 78.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T2-OBJ2-PROJECT'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_a);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_b, 82.00, 82.00, 82.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T2-OBJ2-PROJECT'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_b);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_c, 88.00, 88.00, 88.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T3-OBJ1-EXAM'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_c);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_c, 90.00, 90.00, 90.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T3-OBJ1-PROJECT'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_c);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_c, 74.00, 74.00, 74.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T3-OBJ2-EXAM'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_c);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type,
    source_ref_id, submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT b.id, @student_c, 79.00, 79.00, 79.00, 'MANUAL_IMPORT',
       NULL, 'SUBMITTED', 1, NOW(), NOW(), 0, 'Demo score'
FROM course_score_batch b
WHERE b.batch_no = 'DEMO-T3-OBJ2-PROJECT'
  AND NOT EXISTS (SELECT 1 FROM course_score_detail d WHERE d.batch_id = b.id AND d.student_id = @student_c);

-- ---------------------------------------------------------------------------
-- Seed graduation requirements, indicator points, and support weights
-- ---------------------------------------------------------------------------
SET @requirement_id_1 := (
    SELECT id
    FROM tr_graduation_requirement
    WHERE program_version_id = @program_version_id
      AND is_deleted = 0
    ORDER BY sort_no, id
    LIMIT 1
);

SET @indicator_id_1 := (
    SELECT id
    FROM tr_requirement_indicator_point
    WHERE graduation_requirement_id = @requirement_id_1
      AND is_deleted = 0
    ORDER BY sort_no, id
    LIMIT 1
);

INSERT INTO tr_graduation_requirement (
    program_version_id, requirement_code, requirement_name, requirement_desc,
    sort_no, enabled, created_at, updated_at, is_deleted, remark
)
SELECT @program_version_id, 'GR-02', 'Problem Analysis',
       'Graduates can identify, analyze, and model complex engineering problems.',
       2, 1, NOW(), NOW(), 0, 'Demo graduation requirement'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM tr_graduation_requirement
    WHERE program_version_id = @program_version_id
      AND requirement_code = 'GR-02'
);

INSERT INTO tr_graduation_requirement (
    program_version_id, requirement_code, requirement_name, requirement_desc,
    sort_no, enabled, created_at, updated_at, is_deleted, remark
)
SELECT @program_version_id, 'GR-03', 'Engineering Practice',
       'Graduates can apply methods and tools to deliver engineering outcomes.',
       3, 1, NOW(), NOW(), 0, 'Demo graduation requirement'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM tr_graduation_requirement
    WHERE program_version_id = @program_version_id
      AND requirement_code = 'GR-03'
);

SET @requirement_id_2 := (
    SELECT id
    FROM tr_graduation_requirement
    WHERE program_version_id = @program_version_id
      AND requirement_code = 'GR-02'
      AND is_deleted = 0
    LIMIT 1
);

SET @requirement_id_3 := (
    SELECT id
    FROM tr_graduation_requirement
    WHERE program_version_id = @program_version_id
      AND requirement_code = 'GR-03'
      AND is_deleted = 0
    LIMIT 1
);

INSERT INTO tr_requirement_indicator_point (
    graduation_requirement_id, indicator_code, indicator_name, indicator_desc,
    sort_no, enabled, created_at, updated_at, is_deleted, remark
)
SELECT @requirement_id_1, 'GR-01-IP2', 'Requirement 1 / Indicator 2',
       'Second indicator point for the first graduation requirement.',
       2, 1, NOW(), NOW(), 0, 'Demo indicator point'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM tr_requirement_indicator_point
    WHERE graduation_requirement_id = @requirement_id_1
      AND indicator_code = 'GR-01-IP2'
);

INSERT INTO tr_requirement_indicator_point (
    graduation_requirement_id, indicator_code, indicator_name, indicator_desc,
    sort_no, enabled, created_at, updated_at, is_deleted, remark
)
SELECT @requirement_id_2, 'GR-02-IP1', 'Requirement 2 / Indicator 1',
       'First indicator point for problem analysis.',
       1, 1, NOW(), NOW(), 0, 'Demo indicator point'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM tr_requirement_indicator_point
    WHERE graduation_requirement_id = @requirement_id_2
      AND indicator_code = 'GR-02-IP1'
);

INSERT INTO tr_requirement_indicator_point (
    graduation_requirement_id, indicator_code, indicator_name, indicator_desc,
    sort_no, enabled, created_at, updated_at, is_deleted, remark
)
SELECT @requirement_id_2, 'GR-02-IP2', 'Requirement 2 / Indicator 2',
       'Second indicator point for problem analysis.',
       2, 1, NOW(), NOW(), 0, 'Demo indicator point'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM tr_requirement_indicator_point
    WHERE graduation_requirement_id = @requirement_id_2
      AND indicator_code = 'GR-02-IP2'
);

INSERT INTO tr_requirement_indicator_point (
    graduation_requirement_id, indicator_code, indicator_name, indicator_desc,
    sort_no, enabled, created_at, updated_at, is_deleted, remark
)
SELECT @requirement_id_3, 'GR-03-IP1', 'Requirement 3 / Indicator 1',
       'First indicator point for engineering practice.',
       1, 1, NOW(), NOW(), 0, 'Demo indicator point'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM tr_requirement_indicator_point
    WHERE graduation_requirement_id = @requirement_id_3
      AND indicator_code = 'GR-03-IP1'
);

INSERT INTO tr_requirement_indicator_point (
    graduation_requirement_id, indicator_code, indicator_name, indicator_desc,
    sort_no, enabled, created_at, updated_at, is_deleted, remark
)
SELECT @requirement_id_3, 'GR-03-IP2', 'Requirement 3 / Indicator 2',
       'Second indicator point for engineering practice.',
       2, 1, NOW(), NOW(), 0, 'Demo indicator point'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM tr_requirement_indicator_point
    WHERE graduation_requirement_id = @requirement_id_3
      AND indicator_code = 'GR-03-IP2'
);

SET @indicator_id_2 := (
    SELECT id
    FROM tr_requirement_indicator_point
    WHERE graduation_requirement_id = @requirement_id_1
      AND indicator_code = 'GR-01-IP2'
      AND is_deleted = 0
    LIMIT 1
);

SET @indicator_id_3 := (
    SELECT id
    FROM tr_requirement_indicator_point
    WHERE graduation_requirement_id = @requirement_id_2
      AND indicator_code = 'GR-02-IP1'
      AND is_deleted = 0
    LIMIT 1
);

SET @indicator_id_4 := (
    SELECT id
    FROM tr_requirement_indicator_point
    WHERE graduation_requirement_id = @requirement_id_2
      AND indicator_code = 'GR-02-IP2'
      AND is_deleted = 0
    LIMIT 1
);

SET @indicator_id_5 := (
    SELECT id
    FROM tr_requirement_indicator_point
    WHERE graduation_requirement_id = @requirement_id_3
      AND indicator_code = 'GR-03-IP1'
      AND is_deleted = 0
    LIMIT 1
);

SET @indicator_id_6 := (
    SELECT id
    FROM tr_requirement_indicator_point
    WHERE graduation_requirement_id = @requirement_id_3
      AND indicator_code = 'GR-03-IP2'
      AND is_deleted = 0
    LIMIT 1
);

UPDATE tr_requirement_indicator_support
SET support_level = 'H',
    support_weight = 50.00,
    updated_at = NOW(),
    remark = 'Normalized by attainment demo seed'
WHERE graduation_requirement_id = @requirement_id_1
  AND indicator_point_id = @indicator_id_1
  AND is_deleted = 0;

INSERT INTO tr_requirement_indicator_support (
    graduation_requirement_id, indicator_point_id, support_level, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @requirement_id_1, @indicator_id_2, 'M', 50.00, NOW(), NOW(), 0, 'Demo support'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM tr_requirement_indicator_support
    WHERE graduation_requirement_id = @requirement_id_1
      AND indicator_point_id = @indicator_id_2
);

INSERT INTO tr_requirement_indicator_support (
    graduation_requirement_id, indicator_point_id, support_level, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @requirement_id_2, @indicator_id_3, 'H', 50.00, NOW(), NOW(), 0, 'Demo support'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM tr_requirement_indicator_support
    WHERE graduation_requirement_id = @requirement_id_2
      AND indicator_point_id = @indicator_id_3
);

INSERT INTO tr_requirement_indicator_support (
    graduation_requirement_id, indicator_point_id, support_level, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @requirement_id_2, @indicator_id_4, 'M', 50.00, NOW(), NOW(), 0, 'Demo support'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM tr_requirement_indicator_support
    WHERE graduation_requirement_id = @requirement_id_2
      AND indicator_point_id = @indicator_id_4
);

INSERT INTO tr_requirement_indicator_support (
    graduation_requirement_id, indicator_point_id, support_level, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @requirement_id_3, @indicator_id_5, 'H', 50.00, NOW(), NOW(), 0, 'Demo support'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM tr_requirement_indicator_support
    WHERE graduation_requirement_id = @requirement_id_3
      AND indicator_point_id = @indicator_id_5
);

INSERT INTO tr_requirement_indicator_support (
    graduation_requirement_id, indicator_point_id, support_level, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @requirement_id_3, @indicator_id_6, 'M', 50.00, NOW(), NOW(), 0, 'Demo support'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM tr_requirement_indicator_support
    WHERE graduation_requirement_id = @requirement_id_3
      AND indicator_point_id = @indicator_id_6
);

-- ---------------------------------------------------------------------------
-- Seed objective-to-indicator mappings. These are mandatory for graduation aggregation.
-- Each indicator point is mapped to one or more course objectives and the weights
-- under the same indicator are designed to sum to 100.
-- ---------------------------------------------------------------------------
INSERT INTO edu_course_objective_indicator_point (
    course_objective_id, indicator_point_id, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @c1_obj1, @indicator_id_1, 50.00, NOW(), NOW(), 0, 'Demo mapping'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective_indicator_point
    WHERE course_objective_id = @c1_obj1
      AND indicator_point_id = @indicator_id_1
);

INSERT INTO edu_course_objective_indicator_point (
    course_objective_id, indicator_point_id, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @c2_obj1, @indicator_id_1, 50.00, NOW(), NOW(), 0, 'Demo mapping'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective_indicator_point
    WHERE course_objective_id = @c2_obj1
      AND indicator_point_id = @indicator_id_1
);

INSERT INTO edu_course_objective_indicator_point (
    course_objective_id, indicator_point_id, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @c1_obj2, @indicator_id_2, 50.00, NOW(), NOW(), 0, 'Demo mapping'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective_indicator_point
    WHERE course_objective_id = @c1_obj2
      AND indicator_point_id = @indicator_id_2
);

INSERT INTO edu_course_objective_indicator_point (
    course_objective_id, indicator_point_id, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @c3_obj1, @indicator_id_2, 50.00, NOW(), NOW(), 0, 'Demo mapping'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective_indicator_point
    WHERE course_objective_id = @c3_obj1
      AND indicator_point_id = @indicator_id_2
);

INSERT INTO edu_course_objective_indicator_point (
    course_objective_id, indicator_point_id, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @c2_obj2, @indicator_id_3, 60.00, NOW(), NOW(), 0, 'Demo mapping'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective_indicator_point
    WHERE course_objective_id = @c2_obj2
      AND indicator_point_id = @indicator_id_3
);

INSERT INTO edu_course_objective_indicator_point (
    course_objective_id, indicator_point_id, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @c3_obj2, @indicator_id_3, 40.00, NOW(), NOW(), 0, 'Demo mapping'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective_indicator_point
    WHERE course_objective_id = @c3_obj2
      AND indicator_point_id = @indicator_id_3
);

INSERT INTO edu_course_objective_indicator_point (
    course_objective_id, indicator_point_id, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @c1_obj1, @indicator_id_4, 40.00, NOW(), NOW(), 0, 'Demo mapping'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective_indicator_point
    WHERE course_objective_id = @c1_obj1
      AND indicator_point_id = @indicator_id_4
);

INSERT INTO edu_course_objective_indicator_point (
    course_objective_id, indicator_point_id, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @c3_obj2, @indicator_id_4, 60.00, NOW(), NOW(), 0, 'Demo mapping'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective_indicator_point
    WHERE course_objective_id = @c3_obj2
      AND indicator_point_id = @indicator_id_4
);

INSERT INTO edu_course_objective_indicator_point (
    course_objective_id, indicator_point_id, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @c1_obj2, @indicator_id_5, 50.00, NOW(), NOW(), 0, 'Demo mapping'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective_indicator_point
    WHERE course_objective_id = @c1_obj2
      AND indicator_point_id = @indicator_id_5
);

INSERT INTO edu_course_objective_indicator_point (
    course_objective_id, indicator_point_id, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @c2_obj1, @indicator_id_5, 50.00, NOW(), NOW(), 0, 'Demo mapping'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective_indicator_point
    WHERE course_objective_id = @c2_obj1
      AND indicator_point_id = @indicator_id_5
);

INSERT INTO edu_course_objective_indicator_point (
    course_objective_id, indicator_point_id, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @c2_obj2, @indicator_id_6, 50.00, NOW(), NOW(), 0, 'Demo mapping'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective_indicator_point
    WHERE course_objective_id = @c2_obj2
      AND indicator_point_id = @indicator_id_6
);

INSERT INTO edu_course_objective_indicator_point (
    course_objective_id, indicator_point_id, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT @c3_obj1, @indicator_id_6, 50.00, NOW(), NOW(), 0, 'Demo mapping'
FROM dual
WHERE NOT EXISTS (
    SELECT 1
    FROM edu_course_objective_indicator_point
    WHERE course_objective_id = @c3_obj1
      AND indicator_point_id = @indicator_id_6
);

COMMIT;

-- Quick check targets after import:
-- 1. course_score_batch should be >= 14
-- 2. edu_course_objective_indicator_point should be >= 12
-- 3. tr_graduation_requirement should be >= 3
-- 4. eval_model should include CT_DEMO_PV_20260722 and GR_DEMO_PV_20260722
