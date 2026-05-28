package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class RoomStatusOperationLogPageRequest {

    private String campId;
    private Integer pageNum;
    private Integer pageSize;
    private Integer current;
    private String keyword;
    private Integer adjustType;
    private String channelId;
    private String startDate;
    private String endDate;
    private String createStartTime;
    private String createEndTime;
    private String userName;
}
