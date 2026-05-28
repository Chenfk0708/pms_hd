package com.jeez.zp.platform.service;

import com.jeez.zp.platform.vo.PoiPageResponseVO;

public interface PoiService {

    PoiPageResponseVO getPage(
            Long campId,
            Long userId,
            Long channelId,
            Integer isAvailability,
            Integer pageNum,
            Integer pageSize
    );
}
