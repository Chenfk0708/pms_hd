package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class SystemConfigMutationRequest {

    private String campId;
    private String configKey;
    private String configValue;
}
