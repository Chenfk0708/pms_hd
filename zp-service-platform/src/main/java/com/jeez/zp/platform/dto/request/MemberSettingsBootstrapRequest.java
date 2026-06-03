package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class MemberSettingsBootstrapRequest {

    private String campId;
    private String keyword;
    private String roleName;
    private Integer page;
    private Integer pageSize;
    private String routeMode;
    private String editUserId;
}
