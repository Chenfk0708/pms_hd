package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class PaymentSettingVO {
    private String settingId;
    private String campId;
    private Object nightAudit;
    private Object amortize;
    private Object vendible;
    private String updatedAt;
}
