package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.ApiKeysPayloadVO;

public interface ApiKeysService {

    ApiKeysPayloadVO get(Long campId, Long userId);

    ApiKeysPayloadVO generate(Long campId, Long userId);
}
