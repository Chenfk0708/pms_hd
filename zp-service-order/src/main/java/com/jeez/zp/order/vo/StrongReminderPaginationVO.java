package com.jeez.zp.order.vo;

import lombok.Data;

@Data
public class StrongReminderPaginationVO {

    private Integer pageNum;
    private Integer pageSize;
    private Long total;
}
