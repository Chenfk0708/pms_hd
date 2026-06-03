package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;

@Data
public class ShiftWorkReportPageVO {
    private List<ShiftWorkReportRowVO> list;
    private List<ShiftWorkReportRowVO> rows;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer current;
    private Integer size;
    private Integer pages;
    private Boolean hasNextPage;
    private ShiftWorkPaginationVO pagination;
}
