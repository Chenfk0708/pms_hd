package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryCentralStatusResponseVO {

    private List<RoomCategoryStatusRoomVO> roomStatusViews;
    private PageXVO pageX;
}
