package com.jeez.zp.crm.vo;

import lombok.Data;

@Data
public class MessageItemVO {

    private String messageId;
    private String groupType;
    private String title;
    private String content;
    private String relatedType;
    private String relatedId;
    private String priority;
    private Boolean isRead;
    private String createdAt;
}
