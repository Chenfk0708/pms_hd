package com.jeez.zp.platform.vo;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AuthMeVO {

    Long userId;
    String mobile;
    String email;
    String nickName;
    Long memberId;
    String memberName;
    Long campId;
    String campName;
    Long poiId;
    String poiName;
    Long roleId;
    String roleCode;
    String roleName;
    List<String> permissionCodes;

    public static AuthMeVO from(CurrentUserBundleVO bundle, List<String> permissionCodes) {
        return AuthMeVO.builder()
                .userId(bundle.getUserId())
                .mobile(bundle.getMobile())
                .email(bundle.getEmail())
                .nickName(bundle.getNickName())
                .memberId(bundle.getMemberId())
                .memberName(bundle.getMemberName())
                .campId(bundle.getCampId())
                .campName(bundle.getCampName())
                .poiId(bundle.getPoiId())
                .poiName(bundle.getPoiName())
                .roleId(bundle.getRoleId())
                .roleCode(bundle.getRoleCode())
                .roleName(bundle.getRoleName())
                .permissionCodes(permissionCodes)
                .build();
    }
}
