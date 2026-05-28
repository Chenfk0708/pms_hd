package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class OrderPageResponseVO {

    private Long total;
    private Integer size;
    private Integer current;
    private Integer pageNum;
    private Integer pageSize;
    private Boolean hasNextPage;
    private Integer pages;
    private List<?> list;
}
