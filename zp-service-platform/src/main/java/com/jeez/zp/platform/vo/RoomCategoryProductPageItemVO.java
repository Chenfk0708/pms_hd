package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class RoomCategoryProductPageItemVO {

    private String productId;
    private String id;
    private String title;
    private String productName;
    private String roomCategoryId;
    private String roomCategoryName;
    private String channelId;
    private String channelName;
    private Integer stock;
    private Long salePrice;
    private Long extraPrice;
    private String createdAt;
    private String updatedAt;
    private String status;
    private String reservationPhone;
    private String reservationNote;
}
