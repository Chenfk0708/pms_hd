package com.jeez.zp.room.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CleanSettingMetricVO {

    private String key;
    private String label;
    private String value;
    private String description;
}
