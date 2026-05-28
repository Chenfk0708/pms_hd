package com.jeez.zp.room.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CleanSettingRowVO {

    private String configKey;
    private String configValue;
    private LocalDateTime updatedAt;
}
