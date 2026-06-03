package com.jeez.zp.room.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CleanStatisticsMetricVO {

    private String id;
    private String label;
    private String value;
    private String unit;
    private String trend;
    private String description;
}
