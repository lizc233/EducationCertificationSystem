package com.educationcertificationsystem.util;

import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.SecureUtil;

public final class TokenUtils {

    private TokenUtils() {
    }

    public static String generateToken() {
        return UUID.fastUUID().toString(true);
    }

    public static String hashToken(String token) {
        return SecureUtil.sha256(token == null ? "" : token);
    }
}
