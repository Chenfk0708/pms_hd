package com.jeez.zp.order.dto.request;

import lombok.Data;

@Data
public class ChannelCallbackOrderImportRequest {

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

    public ChannelOrderImportRequest toImportRequest(String channelCode) {
        ChannelOrderImportRequest request = new ChannelOrderImportRequest();
        request.setChannelCode(channelCode);
        request.setAccountId(accountId);
        request.setOutOrderNo(outOrderNo);
        request.setOutPoiId(outPoiId);
        request.setOutRoomCategoryId(outRoomCategoryId);
        request.setRoomCategoryName(roomCategoryName);
        request.setContactName(contactName);
        request.setContactMobile(contactMobile);
        request.setCheckInDate(checkInDate);
        request.setCheckOutDate(checkOutDate);
        request.setQuantity(quantity);
        request.setTotalPrice(totalPrice);
        request.setTotalPayPrice(totalPayPrice);
        request.setCommissionPrice(commissionPrice);
        request.setPaymentStatus(paymentStatus);
        request.setChannelStatus(channelStatus);
        request.setRemark(remark);
        request.setRawPayload(rawPayload);
        return request;
    }
}
