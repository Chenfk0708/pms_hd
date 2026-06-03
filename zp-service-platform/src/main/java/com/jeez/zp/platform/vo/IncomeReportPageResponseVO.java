package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class IncomeReportPageResponseVO {

    private Long total;
    private Integer size;
    private Integer current;
    private Integer pageNum;
    private Integer pageSize;
    private Integer pages;
    private List<IncomeReportRowVO> list;
}
