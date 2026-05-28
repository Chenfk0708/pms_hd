package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("channel_account")
public class ChannelAccount {

    @TableId("account_id")
    private Long accountId;
    private Long campId;
    private Long channelId;
    private String channelName;
    private String accountName;
    private String outAccountId;
    private String status;
    private LocalDateTime authorizedAt;
    private LocalDateTime expiredAt;
    private String configJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
