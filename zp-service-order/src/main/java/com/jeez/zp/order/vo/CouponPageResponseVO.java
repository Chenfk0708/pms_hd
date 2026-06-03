package com.jeez.zp.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class CouponPageResponseVO {

    private Long total;
    private Integer size;
    private Integer current;
    private Integer pageNum;
    private Integer pageSize;
    private Boolean hasNextPage;
    private Integer pages;
    private List<CouponRowVO> list;
}
