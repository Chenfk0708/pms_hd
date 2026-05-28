package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class WorkspaceMemoPageRequest {

    private String campId;
    private Integer current;
    private Integer page;
    private Integer pageNum;
    private Integer pageSize;
    private Integer isHandle;
}
