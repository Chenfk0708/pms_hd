package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;

@Data
public class LedgerEntryPageVO {
    private Long total;
    private Integer size;
    private Integer current;
    private Object extraInfo;
    private Integer pageNum;
    private Boolean hasNextPage;
    private Integer pages;
    private List<LedgerEntryRowVO> list;
}
