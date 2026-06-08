package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class PoiPageItemVO {

    private String poiId;
    private String poiName;
    private String id;
    private String name;
    private String label;
    private String value;
    private String poiType;
    private String typeName;
    private String address;
    private String contactNumber;
    private String cityName;
    private String cityPath;
    private String streetAddress;
    private String communityName;
    private String unitNo;
    private String fullAddress;
    private String tagsJson;
    private String plainIntro;
    private String richIntro;
    private String coverImageDataUrl;
    private Integer photoCount;
}
