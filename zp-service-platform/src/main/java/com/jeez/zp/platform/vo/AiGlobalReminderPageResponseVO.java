package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class AiGlobalReminderPageResponseVO {

    private List<AiGlobalReminderItemVO> list;
    private AiGlobalReminderPaginationVO pagination;
}
