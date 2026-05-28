package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryRoomsGroupVO {

    private String roomCategoryId;
    private String roomCategoryName;
    private List<RoomItemVO> rooms;
}
