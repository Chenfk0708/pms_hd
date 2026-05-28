package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class PageXVO {

    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Boolean hasNextPage;
}
