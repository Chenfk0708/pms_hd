package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class WorkspaceMemoPageResponseVO {

    private Long total;
    private List<WorkspaceMemoItemVO> list;
    private WorkspacePaginationVO pagination;
}
