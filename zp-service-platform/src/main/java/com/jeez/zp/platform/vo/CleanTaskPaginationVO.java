package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class CleanTaskPaginationVO {

    private Integer page;
    private Integer pageSize;
    private Long total;
}
