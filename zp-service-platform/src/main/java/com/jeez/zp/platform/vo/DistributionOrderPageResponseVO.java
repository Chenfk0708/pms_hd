package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class DistributionOrderPageResponseVO {

    private DistributionOrderCampVO camp;
    private List<DistributionFlowRowVO> list;
    private DistributionOrderSummaryVO summary;
    private DistributionOrderPaginationVO pagination;
}
