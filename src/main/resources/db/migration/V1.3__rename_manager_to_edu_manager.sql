SET @old_table_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'manager'
);

SET @new_table_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'edu_manager'
);

SET @rename_table_sql = IF(
    @old_table_exists = 1 AND @new_table_exists = 0,
    'RENAME TABLE manager TO edu_manager',
    'SELECT 1'
);
PREPARE rename_table_stmt FROM @rename_table_sql;
EXECUTE rename_table_stmt;
DEALLOCATE PREPARE rename_table_stmt;

SET @rename_user_idx_sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'edu_manager'
          AND INDEX_NAME = 'uk_manager_user_id'
    ),
    'ALTER TABLE edu_manager RENAME INDEX uk_manager_user_id TO uk_edu_manager_user_id',
    'SELECT 1'
);
PREPARE rename_user_idx_stmt FROM @rename_user_idx_sql;
EXECUTE rename_user_idx_stmt;
DEALLOCATE PREPARE rename_user_idx_stmt;

SET @rename_no_idx_sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'edu_manager'
          AND INDEX_NAME = 'uk_manager_no'
    ),
    'ALTER TABLE edu_manager RENAME INDEX uk_manager_no TO uk_edu_manager_no',
    'SELECT 1'
);
PREPARE rename_no_idx_stmt FROM @rename_no_idx_sql;
EXECUTE rename_no_idx_stmt;
DEALLOCATE PREPARE rename_no_idx_stmt;

SET @rename_status_idx_sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'edu_manager'
          AND INDEX_NAME = 'idx_manager_status'
    ),
    'ALTER TABLE edu_manager RENAME INDEX idx_manager_status TO idx_edu_manager_status',
    'SELECT 1'
);
PREPARE rename_status_idx_stmt FROM @rename_status_idx_sql;
EXECUTE rename_status_idx_stmt;
DEALLOCATE PREPARE rename_status_idx_stmt;

SET @rename_fk_sql = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.TABLE_CONSTRAINTS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'edu_manager'
          AND CONSTRAINT_NAME = 'fk_manager_user'
          AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    ),
    'ALTER TABLE edu_manager DROP FOREIGN KEY fk_manager_user, ADD CONSTRAINT fk_edu_manager_user FOREIGN KEY (user_id) REFERENCES sys_user (id)',
    'SELECT 1'
);
PREPARE rename_fk_stmt FROM @rename_fk_sql;
EXECUTE rename_fk_stmt;
DEALLOCATE PREPARE rename_fk_stmt;
