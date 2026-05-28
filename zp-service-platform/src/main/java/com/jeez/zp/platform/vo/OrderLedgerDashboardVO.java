package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class OrderLedgerDashboardVO {

    private String provider;
    private String state;
    private Object request;
    private List<OrderLedgerStoreOptionVO> stores;
    private List<OrderLedgerOptionVO> typeOptions;
    private List<OrderLedgerOptionVO> sourceOptions;
    private List<OrderLedgerOptionVO> projectOptions;
    private List<OrderLedgerOptionVO> paymentWayOptions;
    private List<RoomCategoryRoomsGroupVO> roomOptions;
    private OrderLedgerSummaryVO summary;
    private List<OrderLedgerRecordVO> records;
    private OrderLedgerPaginationVO pagination;
    private String updatedAt;
    private List<String> traceIds;
}
