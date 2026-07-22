-- Fix graduation result detail insert failure
-- The original column length is too short for GRADUATION_REQUIREMENT.

ALTER TABLE eval_result_detail
    MODIFY result_type VARCHAR(50) NOT NULL;
