package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomItemVO {

    private String roomCategoryId;
    private String roomId;
    private String roomName;
    private Integer isDirty;
    private Integer isCanBooking;
    private String floorId;
    private String floorName;
    private Integer seq;
    private String lockStatus;
    private String saleType;
    private String cleanStatus;
    private List<Object> deviceViews;
}
