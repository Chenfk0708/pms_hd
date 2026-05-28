package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryChannelOptionsResponseVO {

    private List<RoomCategoryChannelOptionVO> select;
    private List<RoomCategoryChannelOptionVO> list;
}
