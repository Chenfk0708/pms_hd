package com.jeez.zp.crm.dto.request;

import lombok.Data;

@Data
public class ImPhrasePageRequest {

    private String campId;
    private Integer current;
    private Integer page;
    private Integer pageNum;
    private Integer pageSize;
    private String keyword;
    private String imWordsGroupId;
    private Integer isTemplate;
    private Integer scope;
}
