package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class RoleCampCreateRequest {

    private String campId;
    private String roleName;
    private String description;
}
