package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class SystemConfigItemVO {

    private String configKey;
    private Object configValue;
    private String configScope;
    private String source;
}
