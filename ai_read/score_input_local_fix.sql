-- Local dev data fix for score-input / achievement-course demo flow
-- Problem:
-- teaching_task.id = 1 points to class_id = 4, but class 4 currently has no students.
-- Result:
-- score workspace initialization cannot create course_score_detail rows.

USE education_certification_system;

UPDATE teaching_task
SET class_id = 1,
    updated_at = NOW(),
    remark = CONCAT(IFNULL(remark, ''), CASE WHEN IFNULL(remark, '') = '' THEN '' ELSE ' | ' END, 'local score-input demo fix')
WHERE id = 1
  AND (is_deleted = 0 OR is_deleted IS NULL);

SELECT id, task_code, course_id, class_id, teacher_id, program_version_id, task_status
FROM teaching_task
WHERE id = 1;

SELECT id, student_no, class_id
FROM edu_student
WHERE class_id = 1
  AND (is_deleted = 0 OR is_deleted IS NULL)
ORDER BY student_no, id;
