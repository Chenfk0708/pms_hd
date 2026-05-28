package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class OrderChannelOptionVO {

    private String channelId;
    private String channelName;
    private String shortChannelName;
    private String color;
    private String imageLogo;
    private String imageOpen;
    private String imageClose;
    private Integer isSupportIcs;
    private Integer isLongRent;
    private Integer isOpen;
}
