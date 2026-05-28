package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class OrderPageRequest {

    private String campId;
    private Integer current;
    private Integer pageNum;
    private Integer pageSize;
    private List<String> roomCategoryTypes;
    private List<String> orderStates;
    private List<String> categoryIds;
    private List<String> orderChannelIds;
    private List<String> paymentWayIds;
    private String refundDisplayState;
    private String bookedStartDate;
    private String bookedEndDate;
    private String orderType;
    private Integer isLt;
    private String searchContent;
    private String keyword;
    private String searchCode;
    private String dateType;
    private String orderStatus;
    private String channelId;
    private String roomCategoryId;
    private String liveStatus;
    private String poiId;
}
