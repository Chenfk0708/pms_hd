package com.jeez.zp.finance.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CustomChannelMutationRequest {
    private String campId;
    private String channelId;
    private String name;
    private String color;
    private String colorName;
    private Integer enabled;
    private List<SystemChannelStateRequest> systemChannels;
}
