package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomTypeFloorVO {

    private String id;
    private String floorId;
    private String name;
    private String floorName;
    private String poiId;
    private Integer sortNo;
    private Integer count;
    private Integer roomTypeCount;
    private List<String> roomTypeIds;
    private List<String> roomTypeNames;
}
