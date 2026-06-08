package com.jeez.zp.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CampSaveRequest {

    private String campId;
    private String storeId;
    private String poiId;
    private String campName;
    private String name;
    private String typeName;
    private String campTypeName;
    private String phone;
    private String contactNumber;
    private String cityName;
    private String cityPath;
    private String address;
    private String streetAddress;
    private String communityName;
    private String unitNo;
    private String fullAddress;
    private List<String> tags;
    private String plainIntro;
    private String richIntro;
    private String coverImageDataUrl;
    private Integer photoCount;
}
