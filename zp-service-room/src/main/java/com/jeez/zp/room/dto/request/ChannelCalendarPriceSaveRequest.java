package com.jeez.zp.room.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ChannelCalendarPriceSaveRequest {

    private String campId;
    private Boolean overwriteStandalone;
    private List<Item> items;

    @Data
    public static class Item {

        private String roomCategoryId;
        private String channelId;
        private String productName;
        private String date;
        private Integer priceUpdateType;
        private BigDecimal calendarPrice;
        private BigDecimal basePrice;
    }
}
