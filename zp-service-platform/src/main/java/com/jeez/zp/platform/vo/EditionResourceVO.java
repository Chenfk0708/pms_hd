package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class EditionResourceVO {

    private String editionId;
    private String editionName;
    private Integer editionType;
    private String resourceName;
    private String expireDateRange;
    private String priceText;
    private String connectorProgress;
    private List<Map<String, Object>> resourceGetViews;
    private Object valueAddServices;
}
