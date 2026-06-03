package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class PriceLogPaginationVO {

    private Integer page;
    private Integer pageSize;
    private Long total;
}
