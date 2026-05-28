package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.Map;

@Data
public class CampDetailVO {

    private Long campId;
    private String name;
    private String cityName;
    private String address;
    private String contactNumber;
    private Long poiId;
    private String poiName;
    private Map<String, Object> camp;
}
