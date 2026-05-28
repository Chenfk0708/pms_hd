package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class AiGlobalShopStatusVO {

    private String id;
    private String campId;
    private String name;
    private String connectorStatus;
    private String radarStatus;
    private List<String> authorizedChannels;
    private String updatedAt;
}
