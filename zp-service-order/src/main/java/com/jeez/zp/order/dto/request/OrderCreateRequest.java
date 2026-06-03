package com.jeez.zp.order.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {

    private String orderId;
    private String campId;
    private String poiId;
    private String roomCategoryId;
    private String roomId;
    private String orderType;
    private String stayType;
    private String guestName;
    private String guestMobile;
    private String checkInDate;
    private String checkOutDate;
    private Long totalPrice;
    private Long totalPayPrice;
    private Long commissionPrice;
    private String paymentStatus;
    private String poiName;
    private String roomCategoryName;
    private String roomName;
    private String sourceLabel;
    private String channelOrderNo;
    private Long depositPrice;
    private Long otherPrice;
    private String invoiceIssuer;
    private Long invoiceAmount;
    private String emergencyName;
    private String emergencyMobile;
    private String paymentCycle;
    private String paymentMonth;
    private String paymentDay;
    private String roomChargeStatus;
    private Long roomChargeReceived;
    private String roomChargeMethod;
    private String depositChargeStatus;
    private Long depositChargeReceived;
    private String depositChargeMethod;
    private Integer reminderEnabled;
    private String contractDueMode;
    private String contractNo;
    private String nextPaymentDate;
    private Long nextPaymentAmount;
    private Long extraFee;
    private List<OrderCreateRoomItemRequest> rooms;
    private List<OrderCreateKeyValueItemRequest> tags;
    private List<OrderCreateKeyValueItemRequest> reminders;
    private List<OrderCreateKeyValueItemRequest> extraFeeItems;
    private String billingSnapshot;
    private String remark;
    private List<OrderGuestSaveItemRequest> guests;
}
