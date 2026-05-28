package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class AiGlobalReminderPageRequest {

    private String campId;
    private Integer current;
    private Integer page;
    private Integer pageNum;
    private Integer pageSize;
}
