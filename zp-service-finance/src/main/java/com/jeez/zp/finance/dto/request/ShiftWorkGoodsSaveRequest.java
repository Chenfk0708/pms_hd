package com.jeez.zp.finance.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ShiftWorkGoodsSaveRequest {
    private String campId;
    private List<ShiftWorkGoodsDraftRequest> drafts;
}
