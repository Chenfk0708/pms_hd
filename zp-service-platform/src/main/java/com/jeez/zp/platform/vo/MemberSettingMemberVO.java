package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class MemberSettingMemberVO {

    private String userId;
    private String name;
    private String phone;
    private String roleId;
    private String roleName;
    private String wecomStatus;
    private String wecomLabel;
    private String email;
    private List<String> roomCategoryIds;
}
