package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.SmsTemplateMsgConfigPageResponseVO;

public interface SmsTemplateMsgConfigService {

    SmsTemplateMsgConfigPageResponseVO getPage(Long campId, Long userId, Integer sendType, Integer pageNum, Integer pageSize);
}
