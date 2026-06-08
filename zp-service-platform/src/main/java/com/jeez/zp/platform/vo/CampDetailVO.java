package com.jeez.zp.platform.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CampDetailVO {

    private Long campId;
    private String campName;
    private String name;
    private String cityName;
    private String address;
    private String contactNumber;
    private Long poiId;
    private String poiName;
    private String poiType;
    private String typeName;
    private String campTypeName;
    private String phone;
    private String cityPath;
    private String streetAddress;
    private String communityName;
    private String unitNo;
    private String fullAddress;
    private List<String> tags;
    private String plainIntro;
    private String richIntro;
    private String coverImageDataUrl;
    private Integer photoCount;
    @JsonIgnore
    private String tagsJson;
    private Map<String, Object> camp;
}
