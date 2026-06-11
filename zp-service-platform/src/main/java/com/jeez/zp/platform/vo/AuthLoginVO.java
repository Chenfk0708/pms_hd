package com.jeez.zp.platform.vo;

import cn.dev33.satoken.stp.StpUtil;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AuthLoginVO {

    String token;
    String tokenName;
    String loginMode;
    Long userId;
    String username;
    String mobile;
    String email;
    String nickName;
    String avatarUrl;
    String wechat;
    Boolean passwordSet;
    Long memberId;
    Long campId;
    String campName;
    String roleCode;
    String roleName;
    List<String> permissionCodes;

    public static AuthLoginVO from(CurrentUserBundleVO bundle, String tokenValue, List<String> permissionCodes) {
        return AuthLoginVO.builder()
                .token(tokenValue)
                .tokenName(StpUtil.getTokenName())
                .loginMode("demo-direct")
                .userId(bundle.getUserId())
                .username(bundle.getUsername())
                .mobile(bundle.getMobile())
                .email(bundle.getEmail())
                .nickName(bundle.getNickName())
                .avatarUrl(bundle.getAvatarUrl())
                .wechat(bundle.getWechat())
                .passwordSet(bundle.getPasswordHash() != null && !bundle.getPasswordHash().isBlank())
                .memberId(bundle.getMemberId())
                .campId(bundle.getCampId())
                .campName(bundle.getCampName())
                .roleCode(bundle.getRoleCode())
                .roleName(bundle.getRoleName())
                .permissionCodes(permissionCodes)
                .build();
    }
}
