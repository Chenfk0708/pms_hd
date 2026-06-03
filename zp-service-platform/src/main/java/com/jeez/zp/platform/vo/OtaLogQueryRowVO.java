package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OtaLogQueryRowVO {

    private String accountId;
    private Long channelId;
    private String channelName;
    private String accountName;
    private String accountStatus;
    private LocalDateTime accountAuthorizedAt;
    private LocalDateTime accountUpdatedAt;
    private String roomRelId;
    private String outRoomCategoryId;
    private String roomCategoryName;
    private String shelfStatus;
    private String auditStatus;
    private LocalDateTime roomRelUpdatedAt;
}
