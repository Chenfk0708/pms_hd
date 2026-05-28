package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class CurrentUserBundleVO {

    private Long userId;
    private String mobile;
    private String email;
    private String nickName;
    private Long memberId;
    private String memberName;
    private Long campId;
    private Long roleId;
    private String roleCode;
    private String roleName;
    private String campName;
    private Long poiId;
    private String poiName;
}
