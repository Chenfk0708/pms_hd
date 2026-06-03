package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class PriceLogPageResponseVO {

    private List<PriceLogRowVO> list;
    private PriceLogPaginationVO pagination;
    private PriceLogDictionariesVO dictionaries;
}
