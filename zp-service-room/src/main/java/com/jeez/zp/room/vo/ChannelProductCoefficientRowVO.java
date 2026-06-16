package com.jeez.zp.room.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChannelProductCoefficientRowVO {

    private String campId;
    private String roomCategoryId;
    private String channelId;
    private String productName;
    private String operator;
    private BigDecimal coefficientValue;
    private String expressValue;
}
