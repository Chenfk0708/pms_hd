package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class RoleSummaryVO {

    private Long roleId;
    private String roleName;
    private String description;
    private Integer memberCount;
    private Boolean canDelete;
    private String updatedAt;
}
