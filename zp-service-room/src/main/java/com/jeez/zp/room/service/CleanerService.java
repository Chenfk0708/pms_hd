package com.jeez.zp.room.service;

import com.jeez.zp.room.dto.request.CleanerPageRequest;
import com.jeez.zp.room.vo.CleanerListItemVO;
import com.jeez.zp.room.vo.CleanerPageResponseVO;

import java.util.List;

public interface CleanerService {

    List<CleanerListItemVO> getCleaners(Long campId, Long userId);

    CleanerPageResponseVO getPage(CleanerPageRequest request, Long userId);
}
