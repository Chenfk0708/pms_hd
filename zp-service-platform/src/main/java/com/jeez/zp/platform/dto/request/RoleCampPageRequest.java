package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class RoleCampPageRequest {

    private String campId;
    private String keyword;
    private Integer pageNum;
    private Integer pageSize;
}
