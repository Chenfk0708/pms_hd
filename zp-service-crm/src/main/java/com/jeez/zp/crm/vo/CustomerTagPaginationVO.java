package com.jeez.zp.crm.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerTagPaginationVO {

    private Integer page;
    private Integer pageSize;
    private Integer total;
}
