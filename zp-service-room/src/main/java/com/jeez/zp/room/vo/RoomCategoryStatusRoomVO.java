package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryStatusRoomVO {

    private String roomCategoryId;
    private String roomCategoryName;
    private Long normalPrice;
    private Long normalActualSalePrice;
    private List<RoomCategoryStatusDayVO> statusViews;
    private List<ChannelRoomCategoryStatusVO> channelRoomCategoryStatuses;
}
