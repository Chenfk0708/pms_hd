package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class WorkspaceOrderItemVO {

    private String channelName;
    private String guestName;
    private String guestMobile;
    private String roomCategoryName;
    private String roomName;
    private Long startTime;
    private Long endTime;
    private Integer dayNum;
    private String orderDetailDisplayStateName;
    private String statusName;
}
