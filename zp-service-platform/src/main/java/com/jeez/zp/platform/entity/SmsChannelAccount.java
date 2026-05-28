package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sms_channel_account")
public class SmsChannelAccount {

    @TableId("sms_channel_account_id")
    private Long smsChannelAccountId;
    private Long campId;
    private String providerCode;
    private String providerName;
    private String signName;
    private Integer balanceNum;
    private Integer totalNum;
    private Integer status;
    private String configJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
