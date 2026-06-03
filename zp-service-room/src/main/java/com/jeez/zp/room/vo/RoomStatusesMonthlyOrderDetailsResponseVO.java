package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomStatusesMonthlyOrderDetailsResponseVO {

    private List<RoomStatusesMonthlyOrderDetailVO> list;
    private List<Object> orderArrangementInfos;
    private RoomStatusesRoomsPaginationVO pagination;
}