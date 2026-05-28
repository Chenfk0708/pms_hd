package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("system_config")
public class SystemConfig {

    @TableId("system_config_id")
    private Long systemConfigId;
    private Long campId;
    private String configKey;
    private String configScope;
    private String configValue;
    private String valueType;
    private String source;
    private Long updatedBy;
    private LocalDateTime updatedAt;
}
