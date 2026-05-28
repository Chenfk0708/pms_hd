package com.jeez.zp.platform.service;

import com.jeez.zp.platform.dto.auth.LoginRequest;
import com.jeez.zp.platform.vo.AuthLoginVO;
import com.jeez.zp.platform.vo.AuthMeVO;

public interface AuthService {

    AuthLoginVO login(LoginRequest request);

    AuthMeVO getCurrentUser(Long userId);

    void logout();
}
