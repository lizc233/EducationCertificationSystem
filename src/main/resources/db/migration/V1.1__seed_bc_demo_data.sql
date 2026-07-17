-- Seed and normalize demo data for member B/C features (F06-F15).
-- The script is written to be idempotent on top of V1.0.
-- Demo file rows in sys_file point to:
-- D:/javacode/Project/downloadtest/bc-demo-files
-- so local preview/download can work immediately on this machine.
SET NAMES utf8mb4;

-- -----------------------------
-- 1) Demo users
-- -----------------------------
UPDATE sys_user
SET username = 'se_teacher_demo',
    real_name = '软件工程示例教师',
    email = 'se.teacher.demo@example.com',
    user_status = 1,
    is_deleted = 0,
    remark = 'F06-F15 演示用户'
WHERE username = 'codex_teacher'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM sys_user WHERE username = 'se_teacher_demo' LIMIT 1) t
  );

INSERT INTO sys_user (
    username, password_hash, real_name, phone, email, user_status,
    created_at, updated_at, is_deleted, remark
)
SELECT
    'se_teacher_demo',
    'BC_DEMO_HASH_TEACHER',
    '软件工程示例教师',
    NULL,
    'se.teacher.demo@example.com',
    1,
    NOW(),
    NOW(),
    0,
    'F06-F15 演示用户'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user WHERE username = 'se_teacher_demo'
);

UPDATE sys_user
SET username = 'se_student_demo_01',
    real_name = '软件工程示例学生甲',
    email = 'se.student.demo.01@example.com',
    user_status = 1,
    is_deleted = 0,
    remark = 'F06-F15 演示用户'
WHERE username = 'codex_student_01'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM sys_user WHERE username = 'se_student_demo_01' LIMIT 1) t
  );

INSERT INTO sys_user (
    username, password_hash, real_name, phone, email, user_status,
    created_at, updated_at, is_deleted, remark
)
SELECT
    'se_student_demo_01',
    'BC_DEMO_HASH_STUDENT_01',
    '软件工程示例学生甲',
    NULL,
    'se.student.demo.01@example.com',
    1,
    NOW(),
    NOW(),
    0,
    'F06-F15 演示用户'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user WHERE username = 'se_student_demo_01'
);

UPDATE sys_user
SET username = 'se_student_demo_02',
    real_name = '软件工程示例学生乙',
    email = 'se.student.demo.02@example.com',
    user_status = 1,
    is_deleted = 0,
    remark = 'F06-F15 演示用户'
WHERE username = 'codex_student_02'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM sys_user WHERE username = 'se_student_demo_02' LIMIT 1) t
  );

INSERT INTO sys_user (
    username, password_hash, real_name, phone, email, user_status,
    created_at, updated_at, is_deleted, remark
)
SELECT
    'se_student_demo_02',
    'BC_DEMO_HASH_STUDENT_02',
    '软件工程示例学生乙',
    NULL,
    'se.student.demo.02@example.com',
    1,
    NOW(),
    NOW(),
    0,
    'F06-F15 演示用户'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user WHERE username = 'se_student_demo_02'
);

SET @teacher_user_id := (
    SELECT id FROM sys_user WHERE username = 'se_teacher_demo' AND is_deleted = 0 LIMIT 1
);
SET @student_user_01_id := (
    SELECT id FROM sys_user WHERE username = 'se_student_demo_01' AND is_deleted = 0 LIMIT 1
);
SET @student_user_02_id := (
    SELECT id FROM sys_user WHERE username = 'se_student_demo_02' AND is_deleted = 0 LIMIT 1
);

-- -----------------------------
-- 2) Base organization/lookups
-- -----------------------------
UPDATE org_college
SET college_code = 'SE-COL',
    college_name = '软件工程学院',
    sort_no = 10,
    status = 1,
    is_deleted = 0,
    remark = 'F06-F15 演示基础数据'
WHERE college_code = 'CODX-COL'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM org_college WHERE college_code = 'SE-COL' LIMIT 1) t
  );

INSERT INTO org_college (
    college_code, college_name, sort_no, status,
    created_at, updated_at, is_deleted, remark
)
SELECT
    'SE-COL',
    '软件工程学院',
    10,
    1,
    NOW(),
    NOW(),
    0,
    'F06-F15 演示基础数据'
WHERE NOT EXISTS (
    SELECT 1 FROM org_college WHERE college_code = 'SE-COL'
);

SET @college_id := (
    SELECT id FROM org_college WHERE college_code = 'SE-COL' AND is_deleted = 0 LIMIT 1
);

UPDATE org_major
SET college_id = @college_id,
    major_code = 'SE-MAJ',
    major_name = '软件工程',
    degree_type = '本科',
    sort_no = 10,
    status = 1,
    is_deleted = 0,
    remark = 'F06-F15 演示基础数据'
WHERE major_code = 'CODX-MAJ'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM org_major WHERE major_code = 'SE-MAJ' LIMIT 1) t
  );

INSERT INTO org_major (
    college_id, major_code, major_name, degree_type, sort_no, status,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @college_id,
    'SE-MAJ',
    '软件工程',
    '本科',
    10,
    1,
    NOW(),
    NOW(),
    0,
    'F06-F15 演示基础数据'
WHERE NOT EXISTS (
    SELECT 1 FROM org_major WHERE major_code = 'SE-MAJ'
);

SET @major_id := (
    SELECT id FROM org_major WHERE major_code = 'SE-MAJ' AND is_deleted = 0 LIMIT 1
);

INSERT INTO org_grade (
    grade_year, admission_year, expected_graduation_year, status,
    created_at, updated_at, is_deleted, remark
)
SELECT
    2024,
    2024,
    2028,
    1,
    NOW(),
    NOW(),
    0,
    'F06-F15 演示年级'
WHERE NOT EXISTS (
    SELECT 1 FROM org_grade WHERE grade_year = 2024
);

UPDATE org_grade
SET admission_year = 2024,
    expected_graduation_year = 2028,
    status = 1,
    is_deleted = 0,
    remark = 'F06-F15 演示年级'
WHERE grade_year = 2024;

INSERT INTO org_grade (
    grade_year, admission_year, expected_graduation_year, status,
    created_at, updated_at, is_deleted, remark
)
SELECT
    2025,
    2025,
    2029,
    1,
    NOW(),
    NOW(),
    0,
    'F06-F15 演示年级'
WHERE NOT EXISTS (
    SELECT 1 FROM org_grade WHERE grade_year = 2025
);

UPDATE org_grade
SET admission_year = 2025,
    expected_graduation_year = 2029,
    status = 1,
    is_deleted = 0,
    remark = 'F06-F15 演示年级'
WHERE grade_year = 2025;

SET @grade_2024_id := (
    SELECT id FROM org_grade WHERE grade_year = 2024 AND is_deleted = 0 LIMIT 1
);
SET @grade_2025_id := (
    SELECT id FROM org_grade WHERE grade_year = 2025 AND is_deleted = 0 LIMIT 1
);

UPDATE edu_teacher
SET user_id = @teacher_user_id,
    teacher_no = 'T-SE-001',
    college_id = @college_id,
    major_id = @major_id,
    title = '讲师',
    job_title = '课程负责人',
    email = 'se.teacher.demo@example.com',
    status = 1,
    is_deleted = 0,
    remark = 'F12-F15 演示教师'
WHERE teacher_no = 'T-CODX-001'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM edu_teacher WHERE teacher_no = 'T-SE-001' LIMIT 1) t
  );

INSERT INTO edu_teacher (
    user_id, teacher_no, college_id, major_id, title, job_title, phone, email, status,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @teacher_user_id,
    'T-SE-001',
    @college_id,
    @major_id,
    '讲师',
    '课程负责人',
    NULL,
    'se.teacher.demo@example.com',
    1,
    NOW(),
    NOW(),
    0,
    'F12-F15 演示教师'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_teacher WHERE teacher_no = 'T-SE-001'
);

SET @teacher_id := (
    SELECT id FROM edu_teacher WHERE teacher_no = 'T-SE-001' AND is_deleted = 0 LIMIT 1
);

UPDATE org_class
SET major_id = @major_id,
    grade_id = @grade_2024_id,
    class_code = 'SE2401',
    class_name = '软件工程 2401 班',
    head_teacher_id = @teacher_user_id,
    student_count = 2,
    status = 1,
    is_deleted = 0,
    remark = 'F12-F15 演示班级'
WHERE class_code = 'CODX-CLS-01'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM org_class WHERE class_code = 'SE2401' LIMIT 1) t
  );

INSERT INTO org_class (
    major_id, grade_id, class_code, class_name, head_teacher_id, student_count, status,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @major_id,
    @grade_2024_id,
    'SE2401',
    '软件工程 2401 班',
    @teacher_user_id,
    2,
    1,
    NOW(),
    NOW(),
    0,
    'F12-F15 演示班级'
WHERE NOT EXISTS (
    SELECT 1 FROM org_class WHERE class_code = 'SE2401'
);

SET @class_id := (
    SELECT id FROM org_class WHERE class_code = 'SE2401' AND is_deleted = 0 LIMIT 1
);

UPDATE edu_student
SET user_id = @student_user_01_id,
    student_no = '20240001',
    class_id = @class_id,
    admission_year = 2024,
    gender = '男',
    status = 1,
    graduation_status = 0,
    is_deleted = 0,
    remark = 'F14-F15 演示学生'
WHERE student_no = 'S-CODX-001'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM edu_student WHERE student_no = '20240001' LIMIT 1) t
  );

INSERT INTO edu_student (
    user_id, student_no, class_id, admission_year, gender, status, graduation_status,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @student_user_01_id,
    '20240001',
    @class_id,
    2024,
    '男',
    1,
    0,
    NOW(),
    NOW(),
    0,
    'F14-F15 演示学生'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_student WHERE student_no = '20240001'
);

UPDATE edu_student
SET user_id = @student_user_02_id,
    student_no = '20240002',
    class_id = @class_id,
    admission_year = 2024,
    gender = '女',
    status = 1,
    graduation_status = 0,
    is_deleted = 0,
    remark = 'F14-F15 演示学生'
WHERE student_no = 'S-CODX-002'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM edu_student WHERE student_no = '20240002' LIMIT 1) t
  );

INSERT INTO edu_student (
    user_id, student_no, class_id, admission_year, gender, status, graduation_status,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @student_user_02_id,
    '20240002',
    @class_id,
    2024,
    '女',
    1,
    0,
    NOW(),
    NOW(),
    0,
    'F14-F15 演示学生'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_student WHERE student_no = '20240002'
);

SET @student_01_id := (
    SELECT id FROM edu_student WHERE student_no = '20240001' AND is_deleted = 0 LIMIT 1
);
SET @student_02_id := (
    SELECT id FROM edu_student WHERE student_no = '20240002' AND is_deleted = 0 LIMIT 1
);

UPDATE edu_semester
SET semester_code = '2026-2027-1',
    semester_name = '2026-2027学年第一学期',
    start_date = '2026-09-01',
    end_date = '2027-01-20',
    active_flag = 1,
    is_deleted = 0,
    remark = 'F12-F15 演示学期'
WHERE semester_code = '2026-2027-1';

INSERT INTO edu_semester (
    semester_code, semester_name, start_date, end_date, active_flag,
    created_at, updated_at, is_deleted, remark
)
SELECT
    '2026-2027-2',
    '2026-2027学年第二学期',
    '2027-02-23',
    '2027-07-02',
    0,
    NOW(),
    NOW(),
    0,
    'F12-F15 演示学期'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_semester WHERE semester_code = '2026-2027-2'
);

SET @semester_1_id := (
    SELECT id FROM edu_semester WHERE semester_code = '2026-2027-1' AND is_deleted = 0 LIMIT 1
);
SET @semester_2_id := (
    SELECT id FROM edu_semester WHERE semester_code = '2026-2027-2' AND is_deleted = 0 LIMIT 1
);

UPDATE edu_course
SET course_code = 'SE101',
    course_name = '程序设计基础',
    course_type = 'CORE',
    credit = 3.0,
    total_hours = 48,
    theory_hours = 32,
    practice_hours = 16,
    offering_unit_id = @college_id,
    status = 1,
    is_deleted = 0,
    remark = 'F09-F15 演示课程'
WHERE course_code = 'COURSE-124045'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM edu_course WHERE course_code = 'SE101' LIMIT 1) t
  );

INSERT INTO edu_course (
    course_code, course_name, course_type, credit, total_hours, theory_hours, practice_hours,
    offering_unit_id, status, created_at, updated_at, is_deleted, remark
)
SELECT
    'SE101',
    '程序设计基础',
    'CORE',
    3.0,
    48,
    32,
    16,
    @college_id,
    1,
    NOW(),
    NOW(),
    0,
    'F09-F15 演示课程'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course WHERE course_code = 'SE101'
);

UPDATE edu_course
SET course_code = 'SE201',
    course_name = '软件工程项目实践',
    course_type = 'ELECTIVE',
    credit = 2.0,
    total_hours = 32,
    theory_hours = 16,
    practice_hours = 16,
    offering_unit_id = @college_id,
    status = 1,
    is_deleted = 0,
    remark = 'F09-F15 演示课程'
WHERE course_code = 'COURSE-DIS-124045'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM edu_course WHERE course_code = 'SE201' LIMIT 1) t
  );

INSERT INTO edu_course (
    course_code, course_name, course_type, credit, total_hours, theory_hours, practice_hours,
    offering_unit_id, status, created_at, updated_at, is_deleted, remark
)
SELECT
    'SE201',
    '软件工程项目实践',
    'ELECTIVE',
    2.0,
    32,
    16,
    16,
    @college_id,
    1,
    NOW(),
    NOW(),
    0,
    'F09-F15 演示课程'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course WHERE course_code = 'SE201'
);

SET @course_se101_id := (
    SELECT id FROM edu_course WHERE course_code = 'SE101' AND is_deleted = 0 LIMIT 1
);
SET @course_se201_id := (
    SELECT id FROM edu_course WHERE course_code = 'SE201' AND is_deleted = 0 LIMIT 1
);

-- -----------------------------
-- 3) Stable files for F13-F14
-- -----------------------------
UPDATE sys_file
SET biz_type = 'COURSE_RESOURCE',
    biz_id = 1,
    original_name = '程序设计基础课程大纲.txt',
    stored_name = 'bc_demo_course_outline.txt',
    file_ext = 'txt',
    file_size = 118,
    mime_type = 'text/plain',
    storage_type = 'LOCAL',
    storage_path = 'D:\\javacode\\Project\\downloadtest\\bc-demo-files\\bc_demo_course_outline.txt',
    md5 = '905E65D0DE9823A542C60779D99C8DC5',
    upload_user_id = @teacher_user_id,
    visibility_scope = 'PRIVATE',
    file_status = 1,
    is_deleted = 0,
    remark = 'F13 课程资源演示文件'
WHERE original_name = 'resource-124045.txt'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM sys_file WHERE stored_name = 'bc_demo_course_outline.txt' LIMIT 1) t
  );

INSERT INTO sys_file (
    biz_type, biz_id, original_name, stored_name, file_ext, file_size, mime_type,
    storage_type, storage_path, md5, upload_user_id, visibility_scope, file_status,
    created_at, updated_at, is_deleted, remark
)
SELECT
    'COURSE_RESOURCE',
    1,
    '程序设计基础课程大纲.txt',
    'bc_demo_course_outline.txt',
    'txt',
    118,
    'text/plain',
    'LOCAL',
    'D:\\javacode\\Project\\downloadtest\\bc-demo-files\\bc_demo_course_outline.txt',
    '905E65D0DE9823A542C60779D99C8DC5',
    @teacher_user_id,
    'PRIVATE',
    1,
    NOW(),
    NOW(),
    0,
    'F13 课程资源演示文件'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_file WHERE stored_name = 'bc_demo_course_outline.txt'
);

UPDATE sys_file
SET biz_type = 'EVIDENCE_MATERIAL',
    biz_id = 1,
    original_name = '程序设计基础-平时作业-20240001.txt',
    stored_name = 'bc_demo_homework_20240001.txt',
    file_ext = 'txt',
    file_size = 102,
    mime_type = 'text/plain',
    storage_type = 'LOCAL',
    storage_path = 'D:\\javacode\\Project\\downloadtest\\bc-demo-files\\bc_demo_homework_20240001.txt',
    md5 = '9360AAAFCAC677DD2801F7A584AEC5EA',
    upload_user_id = @teacher_user_id,
    visibility_scope = 'PRIVATE',
    file_status = 1,
    is_deleted = 0,
    remark = 'F14 证据材料演示文件'
WHERE original_name = 'evidence-a-124045.txt'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM sys_file WHERE stored_name = 'bc_demo_homework_20240001.txt' LIMIT 1) t
  );

INSERT INTO sys_file (
    biz_type, biz_id, original_name, stored_name, file_ext, file_size, mime_type,
    storage_type, storage_path, md5, upload_user_id, visibility_scope, file_status,
    created_at, updated_at, is_deleted, remark
)
SELECT
    'EVIDENCE_MATERIAL',
    1,
    '程序设计基础-平时作业-20240001.txt',
    'bc_demo_homework_20240001.txt',
    'txt',
    102,
    'text/plain',
    'LOCAL',
    'D:\\javacode\\Project\\downloadtest\\bc-demo-files\\bc_demo_homework_20240001.txt',
    '9360AAAFCAC677DD2801F7A584AEC5EA',
    @teacher_user_id,
    'PRIVATE',
    1,
    NOW(),
    NOW(),
    0,
    'F14 证据材料演示文件'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_file WHERE stored_name = 'bc_demo_homework_20240001.txt'
);

UPDATE sys_file
SET biz_type = 'EVIDENCE_MATERIAL',
    biz_id = 2,
    original_name = '程序设计基础-课程报告-20240002.txt',
    stored_name = 'bc_demo_report_20240002.txt',
    file_ext = 'txt',
    file_size = 102,
    mime_type = 'text/plain',
    storage_type = 'LOCAL',
    storage_path = 'D:\\javacode\\Project\\downloadtest\\bc-demo-files\\bc_demo_report_20240002.txt',
    md5 = 'D088A4CB072054F49D13A1DB9D943D1D',
    upload_user_id = @teacher_user_id,
    visibility_scope = 'PRIVATE',
    file_status = 1,
    is_deleted = 0,
    remark = 'F14 证据材料演示文件'
WHERE original_name = 'evidence-b-124045.txt'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM sys_file WHERE stored_name = 'bc_demo_report_20240002.txt' LIMIT 1) t
  );

INSERT INTO sys_file (
    biz_type, biz_id, original_name, stored_name, file_ext, file_size, mime_type,
    storage_type, storage_path, md5, upload_user_id, visibility_scope, file_status,
    created_at, updated_at, is_deleted, remark
)
SELECT
    'EVIDENCE_MATERIAL',
    2,
    '程序设计基础-课程报告-20240002.txt',
    'bc_demo_report_20240002.txt',
    'txt',
    102,
    'text/plain',
    'LOCAL',
    'D:\\javacode\\Project\\downloadtest\\bc-demo-files\\bc_demo_report_20240002.txt',
    'D088A4CB072054F49D13A1DB9D943D1D',
    @teacher_user_id,
    'PRIVATE',
    1,
    NOW(),
    NOW(),
    0,
    'F14 证据材料演示文件'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_file WHERE stored_name = 'bc_demo_report_20240002.txt'
);

SET @resource_file_id := (
    SELECT id FROM sys_file WHERE stored_name = 'bc_demo_course_outline.txt' AND is_deleted = 0 LIMIT 1
);
SET @evidence_file_01_id := (
    SELECT id FROM sys_file WHERE stored_name = 'bc_demo_homework_20240001.txt' AND is_deleted = 0 LIMIT 1
);
SET @evidence_file_02_id := (
    SELECT id FROM sys_file WHERE stored_name = 'bc_demo_report_20240002.txt' AND is_deleted = 0 LIMIT 1
);

-- -----------------------------
-- 4) Program data (F06-F08)
-- -----------------------------
UPDATE tr_program_version
SET major_id = @major_id,
    version_no = 'SE-PV-2024',
    version_name = '软件工程专业 2024 版培养方案',
    effective_date = '2024-09-01',
    status = 'RELEASED',
    released_at = COALESCE(released_at, NOW()),
    is_deleted = 0,
    remark = 'F06-F15 主演示方案'
WHERE version_no = 'CODX-VER-A-124045'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM tr_program_version WHERE major_id = @major_id AND version_no = 'SE-PV-2024' LIMIT 1) t
  );

INSERT INTO tr_program_version (
    major_id, version_no, version_name, effective_date, status, copy_from_version_id,
    released_at, created_at, updated_at, is_deleted, remark
)
SELECT
    @major_id,
    'SE-PV-2024',
    '软件工程专业 2024 版培养方案',
    '2024-09-01',
    'RELEASED',
    NULL,
    NOW(),
    NOW(),
    NOW(),
    0,
    'F06-F15 主演示方案'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_program_version WHERE major_id = @major_id AND version_no = 'SE-PV-2024'
);

SET @program_version_main_id := (
    SELECT id FROM tr_program_version WHERE major_id = @major_id AND version_no = 'SE-PV-2024' AND is_deleted = 0 LIMIT 1
);

UPDATE tr_program_version
SET major_id = @major_id,
    version_no = 'SE-PV-2025',
    version_name = '软件工程专业 2025 修订方案',
    effective_date = '2025-09-01',
    status = 'DRAFT',
    copy_from_version_id = @program_version_main_id,
    released_at = NULL,
    is_deleted = 0,
    remark = 'F06 版本复制与对比演示'
WHERE version_no = 'CODX-VER-B-124045'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM tr_program_version WHERE major_id = @major_id AND version_no = 'SE-PV-2025' LIMIT 1) t
  );

INSERT INTO tr_program_version (
    major_id, version_no, version_name, effective_date, status, copy_from_version_id,
    released_at, created_at, updated_at, is_deleted, remark
)
SELECT
    @major_id,
    'SE-PV-2025',
    '软件工程专业 2025 修订方案',
    '2025-09-01',
    'DRAFT',
    @program_version_main_id,
    NULL,
    NOW(),
    NOW(),
    0,
    'F06 版本复制与对比演示'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_program_version WHERE major_id = @major_id AND version_no = 'SE-PV-2025'
);

SET @program_version_draft_id := (
    SELECT id FROM tr_program_version WHERE major_id = @major_id AND version_no = 'SE-PV-2025' AND is_deleted = 0 LIMIT 1
);

UPDATE tr_program_version
SET copy_from_version_id = @program_version_main_id,
    updated_at = NOW()
WHERE id = @program_version_draft_id;

UPDATE tr_program_version
SET is_deleted = 1,
    updated_at = NOW(),
    remark = 'V1.1 清理遗留联调版本'
WHERE version_no LIKE 'COD-F06-%';

INSERT INTO tr_program_apply_grade (
    program_version_id, grade_id, created_at, updated_at, is_deleted, remark
)
SELECT
    @program_version_main_id,
    @grade_2024_id,
    NOW(),
    NOW(),
    0,
    'F06 主版本适用年级'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_program_apply_grade
    WHERE program_version_id = @program_version_main_id
      AND grade_id = @grade_2024_id
);

UPDATE tr_program_apply_grade
SET is_deleted = 0,
    remark = 'F06 主版本适用年级',
    updated_at = NOW()
WHERE program_version_id = @program_version_main_id
  AND grade_id = @grade_2024_id;

INSERT INTO tr_program_apply_grade (
    program_version_id, grade_id, created_at, updated_at, is_deleted, remark
)
SELECT
    @program_version_draft_id,
    @grade_2025_id,
    NOW(),
    NOW(),
    0,
    'F06 修订版本适用年级'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_program_apply_grade
    WHERE program_version_id = @program_version_draft_id
      AND grade_id = @grade_2025_id
);

UPDATE tr_program_apply_grade
SET is_deleted = 0,
    remark = 'F06 修订版本适用年级',
    updated_at = NOW()
WHERE program_version_id = @program_version_draft_id
  AND grade_id = @grade_2025_id;

UPDATE tr_program_target
SET program_version_id = @program_version_main_id,
    target_code = 'TG1',
    target_name = '职业素养与社会责任',
    target_desc = '具备良好的职业道德、沟通表达能力和社会责任意识，能够在工程实践中遵守规范。',
    sort_no = 10,
    enabled = 1,
    is_deleted = 0,
    remark = 'F07 演示培养目标'
WHERE target_code = 'TGT-124045'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM tr_program_target WHERE program_version_id = @program_version_main_id AND target_code = 'TG1' LIMIT 1) t
  );

INSERT INTO tr_program_target (
    program_version_id, target_code, target_name, target_desc, sort_no, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @program_version_main_id,
    'TG1',
    '职业素养与社会责任',
    '具备良好的职业道德、沟通表达能力和社会责任意识，能够在工程实践中遵守规范。',
    10,
    1,
    NOW(),
    NOW(),
    0,
    'F07 演示培养目标'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_program_target
    WHERE program_version_id = @program_version_main_id
      AND target_code = 'TG1'
);

INSERT INTO tr_program_target (
    program_version_id, target_code, target_name, target_desc, sort_no, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @program_version_main_id,
    'TG2',
    '工程实践与团队协作',
    '能够参与软件系统设计与实现，完成团队协作、文档编写和项目交付。',
    20,
    1,
    NOW(),
    NOW(),
    0,
    'F07 演示培养目标'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_program_target
    WHERE program_version_id = @program_version_main_id
      AND target_code = 'TG2'
);

SET @target_1_id := (
    SELECT id FROM tr_program_target
    WHERE program_version_id = @program_version_main_id AND target_code = 'TG1' AND is_deleted = 0
    LIMIT 1
);
SET @target_2_id := (
    SELECT id FROM tr_program_target
    WHERE program_version_id = @program_version_main_id AND target_code = 'TG2' AND is_deleted = 0
    LIMIT 1
);

UPDATE tr_graduation_requirement
SET program_version_id = @program_version_main_id,
    requirement_code = 'GR1',
    requirement_name = '工程知识应用',
    requirement_desc = '能够将数学、程序设计与软件工程基础知识用于分析和解决典型软件问题。',
    sort_no = 10,
    enabled = 1,
    is_deleted = 0,
    remark = 'F07 演示毕业要求'
WHERE requirement_code = 'REQ1-124045'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM tr_graduation_requirement WHERE program_version_id = @program_version_main_id AND requirement_code = 'GR1' LIMIT 1) t
  );

INSERT INTO tr_graduation_requirement (
    program_version_id, requirement_code, requirement_name, requirement_desc, sort_no, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @program_version_main_id,
    'GR1',
    '工程知识应用',
    '能够将数学、程序设计与软件工程基础知识用于分析和解决典型软件问题。',
    10,
    1,
    NOW(),
    NOW(),
    0,
    'F07 演示毕业要求'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_graduation_requirement
    WHERE program_version_id = @program_version_main_id
      AND requirement_code = 'GR1'
);

UPDATE tr_graduation_requirement
SET program_version_id = @program_version_main_id,
    requirement_code = 'GR2',
    requirement_name = '系统设计与问题分析',
    requirement_desc = '能够围绕给定需求完成软件系统方案设计、模块划分和关键实现决策。',
    sort_no = 20,
    enabled = 1,
    is_deleted = 0,
    remark = 'F07 演示毕业要求'
WHERE requirement_code = 'REQ2-124045'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM tr_graduation_requirement WHERE program_version_id = @program_version_main_id AND requirement_code = 'GR2' LIMIT 1) t
  );

INSERT INTO tr_graduation_requirement (
    program_version_id, requirement_code, requirement_name, requirement_desc, sort_no, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @program_version_main_id,
    'GR2',
    '系统设计与问题分析',
    '能够围绕给定需求完成软件系统方案设计、模块划分和关键实现决策。',
    20,
    1,
    NOW(),
    NOW(),
    0,
    'F07 演示毕业要求'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_graduation_requirement
    WHERE program_version_id = @program_version_main_id
      AND requirement_code = 'GR2'
);

SET @requirement_1_id := (
    SELECT id FROM tr_graduation_requirement
    WHERE program_version_id = @program_version_main_id AND requirement_code = 'GR1' AND is_deleted = 0
    LIMIT 1
);
SET @requirement_2_id := (
    SELECT id FROM tr_graduation_requirement
    WHERE program_version_id = @program_version_main_id AND requirement_code = 'GR2' AND is_deleted = 0
    LIMIT 1
);

UPDATE tr_requirement_indicator_point
SET graduation_requirement_id = @requirement_1_id,
    indicator_code = 'GR1.1',
    indicator_name = '基础理论应用',
    indicator_desc = '能够将程序设计基础知识用于完成简单算法设计和代码实现。',
    sort_no = 10,
    enabled = 1,
    is_deleted = 0,
    remark = 'F07 演示指标点'
WHERE indicator_code = 'IND1-124045'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM tr_requirement_indicator_point WHERE graduation_requirement_id = @requirement_1_id AND indicator_code = 'GR1.1' LIMIT 1) t
  );

INSERT INTO tr_requirement_indicator_point (
    graduation_requirement_id, indicator_code, indicator_name, indicator_desc, sort_no, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @requirement_1_id,
    'GR1.1',
    '基础理论应用',
    '能够将程序设计基础知识用于完成简单算法设计和代码实现。',
    10,
    1,
    NOW(),
    NOW(),
    0,
    'F07 演示指标点'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_requirement_indicator_point
    WHERE graduation_requirement_id = @requirement_1_id
      AND indicator_code = 'GR1.1'
);

UPDATE tr_requirement_indicator_point
SET graduation_requirement_id = @requirement_2_id,
    indicator_code = 'GR2.1',
    indicator_name = '系统方案设计',
    indicator_desc = '能够根据业务需求完成模块划分、数据结构设计和实现路径说明。',
    sort_no = 10,
    enabled = 1,
    is_deleted = 0,
    remark = 'F07 演示指标点'
WHERE indicator_code = 'IND2-124045'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM tr_requirement_indicator_point WHERE graduation_requirement_id = @requirement_2_id AND indicator_code = 'GR2.1' LIMIT 1) t
  );

INSERT INTO tr_requirement_indicator_point (
    graduation_requirement_id, indicator_code, indicator_name, indicator_desc, sort_no, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @requirement_2_id,
    'GR2.1',
    '系统方案设计',
    '能够根据业务需求完成模块划分、数据结构设计和实现路径说明。',
    10,
    1,
    NOW(),
    NOW(),
    0,
    'F07 演示指标点'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_requirement_indicator_point
    WHERE graduation_requirement_id = @requirement_2_id
      AND indicator_code = 'GR2.1'
);

SET @indicator_1_id := (
    SELECT id FROM tr_requirement_indicator_point
    WHERE graduation_requirement_id = @requirement_1_id AND indicator_code = 'GR1.1' AND is_deleted = 0
    LIMIT 1
);
SET @indicator_2_id := (
    SELECT id FROM tr_requirement_indicator_point
    WHERE graduation_requirement_id = @requirement_2_id AND indicator_code = 'GR2.1' AND is_deleted = 0
    LIMIT 1
);

INSERT INTO tr_target_requirement_support (
    program_target_id, graduation_requirement_id, support_level, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @target_1_id,
    @requirement_1_id,
    'H',
    1.00,
    NOW(),
    NOW(),
    0,
    'F07 演示目标支撑关系'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_target_requirement_support
    WHERE program_target_id = @target_1_id
      AND graduation_requirement_id = @requirement_1_id
);

UPDATE tr_target_requirement_support
SET support_level = 'H',
    support_weight = 1.00,
    is_deleted = 0,
    remark = 'F07 演示目标支撑关系',
    updated_at = NOW()
WHERE program_target_id = @target_1_id
  AND graduation_requirement_id = @requirement_1_id;

INSERT INTO tr_target_requirement_support (
    program_target_id, graduation_requirement_id, support_level, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @target_2_id,
    @requirement_2_id,
    'H',
    1.00,
    NOW(),
    NOW(),
    0,
    'F07 演示目标支撑关系'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_target_requirement_support
    WHERE program_target_id = @target_2_id
      AND graduation_requirement_id = @requirement_2_id
);

UPDATE tr_target_requirement_support
SET support_level = 'H',
    support_weight = 1.00,
    is_deleted = 0,
    remark = 'F07 演示目标支撑关系',
    updated_at = NOW()
WHERE program_target_id = @target_2_id
  AND graduation_requirement_id = @requirement_2_id;

INSERT INTO tr_requirement_indicator_support (
    graduation_requirement_id, indicator_point_id, support_level, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @requirement_1_id,
    @indicator_1_id,
    'H',
    1.00,
    NOW(),
    NOW(),
    0,
    'F07 演示要求-指标支撑关系'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_requirement_indicator_support
    WHERE graduation_requirement_id = @requirement_1_id
      AND indicator_point_id = @indicator_1_id
);

UPDATE tr_requirement_indicator_support
SET support_level = 'H',
    support_weight = 1.00,
    is_deleted = 0,
    remark = 'F07 演示要求-指标支撑关系',
    updated_at = NOW()
WHERE graduation_requirement_id = @requirement_1_id
  AND indicator_point_id = @indicator_1_id;

INSERT INTO tr_requirement_indicator_support (
    graduation_requirement_id, indicator_point_id, support_level, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @requirement_2_id,
    @indicator_2_id,
    'H',
    1.00,
    NOW(),
    NOW(),
    0,
    'F07 演示要求-指标支撑关系'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_requirement_indicator_support
    WHERE graduation_requirement_id = @requirement_2_id
      AND indicator_point_id = @indicator_2_id
);

UPDATE tr_requirement_indicator_support
SET support_level = 'H',
    support_weight = 1.00,
    is_deleted = 0,
    remark = 'F07 演示要求-指标支撑关系',
    updated_at = NOW()
WHERE graduation_requirement_id = @requirement_2_id
  AND indicator_point_id = @indicator_2_id;

INSERT INTO tr_program_course (
    program_version_id, course_id, semester_recommend, course_category, is_required, sort_no,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @program_version_main_id,
    @course_se101_id,
    '第1学期',
    '专业必修',
    1,
    10,
    NOW(),
    NOW(),
    0,
    'F08 演示方案课程'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_program_course
    WHERE program_version_id = @program_version_main_id
      AND course_id = @course_se101_id
);

UPDATE tr_program_course
SET semester_recommend = '第1学期',
    course_category = '专业必修',
    is_required = 1,
    sort_no = 10,
    is_deleted = 0,
    remark = 'F08 演示方案课程',
    updated_at = NOW()
WHERE program_version_id = @program_version_main_id
  AND course_id = @course_se101_id;

INSERT INTO tr_program_course (
    program_version_id, course_id, semester_recommend, course_category, is_required, sort_no,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @program_version_main_id,
    @course_se201_id,
    '第3学期',
    '专业选修',
    0,
    20,
    NOW(),
    NOW(),
    0,
    'F08 演示方案课程'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_program_course
    WHERE program_version_id = @program_version_main_id
      AND course_id = @course_se201_id
);

UPDATE tr_program_course
SET semester_recommend = '第3学期',
    course_category = '专业选修',
    is_required = 0,
    sort_no = 20,
    is_deleted = 0,
    remark = 'F08 演示方案课程',
    updated_at = NOW()
WHERE program_version_id = @program_version_main_id
  AND course_id = @course_se201_id;

INSERT INTO tr_course_requirement_support (
    program_version_id, course_id, graduation_requirement_id, support_level, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @program_version_main_id,
    @course_se101_id,
    @requirement_1_id,
    'H',
    1.00,
    NOW(),
    NOW(),
    0,
    'F08 演示课程支撑关系'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_course_requirement_support
    WHERE program_version_id = @program_version_main_id
      AND course_id = @course_se101_id
      AND graduation_requirement_id = @requirement_1_id
);

UPDATE tr_course_requirement_support
SET support_level = 'H',
    support_weight = 1.00,
    is_deleted = 0,
    remark = 'F08 演示课程支撑关系',
    updated_at = NOW()
WHERE program_version_id = @program_version_main_id
  AND course_id = @course_se101_id
  AND graduation_requirement_id = @requirement_1_id;

INSERT INTO tr_course_requirement_support (
    program_version_id, course_id, graduation_requirement_id, support_level, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @program_version_main_id,
    @course_se101_id,
    @requirement_2_id,
    'M',
    0.60,
    NOW(),
    NOW(),
    0,
    'F08 演示课程支撑关系'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_course_requirement_support
    WHERE program_version_id = @program_version_main_id
      AND course_id = @course_se101_id
      AND graduation_requirement_id = @requirement_2_id
);

UPDATE tr_course_requirement_support
SET support_level = 'M',
    support_weight = 0.60,
    is_deleted = 0,
    remark = 'F08 演示课程支撑关系',
    updated_at = NOW()
WHERE program_version_id = @program_version_main_id
  AND course_id = @course_se101_id
  AND graduation_requirement_id = @requirement_2_id;

INSERT INTO tr_course_requirement_support (
    program_version_id, course_id, graduation_requirement_id, support_level, support_weight,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @program_version_main_id,
    @course_se201_id,
    @requirement_2_id,
    'H',
    1.00,
    NOW(),
    NOW(),
    0,
    'F08 演示课程支撑关系'
WHERE NOT EXISTS (
    SELECT 1 FROM tr_course_requirement_support
    WHERE program_version_id = @program_version_main_id
      AND course_id = @course_se201_id
      AND graduation_requirement_id = @requirement_2_id
);

UPDATE tr_course_requirement_support
SET support_level = 'H',
    support_weight = 1.00,
    is_deleted = 0,
    remark = 'F08 演示课程支撑关系',
    updated_at = NOW()
WHERE program_version_id = @program_version_main_id
  AND course_id = @course_se201_id
  AND graduation_requirement_id = @requirement_2_id;

-- -----------------------------
-- 5) Course data (F09-F11)
-- -----------------------------
UPDATE edu_course_objective
SET course_id = @course_se101_id,
    objective_code = 'OBJ1',
    objective_name = '掌握基础语法与流程控制',
    objective_desc = '能够使用顺序、分支、循环等控制结构完成基础编程任务。',
    achievement_standard = '能够独立完成基础编程题并通过课堂实验检查。',
    sort_no = 10,
    enabled = 1,
    is_deleted = 0,
    remark = 'F10 演示课程目标'
WHERE objective_code = 'OBJ-124045'
  AND course_id = @course_se101_id
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM edu_course_objective WHERE course_id = @course_se101_id AND objective_code = 'OBJ1' LIMIT 1) t
  );

INSERT INTO edu_course_objective (
    course_id, objective_code, objective_name, objective_desc, achievement_standard, sort_no, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @course_se101_id,
    'OBJ1',
    '掌握基础语法与流程控制',
    '能够使用顺序、分支、循环等控制结构完成基础编程任务。',
    '能够独立完成基础编程题并通过课堂实验检查。',
    10,
    1,
    NOW(),
    NOW(),
    0,
    'F10 演示课程目标'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_objective
    WHERE course_id = @course_se101_id
      AND objective_code = 'OBJ1'
);

INSERT INTO edu_course_objective (
    course_id, objective_code, objective_name, objective_desc, achievement_standard, sort_no, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @course_se101_id,
    'OBJ2',
    '完成程序结构设计与调试',
    '能够根据题目要求完成函数拆分、调试定位和代码规范整理。',
    '能够提交结构清晰、可运行的实验代码并说明设计思路。',
    20,
    1,
    NOW(),
    NOW(),
    0,
    'F10 演示课程目标'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_objective
    WHERE course_id = @course_se101_id
      AND objective_code = 'OBJ2'
);

INSERT INTO edu_course_objective (
    course_id, objective_code, objective_name, objective_desc, achievement_standard, sort_no, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @course_se201_id,
    'OBJ1',
    '完成项目分析与成果汇报',
    '能够围绕小型软件项目完成需求分解、实现和结果汇报。',
    '能够提交项目报告并完成课堂展示。',
    10,
    1,
    NOW(),
    NOW(),
    0,
    'F10 演示课程目标'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_objective
    WHERE course_id = @course_se201_id
      AND objective_code = 'OBJ1'
);

SET @se101_obj1_id := (
    SELECT id FROM edu_course_objective
    WHERE course_id = @course_se101_id AND objective_code = 'OBJ1' AND is_deleted = 0
    LIMIT 1
);
SET @se101_obj2_id := (
    SELECT id FROM edu_course_objective
    WHERE course_id = @course_se101_id AND objective_code = 'OBJ2' AND is_deleted = 0
    LIMIT 1
);
SET @se201_obj1_id := (
    SELECT id FROM edu_course_objective
    WHERE course_id = @course_se201_id AND objective_code = 'OBJ1' AND is_deleted = 0
    LIMIT 1
);

INSERT INTO edu_course_objective_indicator_point (
    course_objective_id, indicator_point_id, support_weight, created_at, updated_at, is_deleted, remark
)
SELECT
    @se101_obj1_id,
    @indicator_1_id,
    1.00,
    NOW(),
    NOW(),
    0,
    'F10 演示目标映射'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_objective_indicator_point
    WHERE course_objective_id = @se101_obj1_id
      AND indicator_point_id = @indicator_1_id
);

UPDATE edu_course_objective_indicator_point
SET support_weight = 1.00,
    is_deleted = 0,
    remark = 'F10 演示目标映射',
    updated_at = NOW()
WHERE course_objective_id = @se101_obj1_id
  AND indicator_point_id = @indicator_1_id;

INSERT INTO edu_course_objective_indicator_point (
    course_objective_id, indicator_point_id, support_weight, created_at, updated_at, is_deleted, remark
)
SELECT
    @se101_obj2_id,
    @indicator_2_id,
    0.80,
    NOW(),
    NOW(),
    0,
    'F10 演示目标映射'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_objective_indicator_point
    WHERE course_objective_id = @se101_obj2_id
      AND indicator_point_id = @indicator_2_id
);

UPDATE edu_course_objective_indicator_point
SET support_weight = 0.80,
    is_deleted = 0,
    remark = 'F10 演示目标映射',
    updated_at = NOW()
WHERE course_objective_id = @se101_obj2_id
  AND indicator_point_id = @indicator_2_id;

INSERT INTO edu_course_objective_indicator_point (
    course_objective_id, indicator_point_id, support_weight, created_at, updated_at, is_deleted, remark
)
SELECT
    @se201_obj1_id,
    @indicator_2_id,
    1.00,
    NOW(),
    NOW(),
    0,
    'F10 演示目标映射'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_objective_indicator_point
    WHERE course_objective_id = @se201_obj1_id
      AND indicator_point_id = @indicator_2_id
);

UPDATE edu_course_objective_indicator_point
SET support_weight = 1.00,
    is_deleted = 0,
    remark = 'F10 演示目标映射',
    updated_at = NOW()
WHERE course_objective_id = @se201_obj1_id
  AND indicator_point_id = @indicator_2_id;

UPDATE edu_course_content
SET course_id = @course_se101_id,
    content_code = 'CNT1',
    content_title = '基础语法与流程控制',
    content_desc = '讲解变量、数据类型、分支与循环，并完成课堂练习。',
    hours = 8,
    sort_no = 10,
    enabled = 1,
    is_deleted = 0,
    remark = 'F11 演示教学内容'
WHERE content_code = 'CNT-124045'
  AND course_id = @course_se101_id
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM edu_course_content WHERE course_id = @course_se101_id AND content_code = 'CNT1' LIMIT 1) t
  );

INSERT INTO edu_course_content (
    course_id, content_code, content_title, content_desc, hours, sort_no, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @course_se101_id,
    'CNT1',
    '基础语法与流程控制',
    '讲解变量、数据类型、分支与循环，并完成课堂练习。',
    8,
    10,
    1,
    NOW(),
    NOW(),
    0,
    'F11 演示教学内容'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_content
    WHERE course_id = @course_se101_id
      AND content_code = 'CNT1'
);

INSERT INTO edu_course_content (
    course_id, content_code, content_title, content_desc, hours, sort_no, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @course_se101_id,
    'CNT2',
    '函数设计与程序调试',
    '安排函数拆分、调试技巧和实验代码整理。',
    10,
    20,
    1,
    NOW(),
    NOW(),
    0,
    'F11 演示教学内容'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_content
    WHERE course_id = @course_se101_id
      AND content_code = 'CNT2'
);

INSERT INTO edu_course_content (
    course_id, content_code, content_title, content_desc, hours, sort_no, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @course_se201_id,
    'CNT1',
    '项目需求分析与汇报',
    '围绕项目案例完成需求梳理、分工设计和成果说明。',
    12,
    10,
    1,
    NOW(),
    NOW(),
    0,
    'F11 演示教学内容'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_content
    WHERE course_id = @course_se201_id
      AND content_code = 'CNT1'
);

SET @se101_cnt1_id := (
    SELECT id FROM edu_course_content
    WHERE course_id = @course_se101_id AND content_code = 'CNT1' AND is_deleted = 0
    LIMIT 1
);
SET @se101_cnt2_id := (
    SELECT id FROM edu_course_content
    WHERE course_id = @course_se101_id AND content_code = 'CNT2' AND is_deleted = 0
    LIMIT 1
);
SET @se201_cnt1_id := (
    SELECT id FROM edu_course_content
    WHERE course_id = @course_se201_id AND content_code = 'CNT1' AND is_deleted = 0
    LIMIT 1
);

INSERT INTO edu_course_content_objective_rel (
    content_id, objective_id, support_strength, created_at, updated_at, is_deleted, remark
)
SELECT
    @se101_cnt1_id,
    @se101_obj1_id,
    'H',
    NOW(),
    NOW(),
    0,
    'F11 演示内容-目标关系'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_content_objective_rel
    WHERE content_id = @se101_cnt1_id
      AND objective_id = @se101_obj1_id
);

UPDATE edu_course_content_objective_rel
SET support_strength = 'H',
    is_deleted = 0,
    remark = 'F11 演示内容-目标关系',
    updated_at = NOW()
WHERE content_id = @se101_cnt1_id
  AND objective_id = @se101_obj1_id;

INSERT INTO edu_course_content_objective_rel (
    content_id, objective_id, support_strength, created_at, updated_at, is_deleted, remark
)
SELECT
    @se101_cnt2_id,
    @se101_obj2_id,
    'H',
    NOW(),
    NOW(),
    0,
    'F11 演示内容-目标关系'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_content_objective_rel
    WHERE content_id = @se101_cnt2_id
      AND objective_id = @se101_obj2_id
);

UPDATE edu_course_content_objective_rel
SET support_strength = 'H',
    is_deleted = 0,
    remark = 'F11 演示内容-目标关系',
    updated_at = NOW()
WHERE content_id = @se101_cnt2_id
  AND objective_id = @se101_obj2_id;

INSERT INTO edu_course_content_objective_rel (
    content_id, objective_id, support_strength, created_at, updated_at, is_deleted, remark
)
SELECT
    @se201_cnt1_id,
    @se201_obj1_id,
    'H',
    NOW(),
    NOW(),
    0,
    'F11 演示内容-目标关系'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_content_objective_rel
    WHERE content_id = @se201_cnt1_id
      AND objective_id = @se201_obj1_id
);

UPDATE edu_course_content_objective_rel
SET support_strength = 'H',
    is_deleted = 0,
    remark = 'F11 演示内容-目标关系',
    updated_at = NOW()
WHERE content_id = @se201_cnt1_id
  AND objective_id = @se201_obj1_id;

UPDATE edu_course_assessment_method
SET course_id = @course_se101_id,
    method_code = 'AM1',
    method_name = '平时作业',
    ratio_percent = 60.00,
    due_rule = '第8周提交平时作业',
    enabled = 1,
    is_deleted = 0,
    remark = 'F11/F14/F15 演示考核方式'
WHERE method_code = 'M1-124045'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM edu_course_assessment_method WHERE course_id = @course_se101_id AND method_code = 'AM1' LIMIT 1) t
  );

INSERT INTO edu_course_assessment_method (
    course_id, method_code, method_name, ratio_percent, due_rule, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @course_se101_id,
    'AM1',
    '平时作业',
    60.00,
    '第8周提交平时作业',
    1,
    NOW(),
    NOW(),
    0,
    'F11/F14/F15 演示考核方式'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_assessment_method
    WHERE course_id = @course_se101_id
      AND method_code = 'AM1'
);

UPDATE edu_course_assessment_method
SET course_id = @course_se101_id,
    method_code = 'AM2',
    method_name = '课程报告',
    ratio_percent = 40.00,
    due_rule = '第16周提交课程报告',
    enabled = 1,
    is_deleted = 0,
    remark = 'F11/F14/F15 演示考核方式'
WHERE method_code = 'M2-124045'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM edu_course_assessment_method WHERE course_id = @course_se101_id AND method_code = 'AM2' LIMIT 1) t
  );

INSERT INTO edu_course_assessment_method (
    course_id, method_code, method_name, ratio_percent, due_rule, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @course_se101_id,
    'AM2',
    '课程报告',
    40.00,
    '第16周提交课程报告',
    1,
    NOW(),
    NOW(),
    0,
    'F11/F14/F15 演示考核方式'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_assessment_method
    WHERE course_id = @course_se101_id
      AND method_code = 'AM2'
);

INSERT INTO edu_course_assessment_method (
    course_id, method_code, method_name, ratio_percent, due_rule, enabled,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @course_se201_id,
    'AM1',
    '项目实训',
    100.00,
    '学期末提交项目成果',
    1,
    NOW(),
    NOW(),
    0,
    'F11/F14/F15 演示考核方式'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_assessment_method
    WHERE course_id = @course_se201_id
      AND method_code = 'AM1'
);

SET @se101_am1_id := (
    SELECT id FROM edu_course_assessment_method
    WHERE course_id = @course_se101_id AND method_code = 'AM1' AND is_deleted = 0
    LIMIT 1
);
SET @se101_am2_id := (
    SELECT id FROM edu_course_assessment_method
    WHERE course_id = @course_se101_id AND method_code = 'AM2' AND is_deleted = 0
    LIMIT 1
);
SET @se201_am1_id := (
    SELECT id FROM edu_course_assessment_method
    WHERE course_id = @course_se201_id AND method_code = 'AM1' AND is_deleted = 0
    LIMIT 1
);

UPDATE edu_course_assessment_standard
SET method_id = @se101_am1_id,
    standard_name = '基础作业完成度',
    standard_desc = '按题目要求完成语法、流程控制和基本调试。',
    score_min = 0.00,
    score_max = 100.00,
    sort_no = 10,
    is_deleted = 0,
    remark = 'F11 演示评分标准'
WHERE method_id = @se101_am1_id
  AND sort_no = 10;

INSERT INTO edu_course_assessment_standard (
    method_id, standard_name, standard_desc, score_min, score_max, sort_no,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @se101_am1_id,
    '基础作业完成度',
    '按题目要求完成语法、流程控制和基本调试。',
    0.00,
    100.00,
    10,
    NOW(),
    NOW(),
    0,
    'F11 演示评分标准'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_assessment_standard
    WHERE method_id = @se101_am1_id
      AND standard_name = '基础作业完成度'
);

INSERT INTO edu_course_assessment_standard (
    method_id, standard_name, standard_desc, score_min, score_max, sort_no,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @se101_am2_id,
    '课程报告质量',
    '围绕课程主题完成结构完整、结论清晰的书面报告。',
    0.00,
    100.00,
    10,
    NOW(),
    NOW(),
    0,
    'F11 演示评分标准'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_assessment_standard
    WHERE method_id = @se101_am2_id
      AND standard_name = '课程报告质量'
);

INSERT INTO edu_course_assessment_standard (
    method_id, standard_name, standard_desc, score_min, score_max, sort_no,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @se201_am1_id,
    '项目成果展示',
    '完成项目实现、文档整理和课堂展示。',
    0.00,
    100.00,
    10,
    NOW(),
    NOW(),
    0,
    'F11 演示评分标准'
WHERE NOT EXISTS (
    SELECT 1 FROM edu_course_assessment_standard
    WHERE method_id = @se201_am1_id
      AND standard_name = '项目成果展示'
);

-- -----------------------------
-- 6) Teaching/resources/evidence/scores (F12-F15)
-- -----------------------------
UPDATE teaching_task
SET task_code = 'TT-SE101-2026FALL',
    semester_id = @semester_1_id,
    course_id = @course_se101_id,
    class_id = @class_id,
    teacher_id = @teacher_id,
    program_version_id = @program_version_main_id,
    task_status = 'PUBLISHED',
    total_hours = 48,
    schedule_desc = '程序设计基础：每周 4 学时，含课堂练习与阶段作业。',
    is_deleted = 0,
    remark = 'F12-F15 演示授课任务'
WHERE task_code = 'TASK-124045'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM teaching_task WHERE task_code = 'TT-SE101-2026FALL' LIMIT 1) t
  );

INSERT INTO teaching_task (
    task_code, semester_id, course_id, class_id, teacher_id, program_version_id, task_status,
    total_hours, schedule_desc, created_at, updated_at, is_deleted, remark
)
SELECT
    'TT-SE101-2026FALL',
    @semester_1_id,
    @course_se101_id,
    @class_id,
    @teacher_id,
    @program_version_main_id,
    'PUBLISHED',
    48,
    '程序设计基础：每周 4 学时，含课堂练习与阶段作业。',
    NOW(),
    NOW(),
    0,
    'F12-F15 演示授课任务'
WHERE NOT EXISTS (
    SELECT 1 FROM teaching_task WHERE task_code = 'TT-SE101-2026FALL'
);

INSERT INTO teaching_task (
    task_code, semester_id, course_id, class_id, teacher_id, program_version_id, task_status,
    total_hours, schedule_desc, created_at, updated_at, is_deleted, remark
)
SELECT
    'TT-SE201-2027SPR',
    @semester_2_id,
    @course_se201_id,
    @class_id,
    @teacher_id,
    @program_version_main_id,
    'DRAFT',
    32,
    '软件工程项目实践：项目分组、阶段汇报与成果展示。',
    NOW(),
    NOW(),
    0,
    'F12-F15 演示授课任务'
WHERE NOT EXISTS (
    SELECT 1 FROM teaching_task WHERE task_code = 'TT-SE201-2027SPR'
);

SET @task_se101_id := (
    SELECT id FROM teaching_task WHERE task_code = 'TT-SE101-2026FALL' AND is_deleted = 0 LIMIT 1
);
SET @task_se201_id := (
    SELECT id FROM teaching_task WHERE task_code = 'TT-SE201-2027SPR' AND is_deleted = 0 LIMIT 1
);

UPDATE course_resource
SET course_id = @course_se101_id,
    task_id = @task_se101_id,
    resource_type = 'SYLLABUS',
    resource_name = '程序设计基础课程大纲',
    file_id = @resource_file_id,
    resource_desc = '用于 F13 页面演示的课程大纲资源。',
    visible_scope_type = 'COURSE',
    visible_scope_id = NULL,
    publish_status = 1,
    is_deleted = 0,
    remark = 'F13 演示课程资源'
WHERE file_id = @resource_file_id;

INSERT INTO course_resource (
    course_id, task_id, resource_type, resource_name, file_id, resource_desc,
    visible_scope_type, visible_scope_id, publish_status,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @course_se101_id,
    @task_se101_id,
    'SYLLABUS',
    '程序设计基础课程大纲',
    @resource_file_id,
    '用于 F13 页面演示的课程大纲资源。',
    'COURSE',
    NULL,
    1,
    NOW(),
    NOW(),
    0,
    'F13 演示课程资源'
WHERE NOT EXISTS (
    SELECT 1 FROM course_resource WHERE file_id = @resource_file_id
);

SET @course_resource_id := (
    SELECT id FROM course_resource WHERE file_id = @resource_file_id AND is_deleted = 0 LIMIT 1
);

UPDATE course_evidence_material
SET task_id = @task_se101_id,
    method_id = @se101_am1_id,
    material_type = '平时作业',
    file_id = @evidence_file_01_id,
    source_student_id = @student_01_id,
    review_status = 'APPROVED',
    review_user_id = @teacher_user_id,
    review_comment = '材料完整，可用于成绩核验。',
    is_deleted = 0,
    remark = 'F14 演示证据材料'
WHERE file_id = @evidence_file_01_id;

INSERT INTO course_evidence_material (
    task_id, method_id, material_type, file_id, source_student_id, review_status, review_user_id, review_comment,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @task_se101_id,
    @se101_am1_id,
    '平时作业',
    @evidence_file_01_id,
    @student_01_id,
    'APPROVED',
    @teacher_user_id,
    '材料完整，可用于成绩核验。',
    NOW(),
    NOW(),
    0,
    'F14 演示证据材料'
WHERE NOT EXISTS (
    SELECT 1 FROM course_evidence_material WHERE file_id = @evidence_file_01_id
);

UPDATE course_evidence_material
SET task_id = @task_se101_id,
    method_id = @se101_am2_id,
    material_type = '课程报告',
    file_id = @evidence_file_02_id,
    source_student_id = @student_02_id,
    review_status = 'PENDING',
    review_user_id = NULL,
    review_comment = NULL,
    is_deleted = 0,
    remark = 'F14 演示证据材料'
WHERE file_id = @evidence_file_02_id;

INSERT INTO course_evidence_material (
    task_id, method_id, material_type, file_id, source_student_id, review_status, review_user_id, review_comment,
    created_at, updated_at, is_deleted, remark
)
SELECT
    @task_se101_id,
    @se101_am2_id,
    '课程报告',
    @evidence_file_02_id,
    @student_02_id,
    'PENDING',
    NULL,
    NULL,
    NOW(),
    NOW(),
    0,
    'F14 演示证据材料'
WHERE NOT EXISTS (
    SELECT 1 FROM course_evidence_material WHERE file_id = @evidence_file_02_id
);

SET @evidence_material_01_id := (
    SELECT id FROM course_evidence_material WHERE file_id = @evidence_file_01_id AND is_deleted = 0 LIMIT 1
);
SET @evidence_material_02_id := (
    SELECT id FROM course_evidence_material WHERE file_id = @evidence_file_02_id AND is_deleted = 0 LIMIT 1
);

UPDATE course_score_batch
SET batch_no = 'BATCH-SE101-AM1-2026',
    task_id = @task_se101_id,
    objective_id = @se101_obj1_id,
    method_id = @se101_am1_id,
    calc_status = 'DONE',
    locked_flag = 0,
    imported_at = NOW(),
    calculated_at = NOW(),
    is_deleted = 0,
    remark = 'F15 演示成绩批次'
WHERE batch_no = 'BATCH-124045'
  AND NOT EXISTS (
    SELECT 1 FROM (SELECT id FROM course_score_batch WHERE batch_no = 'BATCH-SE101-AM1-2026' LIMIT 1) t
  );

INSERT INTO course_score_batch (
    batch_no, task_id, objective_id, method_id, calc_status, locked_flag, imported_at, calculated_at,
    created_at, updated_at, is_deleted, remark
)
SELECT
    'BATCH-SE101-AM1-2026',
    @task_se101_id,
    @se101_obj1_id,
    @se101_am1_id,
    'DONE',
    0,
    NOW(),
    NOW(),
    NOW(),
    NOW(),
    0,
    'F15 演示成绩批次'
WHERE NOT EXISTS (
    SELECT 1 FROM course_score_batch WHERE batch_no = 'BATCH-SE101-AM1-2026'
);

SET @score_batch_id := (
    SELECT id FROM course_score_batch WHERE batch_no = 'BATCH-SE101-AM1-2026' AND is_deleted = 0 LIMIT 1
);

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type, source_ref_id,
    submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT
    @score_batch_id,
    @student_01_id,
    85.00,
    85.00,
    85.00,
    'MATERIAL',
    @evidence_material_01_id,
    'SUBMITTED',
    0,
    NOW(),
    NOW(),
    0,
    'F15 演示成绩明细'
WHERE NOT EXISTS (
    SELECT 1 FROM course_score_detail
    WHERE batch_id = @score_batch_id
      AND student_id = @student_01_id
);

UPDATE course_score_detail
SET raw_score = 85.00,
    weighted_score = 85.00,
    total_score = 85.00,
    source_type = 'MATERIAL',
    source_ref_id = @evidence_material_01_id,
    submit_status = 'SUBMITTED',
    locked_flag = 0,
    is_deleted = 0,
    remark = 'F15 演示成绩明细',
    updated_at = NOW()
WHERE batch_id = @score_batch_id
  AND student_id = @student_01_id;

INSERT INTO course_score_detail (
    batch_id, student_id, raw_score, weighted_score, total_score, source_type, source_ref_id,
    submit_status, locked_flag, created_at, updated_at, is_deleted, remark
)
SELECT
    @score_batch_id,
    @student_02_id,
    92.00,
    92.00,
    92.00,
    'MATERIAL',
    @evidence_material_02_id,
    'DRAFT',
    0,
    NOW(),
    NOW(),
    0,
    'F15 演示成绩明细'
WHERE NOT EXISTS (
    SELECT 1 FROM course_score_detail
    WHERE batch_id = @score_batch_id
      AND student_id = @student_02_id
);

UPDATE course_score_detail
SET raw_score = 92.00,
    weighted_score = 92.00,
    total_score = 92.00,
    source_type = 'MATERIAL',
    source_ref_id = @evidence_material_02_id,
    submit_status = 'DRAFT',
    locked_flag = 0,
    is_deleted = 0,
    remark = 'F15 演示成绩明细',
    updated_at = NOW()
WHERE batch_id = @score_batch_id
  AND student_id = @student_02_id;

-- -----------------------------
-- 7) Sync file business bindings
-- -----------------------------
UPDATE sys_file
SET biz_type = 'COURSE_RESOURCE',
    biz_id = @course_resource_id,
    updated_at = NOW()
WHERE id = @resource_file_id;

UPDATE sys_file
SET biz_type = 'EVIDENCE_MATERIAL',
    biz_id = @evidence_material_01_id,
    updated_at = NOW()
WHERE id = @evidence_file_01_id;

UPDATE sys_file
SET biz_type = 'EVIDENCE_MATERIAL',
    biz_id = @evidence_material_02_id,
    updated_at = NOW()
WHERE id = @evidence_file_02_id;
