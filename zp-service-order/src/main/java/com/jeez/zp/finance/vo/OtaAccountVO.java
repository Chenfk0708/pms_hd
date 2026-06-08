package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class OtaAccountVO {
    private String accountId;
    private String channelId;
    private String channelName;
    private String accountName;
    private String outAccountId;
    private String status;
    private String authorizedAt;
    private String expiredAt;
}
