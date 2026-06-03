package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class PsbLogPageRequest {

    private String campId;
    private String poiId;
    private String keyword;
    private String bizType;
    private String state;
    private Integer pageNum;
    private Integer current;
    private Integer pageSize;
    private List<String> psbType;
}
