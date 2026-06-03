package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomTypeTagVO {

    private String id;
    private String tagId;
    private String name;
    private String tagName;
    private String tagType;
    private Integer sortNo;
    private Integer count;
    private Integer roomTypeCount;
    private List<String> roomTypeIds;
    private List<String> roomTypeNames;
}
