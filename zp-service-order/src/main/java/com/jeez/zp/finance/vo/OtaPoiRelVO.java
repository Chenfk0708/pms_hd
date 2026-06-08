package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class OtaPoiRelVO {
    private String id;
    private String poiId;
    private String poiName;
    private String outPoiId;
    private String syncStatus;
}
