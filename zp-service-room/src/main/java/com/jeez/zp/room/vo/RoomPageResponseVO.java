package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomPageResponseVO {

    private Long total;
    private Integer size;
    private Integer current;
    private Integer pageNum;
    private Integer pageSize;
    private Boolean hasNextPage;
    private Integer pages;
    private List<RoomPageItemVO> list;
}
