package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class SortSettingMutationResultVO {

    private String message;
    private Integer updatedCount;
    private List<String> ids;

    public static SortSettingMutationResultVO of(String message, List<String> ids) {
        SortSettingMutationResultVO result = new SortSettingMutationResultVO();
        result.setMessage(message);
        result.setIds(ids);
        result.setUpdatedCount(ids == null ? 0 : ids.size());
        return result;
    }
}
