package com.jeez.zp.platform.service;

import com.jeez.zp.platform.dto.request.RoomCategorySaveRequest;
import com.jeez.zp.platform.vo.RoomCategoryDetailResponseVO;
import com.jeez.zp.platform.vo.RoomCategoryEditDraftVO;
import com.jeez.zp.platform.vo.RoomCategoryLinkageResponseVO;
import com.jeez.zp.platform.vo.RoomCategoryMutationResultVO;
import com.jeez.zp.platform.vo.RoomCategoryPageResponseVO;

import java.util.List;

public interface RoomCategoryService {

    RoomCategoryPageResponseVO getPage(
            Long campId,
            Long userId,
            Long poiId,
            Long roomCategoryGroupId,
            String roomCategoryName,
            String keyword,
            Long channelId,
            Integer pageNum,
            Integer pageSize
    );

    RoomCategoryDetailResponseVO getDetail(Long roomCategoryId, Long userId);

    RoomCategoryEditDraftVO getEditDetail(Long campId, Long userId, Long roomCategoryId, String mode);

    RoomCategoryLinkageResponseVO getLinkage(Long campId, Long userId, Long roomCategoryId);

    RoomCategoryMutationResultVO saveLinkage(Long campId, Long userId, Long roomCategoryId, List<Long> linkedRoomCategoryIds);

    RoomCategoryMutationResultVO saveRoomCategory(Long campId, Long userId, RoomCategorySaveRequest.Form form);

    RoomCategoryMutationResultVO deleteRoomCategory(Long campId, Long userId, Long roomCategoryId);
}
