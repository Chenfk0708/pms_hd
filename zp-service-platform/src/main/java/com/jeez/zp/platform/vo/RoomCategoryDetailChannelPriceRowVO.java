package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class RoomCategoryDetailChannelPriceRowVO {

    private String label;
    private Long basePrice;
    private Long weekendPrice;
    private String shelfStatus;
    private String auditStatus;
}
