package com.jeez.zp.room.vo;

import lombok.Data;

@Data
public class CleanSettingPaginationVO {

    private Integer page;
    private Integer pageSize;
    private Long total;
}
