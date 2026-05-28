package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.CleanLogMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.CleanLogService;
import com.jeez.zp.platform.vo.CleanLogPageDataVO;
import com.jeez.zp.platform.vo.CleanLogRowVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CleanLogServiceImpl implements CleanLogService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");

    private final CleanLogMapper cleanLogMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public CleanLogPageDataVO getPage(
            Long campId,
            Long userId,
            Long poiId,
            List<String> roomIds,
            Long operatorId,
            Long operatorStartTime,
            Long operatorEndTime,
            Integer pageNum,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(pageNum);
        int resolvedPageSize = normalizePageSize(pageSize);

        List<CleanLogRowVO> rows = cleanLogMapper.selectRows(
                resolvedCampId,
                poiId,
                parseLongList(roomIds),
                operatorId,
                toLocalDateTime(operatorStartTime),
                toEndExclusiveTime(operatorEndTime)
        );
        PageSlice<CleanLogRowVO> pageSlice = pageSlice(rows, resolvedPageNum, resolvedPageSize);

        CleanLogPageDataVO response = new CleanLogPageDataVO();
        response.setTotal(pageSlice.total());
        response.setList(pageSlice.items());
        return response;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private List<Long> parseLongList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        List<Long> parsed = values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(Long::valueOf)
                .distinct()
                .toList();
        return parsed.isEmpty() ? null : parsed;
    }

    private LocalDateTime toLocalDateTime(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        return Instant.ofEpochMilli(epochMillis).atZone(SHANGHAI_ZONE).toLocalDateTime();
    }

    private LocalDateTime toEndExclusiveTime(Long epochMillis) {
        if (epochMillis == null) {
            return null;
        }
        LocalDate localDate = Instant.ofEpochMilli(epochMillis).atZone(SHANGHAI_ZONE).toLocalDate();
        return localDate.plusDays(1).atStartOfDay();
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
            throw new BusinessException(40301, "无权访问当前门店保洁日志");
        }
        return requestedCampId;
    }

    private <T> PageSlice<T> pageSlice(List<T> items, int pageNum, int pageSize) {
        long total = items.size();
        int fromIndex = Math.min((pageNum - 1) * pageSize, items.size());
        int toIndex = Math.min(fromIndex + pageSize, items.size());
        return new PageSlice<>(total, items.subList(fromIndex, toIndex));
    }

    private record PageSlice<T>(long total, List<T> items) {
    }
}
