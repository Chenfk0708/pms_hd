package com.jeez.zp.platform.service;

import com.jeez.zp.platform.dto.auth.AccountUpdateRequest;
import com.jeez.zp.platform.dto.auth.LoginRequest;
import com.jeez.zp.platform.dto.auth.RegisterRequest;
import com.jeez.zp.platform.vo.AuthLoginVO;
import com.jeez.zp.platform.vo.AuthMeVO;
import com.jeez.zp.platform.vo.RegisterOptionsVO;

public interface AuthService {

    AuthLoginVO login(LoginRequest request);

    AuthLoginVO register(RegisterRequest request);

    RegisterOptionsVO getRegisterOptions(Long campId);

    AuthMeVO getCurrentUser(Long userId);

    AuthMeVO updateCurrentAccount(Long userId, AccountUpdateRequest request);

    void logout();
}
