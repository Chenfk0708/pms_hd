package com.jeez.zp.room.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomStatusesMonthlyOccVO {

    private String date;
    private Integer availabilityCount;
    private Integer openRoomCount;
    private Integer roomSaleCount;
    private Integer vacantCount;
    private BigDecimal occ;
    private BigDecimal occupancyRate;
}