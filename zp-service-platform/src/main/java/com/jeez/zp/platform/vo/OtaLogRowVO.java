package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class OtaLogRowVO {

    private String id;
    private String channelId;
    private String channel;
    private String type;
    private String operationType;
    private String content;
    private String status;
    private String operator;
    private String time;
}
