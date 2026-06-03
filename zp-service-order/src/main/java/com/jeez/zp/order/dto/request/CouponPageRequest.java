package com.jeez.zp.order.dto.request;

import lombok.Data;

@Data
public class CouponPageRequest {

    private String campId;
    private Integer shelfStatus;
    private Integer current;
    private Integer pageNum;
    private Integer pageSize;
}
