CREATE TABLE IF NOT EXISTS edu_manager (
                                          id BIGINT NOT NULL AUTO_INCREMENT,
                                          user_id BIGINT NOT NULL,
                                          admin_no VARCHAR(50) NOT NULL,
                                          department_name VARCHAR(100) DEFAULT NULL,
                                          position_name VARCHAR(50) DEFAULT NULL,
                                          status TINYINT NOT NULL DEFAULT 1,
                                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          is_deleted TINYINT NOT NULL DEFAULT 0,
                                          remark VARCHAR(500) DEFAULT NULL,
                                          PRIMARY KEY (id),
                                          UNIQUE KEY uk_edu_manager_user_id (user_id),
                                          UNIQUE KEY uk_edu_manager_no (admin_no),
                                          KEY idx_edu_manager_status (status),
                                          CONSTRAINT fk_edu_manager_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Administrator profile table';
