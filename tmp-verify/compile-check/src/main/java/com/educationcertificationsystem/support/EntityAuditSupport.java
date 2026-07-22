package com.educationcertificationsystem.support;

import cn.hutool.core.bean.BeanUtil;

import java.time.LocalDateTime;

public final class EntityAuditSupport {

    private EntityAuditSupport() {
    }

    public static void touchCreate(Object target) {
        LocalDateTime now = LocalDateTime.now();
        set(target, "createdAt", now);
        set(target, "updatedAt", now);
        set(target, "isDeleted", 0);
    }

    public static void touchUpdate(Object target) {
        set(target, "updatedAt", LocalDateTime.now());
    }

    public static void touchDelete(Object target) {
        set(target, "isDeleted", 1);
        set(target, "updatedAt", LocalDateTime.now());
    }

    public static void set(Object target, String fieldName, Object value) {
        if (target != null) {
            BeanUtil.setProperty(target, fieldName, value);
        }
    }
}
