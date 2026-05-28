package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.SmsAccountSummaryVO;

public interface SmsAccountService {

    SmsAccountSummaryVO getSmsAccountSummary(Long campId, Long userId);
}
