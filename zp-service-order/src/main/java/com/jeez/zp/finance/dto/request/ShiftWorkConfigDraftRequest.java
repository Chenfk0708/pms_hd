package com.jeez.zp.finance.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ShiftWorkConfigDraftRequest {
    private String id;
    private String name;
    private String startTime;
    private String endTime;
    private List<String> memberIds;
}
