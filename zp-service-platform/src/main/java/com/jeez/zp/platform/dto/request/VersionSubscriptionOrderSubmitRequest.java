package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class VersionSubscriptionOrderSubmitRequest {

    private String campId;
    private String editionId;
    private String duration;
    private Integer quantity;
}
