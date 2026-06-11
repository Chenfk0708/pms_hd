package com.jeez.zp.order.dto.request;

import lombok.Data;

@Data
public class ChannelOrderImportRequest {

    private String channelCode;
    private String accountId;
    private String outOrderNo;
    private String outPoiId;
    private String outRoomCategoryId;
    private String roomCategoryName;
    private String contactName;
    private String contactMobile;
    private String checkInDate;
    private String checkOutDate;
    private Integer quantity;
    private Long totalPrice;
    private Long totalPayPrice;
    private Long commissionPrice;
    private String paymentStatus;
    private String channelStatus;
    private String remark;
    private Object rawPayload;
}
