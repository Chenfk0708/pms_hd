package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WorkspaceProductDynamicRowVO {

    private String productId;
    private String productName;
    private String goodsType;
    private Long sellingPriceCent;
    private Integer stock;
    private String shelfStatus;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
