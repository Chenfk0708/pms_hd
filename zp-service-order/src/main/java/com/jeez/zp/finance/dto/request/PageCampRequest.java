package com.jeez.zp.finance.dto.request;

import lombok.Data;

@Data
public class PageCampRequest {
    private String campId;
    private Integer pageNum;
    private Integer pageSize;
}
