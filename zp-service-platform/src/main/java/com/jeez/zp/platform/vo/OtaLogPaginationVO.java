package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class OtaLogPaginationVO {

    private Integer page;
    private Integer pageSize;
    private Long total;
}
