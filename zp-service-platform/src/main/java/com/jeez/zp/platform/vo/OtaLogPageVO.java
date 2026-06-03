package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class OtaLogPageVO {

    private List<OtaOptionVO> channelOptions;
    private List<OtaOptionVO> operationTypeOptions;
    private List<OtaOptionVO> operationStatusOptions;
    private List<OtaLogRowVO> rows;
    private OtaLogPaginationVO pagination;
}
