package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sms_template")
public class SmsTemplate {

    @TableId("sms_template_id")
    private Long smsTemplateId;
    private Long campId;
    private String providerCode;
    private String templateTitle;
    private String templateContent;
    private String auditStatus;
    private String sendStatus;
    private String signName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
