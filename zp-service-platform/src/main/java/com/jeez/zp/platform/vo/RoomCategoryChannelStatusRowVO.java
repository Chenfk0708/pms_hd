package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryChannelStatusRowVO {

    private String roomCategoryId;
    private String roomCategoryName;
    private String roomCategoryProductName;
    private String channelId;
    private String channelName;
    private String expressValue;
    private Long normalPrice;
    private Long normalActualSalePrice;
    private List<RoomCategoryStatusDayVO> statusViews;
}
