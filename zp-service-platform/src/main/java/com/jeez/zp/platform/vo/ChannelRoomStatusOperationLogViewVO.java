package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class ChannelRoomStatusOperationLogViewVO {

    private String channelName;
    private String channelRoomCategoryProductName;
    private String stockContent;
    private Integer isSuccess;
    private String errorMsg;
}
