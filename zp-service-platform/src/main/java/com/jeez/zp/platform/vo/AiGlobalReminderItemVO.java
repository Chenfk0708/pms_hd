package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class AiGlobalReminderItemVO {

    private String id;
    private String campId;
    private String level;
    private String title;
    private String guestName;
    private String roomName;
    private String orderNo;
    private String dueAt;
    private String channel;
    private String status;
    private String primaryAction;
    private String summary;
}
