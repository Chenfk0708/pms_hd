package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomStatusOperationLogRecordVO {

    private String roomStatusOperationLogId;
    private String roomCategoryName;
    private String roomName;
    private String startDate;
    private String endDate;
    private String operationContent;
    private String adjustContent;
    private String userName;
    private String createTime;
    private List<ChannelRoomStatusOperationLogViewVO> channelRoomStatusOperationLogViews;
}
