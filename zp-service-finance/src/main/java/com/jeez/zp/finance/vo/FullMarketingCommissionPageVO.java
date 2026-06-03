package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FullMarketingCommissionPageVO {
    private Long total;
    private Integer size;
    private Integer current;
    private Integer pageNum;
    private Integer pages;
    private Boolean hasNextPage;
    private List<FullMarketingCommissionRowVO> list;
    private Map<String, Object> requestEcho;
}
