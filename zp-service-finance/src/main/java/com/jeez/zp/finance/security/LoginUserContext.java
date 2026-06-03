package com.jeez.zp.finance.security;

import com.jeez.zp.finance.exception.BusinessException;

public final class LoginUserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    private LoginUserContext() {
    }

    public static void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static Long requiredUserId() {
        Long userId = CURRENT_USER_ID.get();
        if (userId == null) {
            throw new BusinessException(401, "\u672A\u901A\u8FC7\u7F51\u5173\u8BA4\u8BC1");
        }
        return userId;
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}
