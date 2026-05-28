package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoleAuthorityDetailVO {

    private String roleId;
    private String roleName;
    private String description;
    private List<PermissionRowVO> permissionRows;
}
