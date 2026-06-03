package com.jeez.zp.platform.dto.request;

import lombok.Data;

@Data
public class WorkspaceMemoHandleRequest {

    private String campId;
    private String memoId;
    private Integer isHandle;
}
