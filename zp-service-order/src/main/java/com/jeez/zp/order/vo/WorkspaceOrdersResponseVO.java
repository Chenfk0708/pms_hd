package com.jeez.zp.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class WorkspaceOrdersResponseVO {

    private Long total;
    private List<WorkspaceOrderItemVO> list;
    private WorkspacePaginationVO pagination;
}
