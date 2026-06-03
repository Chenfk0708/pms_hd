package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class FullMarketingProductSaleRowVO {
    private String id;
    private String productId;
    private String name;
    private Integer sales;
    private Double turnover;
    private Double amount;
    private Double commission;
}
