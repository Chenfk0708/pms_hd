package com.jeez.zp.crm.vo;

import lombok.Data;

import java.util.List;

@Data
public class CustomerPageResponseVO {

    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private List<CustomerItemVO> list;
}
