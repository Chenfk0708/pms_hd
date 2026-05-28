package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class RolePermissionGrantVO {

    private String moduleId;
    private String moduleName;
    private String permissionLabel;
    private Integer seqNo;
}
