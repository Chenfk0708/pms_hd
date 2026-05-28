package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class OtaDetailStoreQueryRowVO {

    private String id;
    private String accountId;
    private String channelStoreId;
    private String channelStoreName;
    private String hotelId;
    private String syncStatus;
    private Integer roomTypeCount;
    private Integer mappedRoomTypeCount;
}
