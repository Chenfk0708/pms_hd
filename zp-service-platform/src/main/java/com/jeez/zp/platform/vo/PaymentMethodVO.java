package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class PaymentMethodVO {

    private String id;
    private String code;
    private String name;
    private String status;
    private Boolean isSystemDefault;
    private Boolean isPreferred;
    private String description;
    private List<String> availableScopes;
    private String settlementAccount;
    private String lastUsedAt;
    private String updatedAt;
    private String remark;
    private String usageCountLabel;
}
