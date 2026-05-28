package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryChannelStatusResponseVO {

    private List<RoomCategoryChannelStatusRowVO> list;
    private PageXVO pageX;
}
