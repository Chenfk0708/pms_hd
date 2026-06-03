package com.jeez.zp.crm.security;

import com.jeez.zp.crm.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class GatewayTrustInterceptor implements HandlerInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String VERIFIED_HEADER = "X-Auth-Verified";
    private static final String UNAUTHORIZED_MESSAGE = "\u672A\u901A\u8FC7\u7F51\u5173\u8BA4\u8BC1";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(USER_ID_HEADER);
        String verified = request.getHeader(VERIFIED_HEADER);
        if (!"true".equals(verified) || userId == null || userId.isBlank()) {
            throw new BusinessException(401, UNAUTHORIZED_MESSAGE);
        }
        try {
            LoginUserContext.setCurrentUserId(Long.valueOf(userId));
        } catch (NumberFormatException ex) {
            throw new BusinessException(401, UNAUTHORIZED_MESSAGE);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserContext.clear();
    }
}
