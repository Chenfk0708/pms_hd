package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class DistributionFlowPageResponseVO {

    private Long total;
    private Integer size;
    private Integer current;
    private Integer pageNum;
    private Integer pages;
    private Boolean hasNextPage;
    private List<DistributionFlowRowVO> list;
}
