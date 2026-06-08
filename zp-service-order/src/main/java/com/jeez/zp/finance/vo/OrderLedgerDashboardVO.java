package com.jeez.zp.finance.vo;

import lombok.Data;
import java.util.List;

@Data
public class OrderLedgerDashboardVO {
    private OrderLedgerSummaryVO summary;
    private List<OrderLedgerRecordVO> records;
    private Integer pageNum;
    private Integer pageSize;
    private Long total;
    private LedgerEntryPageVO costPricePages;
    private Double income;
    private Double expend;
    private Double netIncome;
}
