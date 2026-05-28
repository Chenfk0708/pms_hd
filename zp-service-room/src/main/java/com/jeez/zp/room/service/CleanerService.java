package com.jeez.zp.room.service;

import com.jeez.zp.room.vo.CleanerListItemVO;

import java.util.List;

public interface CleanerService {

    List<CleanerListItemVO> getCleaners(Long campId, Long userId);
}
