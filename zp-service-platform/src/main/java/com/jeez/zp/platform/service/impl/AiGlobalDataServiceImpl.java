package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.AiGlobalDataMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.AiGlobalDataService;
import com.jeez.zp.platform.vo.AiGlobalReminderItemVO;
import com.jeez.zp.platform.vo.AiGlobalReminderPageResponseVO;
import com.jeez.zp.platform.vo.AiGlobalReminderPaginationVO;
import com.jeez.zp.platform.vo.AiGlobalShopStatusVO;
import com.jeez.zp.platform.vo.AiGlobalStrongReminderRowVO;
import com.jeez.zp.platform.vo.ChannelVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiGlobalDataServiceImpl implements AiGlobalDataService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PlatformBootstrapMapper platformBootstrapMapper;
    private final AiGlobalDataMapper aiGlobalDataMapper;

    @Override
    public AiGlobalReminderPageResponseVO getStrongReminderPage(
            Long campId,
            Long userId,
            Integer page,
            Integer pageNum,
            Integer current,
            Integer pageSize
    ) {
        CurrentUserBundleVO bundle = requireCurrentUserBundle(userId);
        Long resolvedCampId = resolveAccessibleCampId(campId, bundle);
        int resolvedPageNum = normalizePageNum(page, pageNum, current);
        int resolvedPageSize = normalizePageSize(pageSize);

        List<AiGlobalReminderItemVO> items = aiGlobalDataMapper.selectStrongReminderRows(resolvedCampId).stream()
                .map(this::toReminderItem)
                .toList();

        int fromIndex = Math.min((resolvedPageNum - 1) * resolvedPageSize, items.size());
        int toIndex = Math.min(fromIndex + resolvedPageSize, items.size());

        AiGlobalReminderPaginationVO pagination = new AiGlobalReminderPaginationVO();
        pagination.setPageNum(resolvedPageNum);
        pagination.setPageSize(resolvedPageSize);
        pagination.setTotal((long) items.size());

        AiGlobalReminderPageResponseVO response = new AiGlobalReminderPageResponseVO();
        response.setList(items.subList(fromIndex, toIndex));
        response.setPagination(pagination);
        return response;
    }

    @Override
    public List<AiGlobalShopStatusVO> getShopStatuses(Long campId, Long userId, Integer status) {
        CurrentUserBundleVO bundle = requireCurrentUserBundle(userId);
        Long resolvedCampId = resolveAccessibleCampId(campId, bundle);

        Map<Long, ChannelVO> deduplicatedChannels = new LinkedHashMap<>();
        for (ChannelVO channel : platformBootstrapMapper.selectChannelsByCampId(resolvedCampId)) {
            if (channel.getAccountId() == null) {
                continue;
            }
            deduplicatedChannels.merge(channel.getAccountId(), channel, this::mergeChannel);
        }

        boolean hasAuthorizedChannel = deduplicatedChannels.values().stream().anyMatch(channel -> isAuthorized(channel.getStatus()));
        boolean hasDelayedChannel = deduplicatedChannels.values().stream()
                .filter(channel -> isAuthorized(channel.getStatus()))
                .anyMatch(channel -> isDelayed(channel.getSyncStatus()));

        LinkedHashSet<String> authorizedChannels = deduplicatedChannels.values().stream()
                .filter(channel -> isAuthorized(channel.getStatus()))
                .map(ChannelVO::getChannelName)
                .filter(this::hasText)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        AiGlobalShopStatusVO item = new AiGlobalShopStatusVO();
        item.setId(String.valueOf(resolvedCampId));
        item.setCampId(String.valueOf(resolvedCampId));
        item.setName(hasText(bundle.getCampName()) ? bundle.getCampName() : "当前门店");
        item.setConnectorStatus(hasAuthorizedChannel ? (hasDelayedChannel ? "warning" : "online") : "offline");
        item.setRadarStatus(hasAuthorizedChannel ? (hasDelayedChannel ? "delay" : "running") : "setup");
        item.setAuthorizedChannels(new ArrayList<>(authorizedChannels));
        item.setUpdatedAt(LocalDateTime.now(SHANGHAI_ZONE).format(TIMESTAMP_FORMATTER));
        return List.of(item);
    }

    private CurrentUserBundleVO requireCurrentUserBundle(Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "current user context not found");
        }
        return bundle;
    }

    private Long resolveAccessibleCampId(Long requestedCampId, CurrentUserBundleVO bundle) {
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!requestedCampId.equals(bundle.getCampId())) {
            throw new BusinessException(40301, "no access to current camp ai global data");
        }
        return requestedCampId;
    }

    private int normalizePageNum(Integer page, Integer pageNum, Integer current) {
        Integer candidate = page != null ? page : (pageNum != null ? pageNum : current);
        return candidate == null || candidate < 1 ? DEFAULT_PAGE_NUM : candidate;
    }

    private int normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    private AiGlobalReminderItemVO toReminderItem(AiGlobalStrongReminderRowVO row) {
        AiGlobalReminderItemVO item = new AiGlobalReminderItemVO();
        item.setId(defaultString(row.getOrderId(), ""));
        item.setCampId(String.valueOf(row.getCampId()));
        item.setLevel(resolveReminderLevel(row.getOrderStatus()));
        item.setTitle(resolveReminderTitle(row.getOrderStatus()));
        item.setGuestName(defaultString(row.getGuestName(), "客人"));
        item.setRoomName(firstNonBlank(row.getRoomName(), row.getRoomCategoryName(), "未排房"));
        item.setOrderNo(firstNonBlank(row.getOutOrderNo(), row.getOrderNo(), row.getOrderId(), ""));
        item.setDueAt(resolveDueAt(row));
        item.setChannel(resolveReminderChannel(row));
        item.setStatus("pending");
        item.setPrimaryAction("refunding".equalsIgnoreCase(defaultString(row.getOrderStatus(), "")) ? "status" : "order");
        item.setSummary(resolveReminderSummary(row.getOrderStatus(), item.getRoomName()));
        return item;
    }

    private String resolveReminderLevel(String orderStatus) {
        return "pending".equalsIgnoreCase(defaultString(orderStatus, "")) ? "high" : "medium";
    }

    private String resolveReminderTitle(String orderStatus) {
        return switch (defaultString(orderStatus, "").toLowerCase(Locale.ROOT)) {
            case "pending" -> "待处理提醒";
            case "refunding" -> "退款跟进";
            default -> "入住校验";
        };
    }

    private String resolveReminderSummary(String orderStatus, String roomName) {
        return switch (defaultString(orderStatus, "").toLowerCase(Locale.ROOT)) {
            case "pending" -> "订单仍处于待确认状态，请尽快跟进客人并完成接单。";
            case "refunding" -> "订单处于退款处理中，请检查退款进度与客诉情况。";
            default -> roomName + " 临近入住，请核验房态并确认接待安排。";
        };
    }

    private String resolveDueAt(AiGlobalStrongReminderRowVO row) {
        LocalDateTime time = row.getCreatedAt() != null ? row.getCreatedAt() : row.getStartAt();
        if (time == null) {
            time = LocalDateTime.now(SHANGHAI_ZONE);
        }
        return time.format(TIME_FORMATTER);
    }

    private String resolveReminderChannel(AiGlobalStrongReminderRowVO row) {
        String channelName = defaultString(row.getChannelName(), "").toLowerCase(Locale.ROOT);
        if (channelName.contains("meituan") || channelName.contains("美团")) {
            return "meituan";
        }
        if (channelName.contains("ctrip") || channelName.contains("携程")) {
            return "ctrip";
        }
        return row.getChannelId() != null && row.getChannelId() == 7L ? "meituan" : "ctrip";
    }

    private ChannelVO mergeChannel(ChannelVO current, ChannelVO incoming) {
        ChannelVO merged = new ChannelVO();
        merged.setAccountId(current.getAccountId());
        merged.setChannelId(current.getChannelId() != null ? current.getChannelId() : incoming.getChannelId());
        merged.setChannelName(hasText(current.getChannelName()) ? current.getChannelName() : incoming.getChannelName());
        merged.setAccountName(hasText(current.getAccountName()) ? current.getAccountName() : incoming.getAccountName());
        merged.setPoiId(current.getPoiId() != null ? current.getPoiId() : incoming.getPoiId());
        merged.setSyncStatus(pickSyncStatus(current.getSyncStatus(), incoming.getSyncStatus()));
        merged.setStatus(pickStatus(current.getStatus(), incoming.getStatus()));
        return merged;
    }

    private String pickSyncStatus(String current, String incoming) {
        if (isDelayed(current) || isDelayed(incoming)) {
            return isDelayed(current) ? current : incoming;
        }
        if (hasText(current)) {
            return current;
        }
        return incoming;
    }

    private String pickStatus(String current, String incoming) {
        if (isAuthorized(current) || isAuthorized(incoming)) {
            return isAuthorized(current) ? current : incoming;
        }
        if (hasText(current)) {
            return current;
        }
        return incoming;
    }

    private boolean isAuthorized(String status) {
        return "authorized".equalsIgnoreCase(defaultString(status, ""));
    }

    private boolean isDelayed(String syncStatus) {
        if (!hasText(syncStatus)) {
            return false;
        }
        String normalized = syncStatus.trim().toLowerCase(Locale.ROOT);
        return !List.of("synced", "success", "authorized", "running").contains(normalized);
    }

    private String defaultString(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
