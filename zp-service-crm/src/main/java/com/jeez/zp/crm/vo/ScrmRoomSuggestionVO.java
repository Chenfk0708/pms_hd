package com.jeez.zp.crm.vo;

import lombok.Data;

@Data
public class ScrmRoomSuggestionVO {

    private String id;
    private String roomName;
    private String status;
    private Integer availableTonight;
    private String action;
}
