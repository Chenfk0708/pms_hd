package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class ChannelRoomCategoryStatusVO {

    private String channelId;
    private String channelName;
    private String channelRoomCategoryName;
    private String expressValue;
    private Long normalPrice;
    private Long normalActualSalePrice;
    private List<RoomCategoryStatusDayVO> statusViews;
}
