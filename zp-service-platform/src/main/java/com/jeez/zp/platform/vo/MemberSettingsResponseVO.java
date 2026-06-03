package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MemberSettingsResponseVO {

    private MemberSettingSummaryVO summary;
    private List<MemberSettingRoleVO> roles;
    private List<MemberSettingMemberVO> members;
    private List<Map<String, String>> pendingFlows;
    private List<MemberSettingRoomCategoryVO> roomCategories;
    private MemberSettingPaginationVO pagination;
    private MemberSettingEditorVO editor;
}
