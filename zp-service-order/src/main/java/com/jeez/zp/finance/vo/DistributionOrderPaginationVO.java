package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class DistributionOrderPaginationVO {
    private Integer page;
    private Integer pageSize;
    private Long total;
    private Integer pages;
    private Boolean hasNextPage;
}
