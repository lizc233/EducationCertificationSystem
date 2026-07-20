package com.educationcertificationsystem.auth;

import com.educationcertificationsystem.common.BusinessException;

public final class CurrentUserContext {

    private static final ThreadLocal<CurrentUserInfo> CONTEXT = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(CurrentUserInfo currentUser) {
        CONTEXT.set(currentUser);
    }

    public static CurrentUserInfo get() {
        return CONTEXT.get();
    }

    public static CurrentUserInfo require() {
        CurrentUserInfo currentUser = get();
        if (currentUser == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return currentUser;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
