package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class WorkspacePaginationVO {

    private Integer page;
    private Integer pageSize;
    private Long total;
}
