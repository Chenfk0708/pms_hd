package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserOwnVO {

    private Long userId;
    private Long memberId;
    private Long campId;
    private Long poiId;
    private Long roleId;
    private String roleCode;
    private String roleName;
    private Map<String, Object> user;
    private Map<String, Object> member;
    private Map<String, Object> camp;
    private Map<String, Object> poi;
    private Map<String, Object> role;
    private List<String> permissionCodes;
}
