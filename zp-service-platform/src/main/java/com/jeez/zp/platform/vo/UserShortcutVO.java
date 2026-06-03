package com.jeez.zp.platform.vo;

import lombok.Data;

@Data
public class UserShortcutVO {

    private Integer code;
    private String name;
    private String win;
    private String mac;
    private Integer isOpen;
}
