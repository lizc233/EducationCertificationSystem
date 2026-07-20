package com.educationcertificationsystem.util;

import cn.hutool.crypto.SecureUtil;

public final class PasswordUtils {

    private PasswordUtils() {
    }

    public static String encode(String rawPassword) {
        return SecureUtil.sha256(rawPassword == null ? "" : rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }
}
