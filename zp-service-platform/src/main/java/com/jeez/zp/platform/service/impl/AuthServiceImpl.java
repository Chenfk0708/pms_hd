package com.jeez.zp.platform.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.jeez.zp.platform.dto.auth.LoginRequest;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.AuthService;
import com.jeez.zp.platform.vo.AuthLoginVO;
import com.jeez.zp.platform.vo.AuthMeVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEMO_LOGIN_PASSWORD = "demo-login";

    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public AuthLoginVO login(LoginRequest request) {
        if (request == null || (!StringUtils.hasText(request.getMobile()) && !StringUtils.hasText(request.getEmail()))) {
            throw new BusinessException(40003, "手机号或邮箱不能为空");
        }
        if (!DEMO_LOGIN_PASSWORD.equals(request.getPassword())) {
            throw new BusinessException(40002, "演示环境仅支持 demo-login 密码");
        }

        CurrentUserBundleVO bundle = platformBootstrapMapper.selectUserByMobileOrEmail(request.getMobile(), request.getEmail());
        if (bundle == null) {
            throw new BusinessException(40001, "账号不存在");
        }

        StpUtil.login(bundle.getUserId());
        return AuthLoginVO.from(bundle, StpUtil.getTokenValue());
    }

    @Override
    public AuthMeVO getCurrentUser(Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "用户上下文不存在");
        }

        List<String> permissionCodes = platformBootstrapMapper.selectAuthorityCodesByRoleId(bundle.getRoleId());
        return AuthMeVO.from(bundle, permissionCodes);
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }
}
