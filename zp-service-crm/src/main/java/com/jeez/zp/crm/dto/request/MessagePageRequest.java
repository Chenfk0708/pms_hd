package com.jeez.zp.crm.dto.request;

import lombok.Data;

@Data
public class MessagePageRequest {

    private String campId;
    private Integer current;
    private Integer page;
    private Integer pageNum;
    private Integer pageSize;
    private String groupType;
    private Integer isRead;
}
