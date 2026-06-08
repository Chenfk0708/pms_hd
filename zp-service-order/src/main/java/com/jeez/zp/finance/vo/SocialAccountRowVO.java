package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;

@Data
public class SocialAccountRowVO {
    private String id;
    private String channel;
    private String accountId;
    private String store;
    private List<String> authorization;
    private String auditStatus;
    private String syncStatus;
    private String updatedAt;
}
