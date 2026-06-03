package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomStatusesRoomsResponseVO {

    private Integer isSingleInventory;
    private List<RoomStatusesRoomsCategoryVO> list;
    private RoomStatusesRoomsPaginationVO pagination;
}
