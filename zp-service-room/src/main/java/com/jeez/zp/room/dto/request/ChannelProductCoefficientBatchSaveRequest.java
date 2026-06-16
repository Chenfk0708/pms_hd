package com.jeez.zp.room.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ChannelProductCoefficientBatchSaveRequest {

    private String campId;
    private List<Item> items;

    @Data
    public static class Item {
        private String roomCategoryId;
        private String channelId;
        private String productName;
        private String operator;
        private BigDecimal coefficientValue;
    }
}
