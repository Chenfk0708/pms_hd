package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.auth.LoginRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.AuthService;
import com.jeez.zp.platform.vo.AuthLoginVO;
import com.jeez.zp.platform.vo.AuthMeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public HudsonResponse<AuthLoginVO> login(@RequestBody LoginRequest request) {
        return HudsonResponse.success(authService.login(request), TraceIdFactory.next("auth-login"));
    }

    @GetMapping("/me")
    public HudsonResponse<AuthMeVO> me() {
        return HudsonResponse.success(authService.getCurrentUser(LoginUserContext.requiredUserId()), TraceIdFactory.next("auth-me"));
    }

    @PostMapping("/logout")
    public HudsonResponse<Void> logout() {
        authService.logout();
        return HudsonResponse.success(null, TraceIdFactory.next("auth-logout"));
    }
}
