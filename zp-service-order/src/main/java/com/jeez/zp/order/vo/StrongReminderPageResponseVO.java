package com.jeez.zp.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class StrongReminderPageResponseVO {

    private List<StrongReminderItemVO> list;
    private StrongReminderPaginationVO pagination;
}
