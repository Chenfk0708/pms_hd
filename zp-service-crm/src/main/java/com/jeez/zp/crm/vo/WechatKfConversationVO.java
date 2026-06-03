package com.jeez.zp.crm.vo;

import lombok.Data;

@Data
public class WechatKfConversationVO {

    private String id;
    private String customerName;
    private String channel;
    private String channelName;
    private String status;
    private String statusName;
    private String orderStatus;
    private String stayDate;
    private String roomType;
    private String lastMessage;
    private String lastMessageAt;
    private String assignee;
    private Integer unread;
}
