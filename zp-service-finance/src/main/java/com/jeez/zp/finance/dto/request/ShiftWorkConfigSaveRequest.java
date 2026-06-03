package com.jeez.zp.finance.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ShiftWorkConfigSaveRequest {
    private String campId;
    private List<ShiftWorkConfigDraftRequest> drafts;
}
