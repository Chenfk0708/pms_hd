package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class OtaDashboardRequest {

    private String campId;
    private String businessDate;
    private String storeId;
    private String dimension;
    private String channelId;
}
