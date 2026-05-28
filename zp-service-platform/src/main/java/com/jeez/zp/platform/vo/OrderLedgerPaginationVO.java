package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class OrderLedgerPaginationVO {

    private Integer page;
    private Integer pageSize;
    private Long total;
}
