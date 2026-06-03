package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class OrderDetailViewVO {

    private String poiId;
    private String poiName;
    private String roomCategoryId;
    private String roomCategoryName;
    private String roomCategoryProductName;
    private String roomId;
    private String roomName;
    private Long checkInDate;
    private Long checkOutDate;
    private Integer orderDetailDisplayState;
    private Integer isArrangeRoom;
    private Integer isOccupation;
    private Integer isStatistics;
}
