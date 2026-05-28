package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryDetailResponseVO {

    private String roomName;
    private Integer occupancyRate;
    private Integer inventory;
    private Integer staying;
    private Integer pendingOrders;
    private List<RoomCategoryDetailChannelPriceVO> channelPrices;
    private List<String> guidance;
}
