package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class PoiPageRequest {

    private String campId;
    private Integer pageSize;
    private Integer pageNum;
    private Integer current;
    private String channelId;
    private String isAvailability;
}
