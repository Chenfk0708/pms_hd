package com.jeez.zp.crm.vo;

import lombok.Data;

import java.util.List;

@Data
public class WechatKfAccountReportResponseVO {

    private WechatKfSummaryVO summary;
    private List<WechatKfConversationVO> conversations;
    private List<WechatKfTodoVO> todos;
    private Long total;
    private WechatPaginationVO pagination;
}
