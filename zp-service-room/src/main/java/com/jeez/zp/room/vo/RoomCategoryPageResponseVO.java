package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryPageResponseVO {

    private Long total;
    private Integer size;
    private Integer current;
    private Object extraInfo;
    private Integer pageNum;
    private Boolean hasNextPage;
    private Integer pages;
    private List<RoomCategoryPageItemVO> list;
}
