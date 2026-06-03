package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoomCategoryLinkageResponseVO {

    private String roomTypeId;
    private String roomTypeName;
    private String description;
    private List<RoomCategoryLinkageCandidateVO> candidates;
}
