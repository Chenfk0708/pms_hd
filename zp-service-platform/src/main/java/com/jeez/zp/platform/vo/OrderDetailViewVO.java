package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class OrderDetailViewVO {

    private String poiName;
    private String roomCategoryName;
    private String roomCategoryProductName;
    private String roomName;
    private Long checkInDate;
    private Long checkOutDate;
    private Integer orderDetailDisplayState;
    private Integer isArrangeRoom;
    private Integer isOccupation;
    private Integer isStatistics;
}
