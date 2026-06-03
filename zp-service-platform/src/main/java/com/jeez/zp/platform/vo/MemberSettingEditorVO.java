package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class MemberSettingEditorVO {

    private String title;
    private String submitText;
    private String breadcrumbText;
    private String rolePlaceholder;
    private String roomSearchPlaceholder;
    private MemberSettingDraftVO draft;
}
