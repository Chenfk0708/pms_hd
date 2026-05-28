package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class ComprehensiveMonthlyReportPageResponseVO {

    private Long total;
    private Integer size;
    private Integer current;
    private Integer pageNum;
    private List<ComprehensiveMonthlyReportItemVO> list;
}
