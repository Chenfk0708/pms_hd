package com.jeez.zp.platform.security;

import com.jeez.zp.platform.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class GatewayTrustInterceptor implements HandlerInterceptor {

    private static final String AUTH_LOGIN_PATH = "/auth/login";
    private static final String AUTH_REGISTER_PATH = "/auth/register";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String VERIFIED_HEADER = "X-Auth-Verified";
    private static final String UNAUTHORIZED_MESSAGE = "未通过网关认证";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(AUTH_LOGIN_PATH) || requestUri.startsWith(AUTH_REGISTER_PATH)) {
            return true;
        }

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
