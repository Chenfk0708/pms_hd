package com.jeez.zp.crm.vo;

import lombok.Data;

import java.util.List;

@Data
public class CustomerTagPageResponseVO {

    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private CustomerTagSummaryVO summary;
    private List<CustomerTagGroupVO> list;
    private CustomerTagPaginationVO pagination;
}
