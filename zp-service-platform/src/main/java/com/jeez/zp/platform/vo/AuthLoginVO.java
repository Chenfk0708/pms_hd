package com.jeez.zp.platform.vo;

import cn.dev33.satoken.stp.StpUtil;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthLoginVO {

    String token;
    String tokenName;
    String loginMode;
    Long userId;
    Long memberId;
    Long campId;
    String roleCode;
    String roleName;

    public static AuthLoginVO from(CurrentUserBundleVO bundle, String tokenValue) {
        return AuthLoginVO.builder()
                .token(tokenValue)
                .tokenName(StpUtil.getTokenName())
                .loginMode("demo-direct")
                .userId(bundle.getUserId())
                .memberId(bundle.getMemberId())
                .campId(bundle.getCampId())
                .roleCode(bundle.getRoleCode())
                .roleName(bundle.getRoleName())
                .build();
    }
}
