package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class RolePermissionOptionVO {

    private Long authorityId;
    private String authorityType;
    private String moduleId;
    private String moduleName;
    private String permissionLabel;
    private Integer seqNo;
}
