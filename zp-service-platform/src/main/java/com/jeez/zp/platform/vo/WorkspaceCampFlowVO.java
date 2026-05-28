package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class WorkspaceCampFlowVO {

    private Integer isOpenFlow;
    private List<WorkspaceCampFlowChannelInfoVO> channelInfos;
}
