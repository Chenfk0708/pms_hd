package com.jeez.zp.crm.vo;

import lombok.Data;

import java.util.List;

@Data
public class ScrmSidebarConversationVO {

    private String id;
    private String guestName;
    private String channel;
    private String roomName;
    private String status;
    private String lastMessage;
    private String lastSender;
    private String lastMessageAt;
    private String responseSla;
    private String orderNo;
    private String stayRange;
    private List<String> tags;
    private String preference;
    private String orderAmount;
}
