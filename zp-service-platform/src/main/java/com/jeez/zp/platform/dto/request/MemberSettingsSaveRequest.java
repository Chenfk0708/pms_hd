package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class MemberSettingsSaveRequest {

    private String campId;
    private String routeMode;
    private MemberSettingDraftRequest draft;
}
