package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomStatusesMonthlyListResponseVO<T> {

    private List<T> list;
}