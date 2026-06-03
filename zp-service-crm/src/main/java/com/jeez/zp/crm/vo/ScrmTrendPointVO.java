package com.jeez.zp.crm.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScrmTrendPointVO {

    private String label;
    private Integer sessions;
    private Integer orders;
}
