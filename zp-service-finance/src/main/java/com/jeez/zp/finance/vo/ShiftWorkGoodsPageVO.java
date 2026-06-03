package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;

@Data
public class ShiftWorkGoodsPageVO {
    private List<ShiftWorkGoodsVO> list;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer current;
    private Integer size;
    private Integer pages;
    private Boolean hasNextPage;
}
