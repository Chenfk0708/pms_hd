package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class MemberSettingRoomCategoryVO {

    private String roomCategoryId;
    private String roomCategoryName;
    private List<String> roomIds;
}
