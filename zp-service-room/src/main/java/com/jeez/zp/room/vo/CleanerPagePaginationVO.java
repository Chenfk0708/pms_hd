package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class CleanerPagePaginationVO {

    private Integer page;
    private Integer pageNum;
    private Integer pageSize;
    private Long total;
}
