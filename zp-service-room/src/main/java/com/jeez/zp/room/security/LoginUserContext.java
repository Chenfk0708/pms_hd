package com.jeez.zp.room.security;

import com.jeez.zp.room.exception.BusinessException;

public final class LoginUserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    private LoginUserContext() {
    }

    public static void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    public static Long requiredUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(401, "未通过网关认证");
        }
        return userId;
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}
