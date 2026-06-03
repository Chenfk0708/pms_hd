package com.jeez.zp.room.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CleanerPageResponseVO {

    private List<CleanerPageStoreVO> stores;
    private CleanerPageSummaryVO summary;
    private List<CleanerPageItemVO> list;
    private CleanerPagePaginationVO pagination;
    private Map<String, Object> requestBody;
}
