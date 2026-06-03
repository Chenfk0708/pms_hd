package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;

@Data
public class AccountBookPaymentWayPageVO {
    private Long total;
    private Integer size;
    private Integer current;
    private Integer pageNum;
    private Boolean hasNextPage;
    private Integer pages;
    private List<AccountBookPaymentWayRowVO> list;
    private AccountBookPaymentWayExtraInfoVO extraInfo;
}
