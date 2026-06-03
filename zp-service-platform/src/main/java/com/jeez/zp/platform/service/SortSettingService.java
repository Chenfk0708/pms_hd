package com.jeez.zp.platform.service;

import com.jeez.zp.platform.dto.request.ChannelRoomCategorySeqsRequest;
import com.jeez.zp.platform.dto.request.RoomCategorySeqsRequest;
import com.jeez.zp.platform.vo.SortSettingMutationResultVO;

public interface SortSettingService {

    SortSettingMutationResultVO updateRoomCategorySeqs(Long campId, Long userId, RoomCategorySeqsRequest request);

    SortSettingMutationResultVO updateChannelRoomCategorySeqs(Long campId, Long userId, ChannelRoomCategorySeqsRequest request);
}
