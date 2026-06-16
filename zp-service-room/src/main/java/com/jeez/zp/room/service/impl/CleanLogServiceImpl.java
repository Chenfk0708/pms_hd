package com.jeez.zp.room.service.impl;

import com.jeez.zp.room.exception.BusinessException;
import com.jeez.zp.room.mapper.CleanLogMapper;
import com.jeez.zp.room.mapper.UserCampMapper;
import com.jeez.zp.room.service.CleanLogService;
import com.jeez.zp.room.vo.CleanLogExportResponseVO;
import com.jeez.zp.room.vo.CleanLogFilterOptionsVO;
import com.jeez.zp.room.vo.CleanLogOptionVO;
import com.jeez.zp.room.vo.CleanLogPageDataVO;
import com.jeez.zp.room.vo.CleanLogPaginationVO;
import com.jeez.zp.room.vo.CleanLogRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CleanLogServiceImpl implements CleanLogService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CleanLogMapper cleanLogMapper;
    private final UserCampMapper userCampMapper;

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
        List<CleanLogRowVO> rows = selectRows(resolvedCampId, poiId, roomIds, operatorId, operatorStartTime, operatorEndTime);
        PageSlice<CleanLogRowVO> pageSlice = pageSlice(rows, normalizePageNum(pageNum), normalizePageSize(pageSize));

        CleanLogPageDataVO response = new CleanLogPageDataVO();
        response.setTotal(pageSlice.total());
        response.setList(pageSlice.items());
        response.setPagination(toPagination(pageSlice.total(), normalizePageNum(pageNum), normalizePageSize(pageSize)));
        response.setDictionaries(buildFilterOptions(resolvedCampId));
        return response;
    }

    @Override
    public CleanLogExportResponseVO export(
            Long campId,
            Long userId,
            Long poiId,
            List<String> roomIds,
            Long operatorId,
            Long operatorStartTime,
            Long operatorEndTime
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        List<CleanLogRowVO> rows = selectRows(resolvedCampId, poiId, roomIds, operatorId, operatorStartTime, operatorEndTime);
        CleanLogExportResponseVO response = new CleanLogExportResponseVO();
        response.setFileName("clean_logs_" + resolveExportDate(operatorStartTime) + ".csv");
        response.setContentType("text/csv");
        response.setTotal(rows.size());
        response.setRows(rows);
        return response;
    }

    private List<CleanLogRowVO> selectRows(
            Long resolvedCampId,
            Long poiId,
            List<String> roomIds,
            Long operatorId,
            Long operatorStartTime,
            Long operatorEndTime
    ) {
        return cleanLogMapper.selectRows(
                resolvedCampId,
                poiId,
                parseLongList(roomIds),
                operatorId,
                toLocalDateTime(operatorStartTime),
                toEndExclusiveTime(operatorEndTime)
        );
    }

    private CleanLogFilterOptionsVO buildFilterOptions(Long campId) {
        CleanLogFilterOptionsVO options = new CleanLogFilterOptionsVO();
        options.setStores(buildStores(campId));
        options.setRooms(cleanLogMapper.selectRooms(campId));
        options.setOperators(cleanLogMapper.selectOperators(campId));
        return options;
    }

    private List<CleanLogOptionVO> buildStores(Long campId) {
        List<CleanLogOptionVO> stores = new ArrayList<>();
        CleanLogOptionVO all = new CleanLogOptionVO();
        all.setLabel("全部门店");
        all.setValue("");
        stores.add(all);
        stores.addAll(cleanLogMapper.selectStores(campId));
        return stores;
    }

    private CleanLogPaginationVO toPagination(long total, int pageNum, int pageSize) {
        CleanLogPaginationVO pagination = new CleanLogPaginationVO();
        pagination.setPage(pageNum);
        pagination.setPageSize(pageSize);
        pagination.setTotal(total);
        return pagination;
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

    private String resolveExportDate(Long operatorStartTime) {
        if (operatorStartTime == null) {
            return LocalDate.now(SHANGHAI_ZONE).format(FILE_DATE_FORMATTER);
        }
        return Instant.ofEpochMilli(operatorStartTime).atZone(SHANGHAI_ZONE).toLocalDate().format(FILE_DATE_FORMATTER);
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        Long currentCampId = userCampMapper.selectCurrentCampId(userId);
        if (currentCampId == null) {
            throw new BusinessException(40401, "未找到当前用户门店");
        }
        if (requestedCampId == null) {
            return currentCampId;
        }
        if (!requestedCampId.equals(currentCampId)) {
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
