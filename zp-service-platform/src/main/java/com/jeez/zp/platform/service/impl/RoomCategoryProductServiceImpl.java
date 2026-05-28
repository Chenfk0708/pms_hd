package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.RoomCategoryProductMapper;
import com.jeez.zp.platform.service.RoomCategoryProductService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.PaginationVO;
import com.jeez.zp.platform.vo.RoomCategoryProductPageItemVO;
import com.jeez.zp.platform.vo.RoomCategoryProductPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomCategoryProductServiceImpl implements RoomCategoryProductService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final RoomCategoryProductMapper roomCategoryProductMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public RoomCategoryProductPageResponseVO getPage(
            Long campId,
            Long userId,
            String keyword,
            Long roomCategoryId,
            Long channelId,
            Integer pageNum,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int resolvedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        long offset = (long) (resolvedPageNum - 1) * resolvedPageSize;

        long total = roomCategoryProductMapper.countPage(
                resolvedCampId,
                trimToNull(keyword),
                roomCategoryId,
                normalizeChannelId(channelId)
        );
        List<RoomCategoryProductPageItemVO> items = total == 0
                ? List.of()
                : roomCategoryProductMapper.selectPage(
                resolvedCampId,
                trimToNull(keyword),
                roomCategoryId,
                normalizeChannelId(channelId),
                offset,
                resolvedPageSize
        );

        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / resolvedPageSize);

        RoomCategoryProductPageResponseVO response = new RoomCategoryProductPageResponseVO();
        response.setTotal(total);
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setExtraInfo(null);
        response.setPageNum(resolvedPageNum);
        response.setHasNextPage(resolvedPageNum < pages);
        response.setPages(pages);
        response.setList(items);
        response.setPagination(new PaginationVO(resolvedPageNum, resolvedPageSize, Math.toIntExact(total)));
        return response;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long normalizeChannelId(Long channelId) {
        if (channelId == null || channelId == 0L) {
            return null;
        }
        return channelId;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "无权访问当前门店酒店套餐数据");
        }
        return requestedCampId;
    }
}
