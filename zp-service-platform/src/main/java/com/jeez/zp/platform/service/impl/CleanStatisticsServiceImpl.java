package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.CleanTaskMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.CleanStatisticsService;
import com.jeez.zp.platform.vo.CleanTaskOptionVO;
import com.jeez.zp.platform.vo.CleanTaskQueryRowVO;
import com.jeez.zp.platform.vo.CleanTaskStatisticsResponseVO;
import com.jeez.zp.platform.vo.CleanTaskStatisticsRowVO;
import com.jeez.zp.platform.vo.CleanerListItemVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CleanStatisticsServiceImpl implements CleanStatisticsService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final long FEE_TYPE_ONE = 3600L;
    private static final long FEE_TYPE_TWO = 6100L;
    private static final long FEE_TYPE_THREE = 6600L;
    private static final long FEE_TYPE_FOUR = 7600L;

    private final CleanTaskMapper cleanTaskMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public CleanTaskStatisticsResponseVO getStatistics(
            Long campId,
            Long userId,
            Long storeId,
            List<String> roomIds,
            List<String> cleanerIds,
            Long cleanStartTime,
            Long cleanEndTime,
            Integer pageNum,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = normalizePageNum(pageNum);
        int resolvedPageSize = normalizePageSize(pageSize);

        List<CleanTaskQueryRowVO> rows = cleanTaskMapper.selectTaskRowsByRange(
                resolvedCampId,
                storeId,
                parseLongList(roomIds),
                parseLongList(cleanerIds),
                toLocalDateTime(cleanStartTime),
                toLocalDateTime(cleanEndTime)
        );

        List<CleanTaskStatisticsRowVO> aggregatedRows = aggregateRows(rows);
        PageSlice<CleanTaskStatisticsRowVO> pageSlice = pageSlice(aggregatedRows, resolvedPageNum, resolvedPageSize);

        CleanTaskStatisticsResponseVO response = new CleanTaskStatisticsResponseVO();
        response.setTotal(pageSlice.total());
        response.setPageNum(resolvedPageNum);
        response.setCurrent(resolvedPageNum);
        response.setSize(resolvedPageSize);
        response.setList(pageSlice.items());
        return response;
    }

    @Override
    public List<CleanerListItemVO> getCleaners(Long campId, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        return cleanTaskMapper.selectCleaners(resolvedCampId).stream()
                .map(this::toCleaner)
                .toList();
    }

    private CleanerListItemVO toCleaner(CleanTaskOptionVO option) {
        CleanerListItemVO cleaner = new CleanerListItemVO();
        cleaner.setCleanerId(option.getId());
        cleaner.setCleanerName(option.getLabel());
        return cleaner;
    }

    private List<CleanTaskStatisticsRowVO> aggregateRows(List<CleanTaskQueryRowVO> rows) {
        Map<String, MutableStatisticsRow> grouped = new LinkedHashMap<>();
        for (CleanTaskQueryRowVO row : rows) {
            LocalDateTime deadlineAt = row.getDeadlineAt();
            if (deadlineAt == null) {
                continue;
            }
            String date = deadlineAt.toLocalDate().format(DATE_FORMATTER);
            grouped.computeIfAbsent(date, MutableStatisticsRow::new).accept(row.getTaskType());
        }

        List<CleanTaskStatisticsRowVO> result = new ArrayList<>();
        MutableStatisticsRow totalRow = new MutableStatisticsRow("合计");
        for (MutableStatisticsRow row : grouped.values()) {
            totalRow.merge(row);
        }
        result.add(totalRow.toView());
        grouped.values().forEach(row -> result.add(row.toView()));
        return result;
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

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "无权访问当前门店保洁统计");
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

    private static final class MutableStatisticsRow {

        private final String cleanTime;
        private long countNum;
        private long countCost;
        private long cleanTypeOneNum;
        private long cleanTypeOneCost;
        private long cleanTypeTwoNum;
        private long cleanTypeTwoCost;
        private long cleanTypeThreeNum;
        private long cleanTypeThreeCost;
        private long cleanTypeFourNum;
        private long cleanTypeFourCost;

        private MutableStatisticsRow(String cleanTime) {
            this.cleanTime = cleanTime;
        }

        private void accept(String taskType) {
            countNum += 1;
            switch (taskType) {
                case "night_clean" -> {
                    cleanTypeOneNum += 1;
                    cleanTypeOneCost += FEE_TYPE_ONE;
                    countCost += FEE_TYPE_ONE;
                }
                case "daily_clean" -> {
                    cleanTypeTwoNum += 1;
                    cleanTypeTwoCost += FEE_TYPE_TWO;
                    countCost += FEE_TYPE_TWO;
                }
                case "checkout_clean" -> {
                    cleanTypeThreeNum += 1;
                    cleanTypeThreeCost += FEE_TYPE_THREE;
                    countCost += FEE_TYPE_THREE;
                }
                default -> {
                    cleanTypeFourNum += 1;
                    cleanTypeFourCost += FEE_TYPE_FOUR;
                    countCost += FEE_TYPE_FOUR;
                }
            }
        }

        private void merge(MutableStatisticsRow row) {
            countNum += row.countNum;
            countCost += row.countCost;
            cleanTypeOneNum += row.cleanTypeOneNum;
            cleanTypeOneCost += row.cleanTypeOneCost;
            cleanTypeTwoNum += row.cleanTypeTwoNum;
            cleanTypeTwoCost += row.cleanTypeTwoCost;
            cleanTypeThreeNum += row.cleanTypeThreeNum;
            cleanTypeThreeCost += row.cleanTypeThreeCost;
            cleanTypeFourNum += row.cleanTypeFourNum;
            cleanTypeFourCost += row.cleanTypeFourCost;
        }

        private CleanTaskStatisticsRowVO toView() {
            CleanTaskStatisticsRowVO row = new CleanTaskStatisticsRowVO();
            row.setCleanTime(cleanTime);
            row.setCountNum(countNum);
            row.setCountCost(countCost);
            row.setCleanTypeOneNum(cleanTypeOneNum);
            row.setCleanTypeOneCost(cleanTypeOneCost);
            row.setCleanTypeTwoNum(cleanTypeTwoNum);
            row.setCleanTypeTwoCost(cleanTypeTwoCost);
            row.setCleanTypeThreeNum(cleanTypeThreeNum);
            row.setCleanTypeThreeCost(cleanTypeThreeCost);
            row.setCleanTypeFourNum(cleanTypeFourNum);
            row.setCleanTypeFourCost(cleanTypeFourCost);
            return row;
        }
    }
}
