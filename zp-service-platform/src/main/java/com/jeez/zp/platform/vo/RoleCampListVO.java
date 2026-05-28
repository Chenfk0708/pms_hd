package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class RoleCampListVO {

    private List<RoleSummaryVO> roles;
    private PaginationVO pagination;
}
