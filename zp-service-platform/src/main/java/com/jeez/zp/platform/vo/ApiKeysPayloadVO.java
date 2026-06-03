package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class ApiKeysPayloadVO {

    private ApiKeyRecordVO keyRecord;
    private List<ApiKeysActivityVO> activityLog;
}
