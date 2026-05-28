package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class OtaChannelAuthorizationNoticeVO {

    private String title;
    private String summary;
    private String highlight;
    private String summarySuffix;
    private String noticeTitle;
    private List<OtaChannelNoticeSectionVO> noticeSections;
    private String cancelLabel;
    private String confirmLabel;
    private Integer countdownSeconds;
    private String badgeText;
    private String badgeTone;
}
