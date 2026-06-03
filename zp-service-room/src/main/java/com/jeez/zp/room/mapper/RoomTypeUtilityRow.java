package com.jeez.zp.room.mapper;

import lombok.Data;

@Data
public class RoomTypeUtilityRow {

    private Long id;
    private Long poiId;
    private String name;
    private String tagType;
    private Integer sortNo;
    private Integer roomTypeCount;
    private String roomTypeIdsText;
    private String roomTypeNamesText;
}
