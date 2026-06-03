package com.jeez.zp.crm.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrmMetricVO {

    private String id;
    private String label;
    private String value;
    private String change;
    private String tone;
    private String detail;
}
