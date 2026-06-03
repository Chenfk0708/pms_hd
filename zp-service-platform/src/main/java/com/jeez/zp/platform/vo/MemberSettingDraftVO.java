package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class MemberSettingDraftVO {

    private String userId;
    private String name;
    private String phone;
    private String roleId;
    private String roleName;
    private List<String> roomCategoryIds;
}
