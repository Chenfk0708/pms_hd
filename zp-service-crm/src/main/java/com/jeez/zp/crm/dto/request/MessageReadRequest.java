package com.jeez.zp.crm.dto.request;

import lombok.Data;

@Data
public class MessageReadRequest {

    private String campId;
    private String messageId;
    private String groupType;
}
