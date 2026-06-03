package com.jeez.zp.platform.vo;

import lombok.Data;

import java.util.List;

@Data
public class PrintSettingsResponseVO {

    private List<PrintSettingSectionVO> sections;
    private PrintSettingEmptyStateVO emptyState;
}
