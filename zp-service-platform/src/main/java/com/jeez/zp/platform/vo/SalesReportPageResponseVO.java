package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class SalesReportPageResponseVO {

    private Long total;
    private Integer size;
    private Integer current;
    private Integer pageNum;
    private Integer pageSize;
    private Integer pages;
    private List<SalesReportRowVO> list;
}
