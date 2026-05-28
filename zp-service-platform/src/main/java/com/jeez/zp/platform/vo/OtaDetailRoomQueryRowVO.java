package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class OtaDetailRoomQueryRowVO {

    private String id;
    private String channelStoreId;
    private String channelStoreName;
    private String channelRoomType;
    private String linkedRoomType;
    private String shelfStatus;
    private String auditStatus;
}
