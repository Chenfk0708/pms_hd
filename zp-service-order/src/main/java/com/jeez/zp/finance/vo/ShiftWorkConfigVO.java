package com.jeez.zp.finance.vo;

import lombok.Data;

import java.util.List;

@Data
public class ShiftWorkConfigVO {
    private String id;
    private String shiftWorkConfigId;
    private String name;
    private String shiftName;
    private String startTime;
    private String endTime;
    private List<String> memberIds;
    private List<String> memberNames;
    private String updatedAt;
}
