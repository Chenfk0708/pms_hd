package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomStatusesTodayCategoryVO {

    private String roomCategoryId;
    private String roomCategoryName;
    private Integer roomCategorySeq;
    private List<RoomStatusesTodayRoomVO> rooms;
    private Integer roomNum;
    private Integer soldNum;
    private Integer liveNum;
    private Integer idleNum;
    private Integer occNum;
}
