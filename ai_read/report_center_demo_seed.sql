-- Report center demo seed
-- Rebuilds only the two demo report projects below.

USE education_certification_system;

SET NAMES utf8mb4;

SET @report_code_1 := 'RC-2026-001';
SET @report_code_2 := 'RC-2026-002';

DROP TEMPORARY TABLE IF EXISTS tmp_report_project_ids;
CREATE TEMPORARY TABLE tmp_report_project_ids (
    id BIGINT PRIMARY KEY
);

INSERT INTO tmp_report_project_ids (id)
SELECT id
FROM report_project
WHERE report_code IN (@report_code_1, @report_code_2);

DROP TEMPORARY TABLE IF EXISTS tmp_report_chapter_ids;
CREATE TEMPORARY TABLE tmp_report_chapter_ids (
    id BIGINT PRIMARY KEY
);

INSERT INTO tmp_report_chapter_ids (id)
SELECT id
FROM report_chapter
WHERE project_id IN (SELECT id FROM tmp_report_project_ids);

DELETE FROM report_draft
WHERE chapter_id IN (SELECT id FROM tmp_report_chapter_ids);

DELETE FROM report_progress_log
WHERE project_id IN (SELECT id FROM tmp_report_project_ids);

DELETE FROM report_task_assignment
WHERE project_id IN (SELECT id FROM tmp_report_project_ids);

DELETE FROM report_chapter
WHERE id IN (SELECT id FROM tmp_report_chapter_ids);

DELETE FROM report_project
WHERE id IN (SELECT id FROM tmp_report_project_ids);

SET @semester_id := (
    SELECT id
    FROM edu_semester
    WHERE is_deleted = 0
      AND semester_name = '2026 Fall Demo'
    ORDER BY id DESC
    LIMIT 1
);

SET @admin_user_id := (
    SELECT id
    FROM sys_user
    WHERE is_deleted = 0
      AND username = 'A2026001'
    LIMIT 1
);

SET @director_user_id := (
    SELECT id
    FROM sys_user
    WHERE is_deleted = 0
      AND username = 'A2026002'
    LIMIT 1
);

SET @teacher_li_id := (
    SELECT id
    FROM sys_user
    WHERE is_deleted = 0
      AND username = 'T2026001'
    LIMIT 1
);

SET @teacher_chen_id := (
    SELECT id
    FROM sys_user
    WHERE is_deleted = 0
      AND username = 'T2026002'
    LIMIT 1
);

SET @teacher_huang_id := (
    SELECT id
    FROM sys_user
    WHERE is_deleted = 0
      AND username = 'T2026003'
    LIMIT 1
);

SET @teacher_he_id := (
    SELECT id
    FROM sys_user
    WHERE is_deleted = 0
      AND username = 'T2026004'
    LIMIT 1
);

INSERT INTO report_project (
    report_code,
    project_name,
    academic_year,
    semester_id,
    owner_user_id,
    generation_mode,
    status,
    total_chapters,
    locked_flag,
    exported_at,
    created_at,
    updated_at,
    is_deleted,
    remark
)
VALUES
(
    @report_code_1,
    'Software Engineering Self Review 2026',
    '2026-2027',
    @semester_id,
    @admin_user_id,
    'MANUAL',
    'IN_PROGRESS',
    6,
    0,
    NULL,
    NOW(),
    NOW(),
    0,
    'Demo project for report center page'
),
(
    @report_code_2,
    'Accreditation Material Workbook 2026',
    '2026-2027',
    @semester_id,
    @director_user_id,
    'MANUAL',
    'COMPLETED',
    4,
    1,
    NOW(),
    NOW(),
    NOW(),
    0,
    'Demo project with completed status'
);

SET @project_1_id := (
    SELECT id
    FROM report_project
    WHERE report_code = @report_code_1
      AND is_deleted = 0
    LIMIT 1
);

SET @project_2_id := (
    SELECT id
    FROM report_project
    WHERE report_code = @report_code_2
      AND is_deleted = 0
    LIMIT 1
);

INSERT INTO report_chapter (
    project_id,
    parent_id,
    chapter_code,
    chapter_title,
    source_type,
    source_ref_id,
    content_text,
    chapter_status,
    sort_no,
    locked_flag,
    created_at,
    updated_at,
    is_deleted,
    remark
)
VALUES
(@project_1_id, NULL, '1', 'Program Overview', 'MANUAL', NULL, 'Program background and scale summary.', 'IN_PROGRESS', 1, 0, NOW(), NOW(), 0, 'Root chapter'),
(@project_1_id, NULL, '2', 'Objectives And Attainment', 'MANUAL', NULL, 'Objectives, outcomes and attainment analysis.', 'IN_PROGRESS', 2, 0, NOW(), NOW(), 0, 'Root chapter'),
(@project_1_id, NULL, '3', 'Continuous Improvement', 'MANUAL', NULL, 'Issue list and improvement loop.', 'TODO', 3, 0, NOW(), NOW(), 0, 'Root chapter'),
(@project_2_id, NULL, '1', 'Work Overview', 'MANUAL', NULL, 'Overall accreditation work overview.', 'COMPLETED', 1, 1, NOW(), NOW(), 0, 'Root chapter'),
(@project_2_id, NULL, '2', 'Supporting Evidence', 'MANUAL', NULL, 'Evidence catalog and archive rules.', 'COMPLETED', 2, 1, NOW(), NOW(), 0, 'Root chapter');

SET @p1_c1_id := (
    SELECT id FROM report_chapter
    WHERE project_id = @project_1_id AND chapter_code = '1' AND parent_id IS NULL
    LIMIT 1
);

SET @p1_c2_id := (
    SELECT id FROM report_chapter
    WHERE project_id = @project_1_id AND chapter_code = '2' AND parent_id IS NULL
    LIMIT 1
);

SET @p1_c3_id := (
    SELECT id FROM report_chapter
    WHERE project_id = @project_1_id AND chapter_code = '3' AND parent_id IS NULL
    LIMIT 1
);

SET @p2_c1_id := (
    SELECT id FROM report_chapter
    WHERE project_id = @project_2_id AND chapter_code = '1' AND parent_id IS NULL
    LIMIT 1
);

SET @p2_c2_id := (
    SELECT id FROM report_chapter
    WHERE project_id = @project_2_id AND chapter_code = '2' AND parent_id IS NULL
    LIMIT 1
);

INSERT INTO report_chapter (
    project_id,
    parent_id,
    chapter_code,
    chapter_title,
    source_type,
    source_ref_id,
    content_text,
    chapter_status,
    sort_no,
    locked_flag,
    created_at,
    updated_at,
    is_deleted,
    remark
)
VALUES
(@project_1_id, @p1_c1_id, '1.1', 'Program Positioning', 'MANUAL', NULL, 'Draft text already prepared.', 'COMPLETED', 11, 0, NOW(), NOW(), 0, 'Leaf chapter'),
(@project_1_id, @p1_c1_id, '1.2', 'Faculty And Resources', 'MANUAL', NULL, 'Data collection is in progress.', 'IN_PROGRESS', 12, 0, NOW(), NOW(), 0, 'Leaf chapter'),
(@project_1_id, @p1_c2_id, '2.1', 'Course Target Analysis', 'MANUAL', NULL, 'Attainment analysis draft prepared.', 'IN_PROGRESS', 21, 0, NOW(), NOW(), 0, 'Leaf chapter'),
(@project_1_id, @p1_c3_id, '3.1', 'Issue List And Plan', 'MANUAL', NULL, 'Waiting for action plan summary.', 'TODO', 31, 0, NOW(), NOW(), 0, 'Leaf chapter'),
(@project_2_id, @p2_c1_id, '1.1', 'Task Breakdown Review', 'MANUAL', NULL, 'Reviewed and finalized.', 'COMPLETED', 11, 1, NOW(), NOW(), 0, 'Leaf chapter'),
(@project_2_id, @p2_c2_id, '2.1', 'Archive Rules', 'MANUAL', NULL, 'Archived and finalized.', 'COMPLETED', 21, 1, NOW(), NOW(), 0, 'Leaf chapter');

SET @p1_c11_id := (
    SELECT id FROM report_chapter
    WHERE project_id = @project_1_id AND chapter_code = '1.1'
    LIMIT 1
);

SET @p1_c12_id := (
    SELECT id FROM report_chapter
    WHERE project_id = @project_1_id AND chapter_code = '1.2'
    LIMIT 1
);

SET @p1_c21_id := (
    SELECT id FROM report_chapter
    WHERE project_id = @project_1_id AND chapter_code = '2.1'
    LIMIT 1
);

SET @p1_c31_id := (
    SELECT id FROM report_chapter
    WHERE project_id = @project_1_id AND chapter_code = '3.1'
    LIMIT 1
);

SET @p2_c11_id := (
    SELECT id FROM report_chapter
    WHERE project_id = @project_2_id AND chapter_code = '1.1'
    LIMIT 1
);

SET @p2_c21_id := (
    SELECT id FROM report_chapter
    WHERE project_id = @project_2_id AND chapter_code = '2.1'
    LIMIT 1
);

INSERT INTO report_task_assignment (
    project_id,
    chapter_id,
    assignee_user_id,
    role_type,
    due_date,
    assignment_status,
    completed_at,
    created_at,
    updated_at,
    is_deleted,
    remark
)
VALUES
(@project_1_id, @p1_c11_id, @teacher_li_id, 'Writer-A', '2026-09-15', 'COMPLETED', '2026-07-18 10:00:00', NOW(), NOW(), 0, 'Assigned to teacher Li'),
(@project_1_id, @p1_c12_id, @teacher_chen_id, 'Writer-B', '2026-09-20', 'IN_PROGRESS', NULL, NOW(), NOW(), 0, 'Assigned to teacher Chen'),
(@project_1_id, @p1_c21_id, @teacher_huang_id, 'Writer-C', '2026-09-25', 'IN_PROGRESS', NULL, NOW(), NOW(), 0, 'Assigned to teacher Huang'),
(@project_1_id, @p1_c31_id, @teacher_he_id, 'Writer-D', '2026-10-10', 'PENDING', NULL, NOW(), NOW(), 0, 'Assigned to teacher He'),
(@project_2_id, @p2_c11_id, @teacher_li_id, 'Editor-A', '2026-07-10', 'COMPLETED', '2026-07-09 16:30:00', NOW(), NOW(), 0, 'Completed'),
(@project_2_id, @p2_c21_id, @teacher_chen_id, 'Editor-B', '2026-07-12', 'COMPLETED', '2026-07-11 14:00:00', NOW(), NOW(), 0, 'Completed');

INSERT INTO report_progress_log (
    project_id,
    chapter_id,
    user_id,
    progress_percent,
    comment,
    created_at,
    updated_at,
    is_deleted,
    remark
)
VALUES
(@project_1_id, @p1_c11_id, @teacher_li_id, 100.00, 'Program positioning chapter finished.', '2026-07-18 10:10:00', '2026-07-18 10:10:00', 0, 'Demo log'),
(@project_1_id, @p1_c12_id, @teacher_chen_id, 65.00, 'Faculty data collected, trend data pending.', '2026-07-20 14:20:00', '2026-07-20 14:20:00', 0, 'Demo log'),
(@project_1_id, @p1_c21_id, @teacher_huang_id, 80.00, 'Target analysis draft completed.', '2026-07-21 09:30:00', '2026-07-21 09:30:00', 0, 'Demo log'),
(@project_2_id, @p2_c11_id, @teacher_li_id, 100.00, 'Work overview finalized.', '2026-07-09 16:40:00', '2026-07-09 16:40:00', 0, 'Demo log'),
(@project_2_id, @p2_c21_id, @teacher_chen_id, 100.00, 'Archive rules finalized.', '2026-07-11 14:10:00', '2026-07-11 14:10:00', 0, 'Demo log');

INSERT INTO report_draft (
    chapter_id,
    version_no,
    draft_content,
    edited_by,
    edited_at,
    lock_flag,
    created_at,
    updated_at,
    is_deleted,
    remark
)
VALUES
(@p1_c11_id, 1, 'Draft for program positioning chapter.', @teacher_li_id, '2026-07-18 09:50:00', 0, NOW(), NOW(), 0, 'Demo draft'),
(@p1_c21_id, 1, 'Draft for course target analysis chapter.', @teacher_huang_id, '2026-07-21 09:00:00', 0, NOW(), NOW(), 0, 'Demo draft'),
(@p2_c11_id, 1, 'Final draft for work overview chapter.', @teacher_li_id, '2026-07-09 16:20:00', 1, NOW(), NOW(), 0, 'Demo draft');

SELECT
    rp.id,
    rp.report_code,
    rp.project_name,
    rp.status,
    rp.total_chapters,
    rp.owner_user_id
FROM report_project rp
WHERE rp.report_code IN (@report_code_1, @report_code_2)
ORDER BY rp.id;
