package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class PresaleOrderDetailViewVO {

    private String goodsName;
    private Integer roomCategoryType;
    private String categoryId;
    private String categoryName;
    private Integer count;
    private Long price;
}
