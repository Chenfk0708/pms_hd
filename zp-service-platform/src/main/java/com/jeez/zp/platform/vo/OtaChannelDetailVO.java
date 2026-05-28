package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class OtaChannelDetailVO {

    private String id;
    private String channelName;
    private String title;
    private String description;
    private String logoText;
    private Integer logoTone;
    private String noticeText;
    private String noticeLinkLabel;
    private List<OtaOptionVO> channelStoreOptions;
    private List<OtaOptionVO> accountOptions;
    private List<OtaOptionVO> statusOptions;
    private List<OtaDetailRoomRowVO> roomRows;
    private List<OtaDetailStoreRowVO> storeRows;
    private OtaSyncStoreNoticeVO syncStoreNotice;
    private OtaSyncStoreDefaultsVO syncStoreDefaults;
}
