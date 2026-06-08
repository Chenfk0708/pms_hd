package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class ShiftWorkConfigRowVO {
    private String id;
    private String name;
    private String startTime;
    private String endTime;
    private String memberIdsJson;
    private String memberNamesText;
    private String updatedAt;
}
