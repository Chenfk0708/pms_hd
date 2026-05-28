package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.PoiMapper;
import com.jeez.zp.platform.service.PoiService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.PoiPageItemVO;
import com.jeez.zp.platform.vo.PoiPageResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoiServiceImpl implements PoiService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final PoiMapper poiMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public PoiPageResponseVO getPage(
            Long campId,
            Long userId,
            Long channelId,
            Integer isAvailability,
            Integer pageNum,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int resolvedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        long offset = (long) (resolvedPageNum - 1) * resolvedPageSize;

        long total = poiMapper.countPage(
                resolvedCampId,
                normalizeChannelId(channelId),
                isAvailability
        );
        List<PoiPageItemVO> items = total == 0
                ? List.of()
                : poiMapper.selectPage(
                resolvedCampId,
                normalizeChannelId(channelId),
                isAvailability,
                offset,
                resolvedPageSize
        );

        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / resolvedPageSize);

        PoiPageResponseVO response = new PoiPageResponseVO();
        response.setTotal(total);
        response.setSize(resolvedPageSize);
        response.setCurrent(resolvedPageNum);
        response.setExtraInfo(null);
        response.setPageNum(resolvedPageNum);
        response.setHasNextPage(resolvedPageNum < pages);
        response.setPages(pages);
        response.setList(items);
        return response;
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
            throw new BusinessException(40301, "无权访问当前门店门店列表数据");
        }
        return requestedCampId;
    }
}
