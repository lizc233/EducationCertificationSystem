package com.educationcertificationsystem.config;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
public class DevDataInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DevDataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initialize() {
        if (!isCoreDataEmpty()) {
            log.info("Skip dev seed initialization because core tables already contain data");
            return;
        }

        seedRoles();
        seedUsers();
        seedUserRoles();
        log.info("Dev auth seed data initialized");
    }

    private boolean isCoreDataEmpty() {
        return count("sys_user") == 0
            && count("sys_role") == 0
            && count("sys_user_role") == 0;
    }

    private long count(String tableName) {
        Long value = jdbcTemplate.queryForObject("select count(*) from " + tableName, Long.class);
        return value == null ? 0L : value;
    }

    private void seedRoles() {
        jdbcTemplate.batchUpdate(
            "insert into sys_role (id, role_code, role_name, role_type, data_scope, sort_no, status, is_deleted, remark) "
                + "values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            List.of(
                new Object[]{1L, "ROLE_SUPER_ADMIN", "Administrator", "SYSTEM", "ALL", 1, 1, 0, "System administrator"},
                new Object[]{2L, "ROLE_TEACHER", "Teacher", "SYSTEM", "COLLEGE", 2, 1, 0, "Academic staff"},
                new Object[]{3L, "ROLE_STUDENT", "Student", "SYSTEM", "SELF", 3, 1, 0, "Student account"}
            )
        );
    }

    private void seedUsers() {
        jdbcTemplate.batchUpdate(
            "insert into sys_user (id, username, password_hash, real_name, phone, email, user_status, is_deleted, remark) "
                + "values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
            List.of(
                new Object[]{1L, "A2026001", PASSWORD_123456_HASH, "Admin Zhang", "13800000001", "admin@school.edu", 1, 0, "Academic Affairs Office"},
                new Object[]{2L, "T2026001", PASSWORD_123456_HASH, "Teacher Li", "13800000002", "teacher@school.edu", 1, 0, "School of Computer Science"},
                new Object[]{3L, "S2026001", PASSWORD_123456_HASH, "Student Wang", "13800000003", "student@school.edu", 1, 0, "CS 2501"}
            )
        );
    }

    private void seedUserRoles() {
        jdbcTemplate.batchUpdate(
            "insert into sys_user_role (id, user_id, role_id, is_deleted) values (?, ?, ?, ?)",
            List.of(
                new Object[]{1L, 1L, 1L, 0},
                new Object[]{2L, 2L, 2L, 0},
                new Object[]{3L, 3L, 3L, 0}
            )
        );
    }

    private static final String PASSWORD_123456_HASH = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";
}
