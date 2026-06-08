package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class OtaSummaryVO {
    private Integer totalAccounts;
    private Integer authorizedAccounts;
    private Integer linkedPois;
    private Integer linkedRoomCategories;
}
