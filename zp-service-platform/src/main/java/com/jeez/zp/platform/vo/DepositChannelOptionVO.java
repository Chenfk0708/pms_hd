package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class DepositChannelOptionVO {

    private String channelId;
    private String channelName;
    private String channelImageLogo;
    private String channelImageOpen;
    private String channelImageClose;
    private Integer isOpen;
}
