package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class WorkspaceAccommodationAnalysisRequest {

    private String campId;
    private String startDate;
    private String endDate;
    private String predictStartDate;
    private String predictEndDate;
}
