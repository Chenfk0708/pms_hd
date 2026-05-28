package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class RoleCampUpdateRequest {

    private String campId;
    private String roleId;
    private String roleName;
    private String description;
}
