package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class AiGlobalReminderActionRequest {

    private String campId;
    private String reminderId;
    private String orderNo;
}
