package com.jeez.zp.finance.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftWorkPaginationVO {
    private Long total;
    private Integer size;
    private Integer current;
    private Integer pageNum;
    private Boolean hasNextPage;
    private Integer pages;
}
