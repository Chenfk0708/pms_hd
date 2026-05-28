package com.jeez.zp.platform.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_system_config")
public class UserSystemConfig {

    @TableId("user_system_config_id")
    private Long userSystemConfigId;
    private Long campId;
    private Long userId;
    private String configKey;
    private String configValue;
    private LocalDateTime updatedAt;
}
