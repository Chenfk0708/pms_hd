package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class WorkspaceOrdersResponseVO {

    private Long total;
    private List<WorkspaceOrderItemVO> list;
    private WorkspacePaginationVO pagination;
}
