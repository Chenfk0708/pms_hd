package com.jeez.zp.platform.service;

import com.jeez.zp.platform.dto.request.PrintSettingsSaveRequest;
import com.jeez.zp.platform.vo.PrintSettingsResponseVO;

public interface PrintSettingService {

    PrintSettingsResponseVO getPrintSettings(Long campId, Long userId);

    PrintSettingsResponseVO savePrintSetting(PrintSettingsSaveRequest request, Long userId);
}
