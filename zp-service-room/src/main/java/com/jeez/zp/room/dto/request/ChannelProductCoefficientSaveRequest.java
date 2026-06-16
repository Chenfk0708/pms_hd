package com.jeez.zp.room.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChannelProductCoefficientSaveRequest {

    private String campId;
    private String roomCategoryId;
    private String channelId;
    private String productName;
    private String operator;
    private BigDecimal coefficientValue;
}
