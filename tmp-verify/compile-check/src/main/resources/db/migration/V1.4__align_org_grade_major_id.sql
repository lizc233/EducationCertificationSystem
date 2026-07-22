ALTER TABLE org_grade
    ADD COLUMN major_id BIGINT NULL AFTER id;

UPDATE org_grade g
LEFT JOIN (
    SELECT oc.grade_id, MIN(oc.major_id) AS major_id
    FROM org_class oc
    WHERE oc.grade_id IS NOT NULL
      AND oc.major_id IS NOT NULL
      AND (oc.is_deleted = 0 OR oc.is_deleted IS NULL)
    GROUP BY oc.grade_id
) cls ON cls.grade_id = g.id
LEFT JOIN (
    SELECT pag.grade_id, MIN(pv.major_id) AS major_id
    FROM tr_program_apply_grade pag
    INNER JOIN tr_program_version pv ON pv.id = pag.program_version_id
    WHERE pag.grade_id IS NOT NULL
      AND pv.major_id IS NOT NULL
      AND (pag.is_deleted = 0 OR pag.is_deleted IS NULL)
      AND (pv.is_deleted = 0 OR pv.is_deleted IS NULL)
    GROUP BY pag.grade_id
) prog ON prog.grade_id = g.id
SET g.major_id = COALESCE(cls.major_id, prog.major_id)
WHERE g.major_id IS NULL;

ALTER TABLE org_grade
    DROP INDEX uk_org_grade_year,
    ADD KEY idx_org_grade_major_id (major_id),
    ADD UNIQUE KEY uk_org_grade_major_year (major_id, grade_year),
    ADD CONSTRAINT fk_org_grade_major FOREIGN KEY (major_id) REFERENCES org_major (id);
