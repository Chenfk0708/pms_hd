package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.auth.AccountUpdateRequest;
import com.jeez.zp.platform.dto.auth.LoginRequest;
import com.jeez.zp.platform.dto.auth.RegisterRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.AuthService;
import com.jeez.zp.platform.vo.AuthLoginVO;
import com.jeez.zp.platform.vo.AuthMeVO;
import com.jeez.zp.platform.vo.RegisterOptionsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PostMapping("/register")
    public HudsonResponse<AuthLoginVO> register(@RequestBody RegisterRequest request) {
        return HudsonResponse.success(authService.register(request), TraceIdFactory.next("auth-register"));
    }

    @GetMapping("/register/options")
    public HudsonResponse<RegisterOptionsVO> registerOptions(@RequestParam(value = "campId", required = false) Long campId) {
        return HudsonResponse.success(authService.getRegisterOptions(campId), TraceIdFactory.next("auth-register-options"));
    }

    @GetMapping("/me")
    public HudsonResponse<AuthMeVO> me() {
        return HudsonResponse.success(authService.getCurrentUser(LoginUserContext.requiredUserId()), TraceIdFactory.next("auth-me"));
    }

    @PostMapping("/account")
    public HudsonResponse<AuthMeVO> updateAccount(@RequestBody AccountUpdateRequest request) {
        return HudsonResponse.success(
                authService.updateCurrentAccount(LoginUserContext.requiredUserId(), request),
                TraceIdFactory.next("auth-account")
        );
    }

    @PostMapping("/logout")
    public HudsonResponse<Void> logout() {
        authService.logout();
        return HudsonResponse.success(null, TraceIdFactory.next("auth-logout"));
    }
}
