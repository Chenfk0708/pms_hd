package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class CategoryRequest {

    private String campId;
    private Integer parentId;
}
