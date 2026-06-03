package com.jeez.zp.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class OrderPageItemVO {

    private String orderId;
    private String outOrderId;
    private String channelName;
    private String orderChannelName;
    private String channelId;
    private String guestName;
    private String guestMobile;
    private Integer orderState;
    private Integer refundDisplayState;
    private Long totalRoomPrice;
    private Long otherPrice;
    private Long includeCommissionRoomPrice;
    private Long orderTotalIncomePrice;
    private Long totalPayPrice;
    private Long commissionPrice;
    private Long debtPrice;
    private Long bookedTime;
    private Long createTime;
    private String confirmNo;
    private List<OrderDetailViewVO> orderDetailViews;
}
