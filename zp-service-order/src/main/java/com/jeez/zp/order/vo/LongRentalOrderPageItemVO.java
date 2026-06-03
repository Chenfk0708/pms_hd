package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class LongRentalOrderPageItemVO {

    private String orderId;
    private String outOrderId;
    private String channelId;
    private String orderChannelId;
    private String channelName;
    private String guestName;
    private String guestMobile;
    private String roomCategoryId;
    private String roomCategoryName;
    private String roomName;
    private String poiId;
    private String poiName;
    private String checkInTime;
    private String checkOutTime;
    private String liveStatusName;
    private String liveStatusCode;
    private Integer orderState;
    private String orderType;
    private Long ltGrossRevenuePrice;
    private Long ltGrossProceedPrice;
    private Long ltOtherPrice;
    private Long ltDepositPrice;
    private Long orderTotalIncomePrice;
    private String ltRentStartDate;
    private String ltRentEndDate;
    private String ltPeriodOfContract;
    private String paymentWayName;
    private String paymentTime;
    private String createTimeText;
    private Integer isOccupyStock;
    private String arrangeRoomStatusName;
    private String includeStatisticsName;
    private String contractNo;
    private Long nextPaymentAmount;
    private String nextPaymentDate;
}
