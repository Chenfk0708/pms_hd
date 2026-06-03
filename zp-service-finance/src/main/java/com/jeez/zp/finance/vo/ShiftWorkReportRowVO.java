package com.jeez.zp.finance.vo;

import lombok.Data;

@Data
public class ShiftWorkReportRowVO {
    private String id;
    private String shiftRecordId;
    private String workDate;
    private String handoverDate;
    private String shiftName;
    private String workName;
    private String handoverUserId;
    private String handoverUserName;
    private String workUserName;
    private String handoverTime;
    private String workTime;
    private String receiverUserId;
    private String receiverUserName;
    private String successorUserName;
    private String receiverTime;
    private String successorTime;
    private Integer workStatus;
    private String statusName;
    private String handoverRemark;
    private String receiverRemark;
    private String systemGeneratedAt;
    private String createTime;
    private String poiId;
    private String storeId;
    private String poiName;
    private String storeName;
    private String workReport;
    private Double cashAmount;
    private Integer roomCardCount;
    private String nightAuditStatus;
}
