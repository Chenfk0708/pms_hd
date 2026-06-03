package com.jeez.zp.crm.vo;

import lombok.Data;

@Data
public class WechatKfAccountVO {

    private String id;
    private String name;
    private String status;
    private Integer todaySessions;
    private Integer averageReplySeconds;
    private Integer serviceScore;
}
