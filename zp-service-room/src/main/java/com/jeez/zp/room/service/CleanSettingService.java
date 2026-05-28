package com.jeez.zp.room.service;

import com.jeez.zp.room.vo.CleanSettingBootstrapResponseVO;

public interface CleanSettingService {

    CleanSettingBootstrapResponseVO bootstrap(
            Long campId,
            Long userId,
            String businessDate,
            String storeId,
            String projectId,
            String status,
            Integer page,
            Integer pageSize
    );
}
