package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomSituationPageResponseVO {

    private Long total;
    private Integer pageNum;
    private Integer current;
    private Integer pageSize;
    private Integer size;
    private List<?> list;
}
