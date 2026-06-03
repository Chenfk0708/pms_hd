package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SystemConfigMutationRequest {

    private String campId;
    private String configKey;
    private String configValue;
    private Integer isCheckInGuideIdentityRegCompleted;
    private Integer isCheckInGuideVerifyPayDeposit;
    private Integer isWifiDisplayEnabled;
    private Integer isNightAudit;
    private Integer autoNightAuditTime;
    private Integer orderAmortizeStrategy;
    private List<Integer> vendibleTypes;
}
