package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("company_api_key")
public class CompanyApiKey {

    @TableId("api_key_id")
    private Long apiKeyId;
    private Long campId;
    private String appId;
    private String accessKeyId;
    private String secretKeyCiphertext;
    private String secretKeyPreview;
    private Integer status;
    private String scopesJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime rotatedAt;
    private Integer isDeleted;
}
