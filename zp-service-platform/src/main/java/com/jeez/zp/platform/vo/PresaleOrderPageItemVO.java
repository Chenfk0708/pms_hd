package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class PresaleOrderPageItemVO {

    private String orderId;
    private String orderChannelId;
    private String orderChannelName;
    private String paymentWayId;
    private String paymentWayName;
    private Integer orderState;
    private Integer refundDisplayState;
    private Long totalAmount;
    private Long paidAmount;
    private String buyerName;
    private String buyerMobile;
    private String createdAt;
    private List<PresaleOrderDetailViewVO> orderDetailViews;
}
