package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;

@Data
public class DistributionOrderPageVO {
    private Long total;
    private Integer size;
    private Integer current;
    private Integer pageNum;
    private Integer pages;
    private Boolean hasNextPage;
    private DistributionOrderCampVO camp;
    private DistributionOrderSummaryVO summary;
    private DistributionOrderPaginationVO pagination;
    private List<DistributionOrderVO> list;
}
