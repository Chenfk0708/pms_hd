package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class SmsAccountSummaryVO {

    private String id;
    private String campId;
    private String totalSmsCount;
    private String curSmsCount;
}
