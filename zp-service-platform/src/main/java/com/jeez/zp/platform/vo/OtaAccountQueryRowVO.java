package com.jeez.zp.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OtaAccountQueryRowVO {

    private String accountId;
    private Long accountIdRaw;
    private Long channelId;
    private String channelName;
    private String accountName;
    private String status;
    private LocalDateTime authorizedAt;
    private LocalDateTime updatedAt;
    private Integer roomTypeCount;
    private Integer mappedRoomTypeCount;
    private LocalDateTime lastRoomSyncAt;
}
