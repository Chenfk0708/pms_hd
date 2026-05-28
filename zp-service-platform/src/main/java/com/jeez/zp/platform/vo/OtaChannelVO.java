package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class OtaChannelVO {

    private String id;
    private String name;
    private String relation;
    private String status;
    private Integer roomTypeCount;
    private Integer mappedRoomTypeCount;
    private String lastSyncAt;
    private String logoText;
    private String detail;
    private OtaChannelAuthorizationNoticeVO authorizationNotice;
}
