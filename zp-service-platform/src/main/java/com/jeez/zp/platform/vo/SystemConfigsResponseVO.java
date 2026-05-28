package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class SystemConfigsResponseVO {

    private List<SystemConfigItemVO> configs;
}
