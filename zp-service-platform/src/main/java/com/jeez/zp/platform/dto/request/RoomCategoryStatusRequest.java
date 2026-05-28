package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryStatusRequest {

    private String campId;
    private List<String> channelIds;
    private String date;
    private Integer days;
    private Integer pageNum;
    private Integer pageSize;
    private Integer isFinalChannelRp;
}
