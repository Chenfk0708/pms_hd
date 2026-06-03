package com.jeez.zp.crm.vo;

import lombok.Data;

import java.util.List;

@Data
public class ScrmSidebarDashboardResponseVO {

    private List<ScrmLookupOptionVO> stores;
    private List<ScrmLookupOptionVO> channels;
    private List<ScrmMetricVO> metrics;
    private List<ScrmSidebarConversationVO> conversations;
    private List<ScrmPendingItemVO> pendingItems;
    private List<ScrmReplyTemplateVO> replyTemplates;
    private List<ScrmRoomSuggestionVO> roomSuggestions;
    private List<ScrmTrendPointVO> trend;
    private WechatPaginationVO pagination;
}
