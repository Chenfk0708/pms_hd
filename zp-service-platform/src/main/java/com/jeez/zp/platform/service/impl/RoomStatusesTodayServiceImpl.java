package com.jeez.zp.platform.service.impl;

import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.RoomStatusesTodayMapper;
import com.jeez.zp.platform.service.RoomStatusesTodayService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.RoomStatusesTodayBasicVO;
import com.jeez.zp.platform.vo.RoomStatusesTodayCategoryVO;
import com.jeez.zp.platform.vo.RoomStatusesTodayFloorVO;
import com.jeez.zp.platform.vo.RoomStatusesTodayOrderVO;
import com.jeez.zp.platform.vo.RoomStatusesTodayResponseVO;
import com.jeez.zp.platform.vo.RoomStatusesTodayRoomVO;
import com.jeez.zp.platform.vo.RoomStatusesTodayRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoomStatusesTodayServiceImpl implements RoomStatusesTodayService {

    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final int VIEW_BY_ROOM_CATEGORY = 1;
    private static final int VIEW_BY_ROOM = 2;
    private static final int VIEW_BY_FLOOR = 3;

    private final RoomStatusesTodayMapper roomStatusesTodayMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public RoomStatusesTodayResponseVO getRoomStatusesToday(
            Long campId,
            Long userId,
            List<Long> channelIds,
            List<Long> roomCategoryGroupIds,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Object cleanStatus,
            Object date,
            Object queryCode,
            String storeId,
            String keyword,
            String viewMode,
            List<String> statusFilters,
            String channel,
            String roomType,
            String tag
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        NormalizedQuery query = normalizeQuery(
                channelIds,
                roomCategoryGroupIds,
                roomCategoryIds,
                poiIds,
                cleanStatus,
                date,
                queryCode,
                storeId,
                keyword,
                viewMode,
                statusFilters,
                channel,
                roomType,
                tag
        );

        List<RoomStatusesTodayRowVO> filteredRows = roomStatusesTodayMapper.selectRows(
                        resolvedCampId,
                        query.dayStart(),
                        query.nextDayStart(),
                        query.roomCategoryGroupIds(),
                        query.roomCategoryIds(),
                        query.poiIds()
                ).stream()
                .filter(row -> matches(row, query))
                .toList();

        RoomStatusesTodayResponseVO response = new RoomStatusesTodayResponseVO();
        response.setBasic(buildBasic(filteredRows, query.targetDate()));
        response.setRoomCategories(List.of());
        response.setRoomViews(List.of());
        response.setFloorViews(List.of());
        response.setIsInitFloor(null);

        if (query.viewCode() == VIEW_BY_ROOM_CATEGORY) {
            response.setRoomCategories(buildRoomCategories(filteredRows, query.targetDate()));
            return response;
        }

        if (query.viewCode() == VIEW_BY_ROOM) {
            response.setRoomViews(buildRoomViews(filteredRows, query.targetDate()));
            return response;
        }

        response.setFloorViews(buildFloorViews(filteredRows, query.targetDate()));
        response.setIsInitFloor(hasFloor(filteredRows) ? 1 : 0);
        return response;
    }

    private NormalizedQuery normalizeQuery(
            List<Long> channelIds,
            List<Long> roomCategoryGroupIds,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            Object cleanStatus,
            Object date,
            Object queryCode,
            String storeId,
            String keyword,
            String viewMode,
            List<String> statusFilters,
            String channel,
            String roomType,
            String tag
    ) {
        LocalDate targetDate = resolveTargetDate(date);
        LocalDateTime dayStart = targetDate.atStartOfDay();
        LocalDateTime nextDayStart = targetDate.plusDays(1).atStartOfDay();
        Integer viewCode = resolveViewCode(queryCode, viewMode);
        String normalizedKeyword = trimToNull(keyword);
        if (normalizedKeyword == null) {
            String queryCodeText = trimToNull(queryCode == null ? null : String.valueOf(queryCode));
            if (queryCodeText != null && !isViewCode(queryCodeText)) {
                normalizedKeyword = queryCodeText;
            }
        }

        List<Long> normalizedPoiIds = normalizeIds(poiIds);
        if (normalizedPoiIds.isEmpty()) {
            Long storePoiId = parseLongOrNull(storeId);
            if (storePoiId != null) {
                normalizedPoiIds = List.of(storePoiId);
            }
        }

        List<Long> normalizedChannelIds = normalizeIds(channelIds);
        String normalizedChannel = trimToNull(channel);
        if (normalizedChannelIds.isEmpty()) {
            Long channelId = parseLongOrNull(normalizedChannel);
            if (channelId != null) {
                normalizedChannelIds = List.of(channelId);
                normalizedChannel = null;
            }
        }

        return new NormalizedQuery(
                targetDate,
                dayStart,
                nextDayStart,
                viewCode,
                normalizeIds(roomCategoryGroupIds),
                normalizeIds(roomCategoryIds),
                normalizedPoiIds,
                normalizedChannelIds,
                resolveCleanState(cleanStatus),
                normalizeStatusFilters(statusFilters),
                normalizedKeyword,
                normalizedChannel,
                trimToNull(roomType),
                "remark".equalsIgnoreCase(trimToNull(tag))
        );
    }

    private boolean matches(RoomStatusesTodayRowVO row, NormalizedQuery query) {
        DerivedRoomState state = deriveState(row, query.targetDate());

        if (query.cleanState() != null) {
            boolean isDirty = state.isDirty() == 1;
            if (query.cleanState() == CleanState.CLEAN && isDirty) {
                return false;
            }
            if (query.cleanState() == CleanState.DIRTY && !isDirty) {
                return false;
            }
        }

        if (!query.channelIds().isEmpty()) {
            if (row.getChannelId() == null || !query.channelIds().contains(Long.valueOf(row.getChannelId()))) {
                return false;
            }
        }

        if (query.channelKeyword() != null) {
            String normalizedChannelKeyword = query.channelKeyword().toLowerCase(Locale.ROOT);
            if ("direct".equals(normalizedChannelKeyword)) {
                if (row.getChannelId() != null && !row.getChannelId().isBlank()) {
                    return false;
                }
            } else if ("ota".equals(normalizedChannelKeyword)) {
                if (row.getChannelId() == null || row.getChannelId().isBlank()) {
                    return false;
                }
            } else if (!containsIgnoreCase(row.getChannelName(), query.channelKeyword())) {
                return false;
            }
        }

        if (query.roomTypeKeyword() != null && !containsIgnoreCase(row.getRoomCategoryName(), query.roomTypeKeyword())) {
            return false;
        }

        if (query.remarkOnly() && state.isOrderRemark() != 1) {
            return false;
        }

        if (query.keyword() != null
                && !containsIgnoreCase(row.getRoomName(), query.keyword())
                && !containsIgnoreCase(row.getRoomCategoryName(), query.keyword())
                && !containsIgnoreCase(row.getGuestName(), query.keyword())
                && !containsIgnoreCase(row.getChannelName(), query.keyword())) {
            return false;
        }

        if (query.statusFilters().isEmpty()) {
            return true;
        }

        Set<String> labels = state.labels();
        return query.statusFilters().stream().anyMatch(labels::contains);
    }

    private RoomStatusesTodayBasicVO buildBasic(List<RoomStatusesTodayRowVO> rows, LocalDate targetDate) {
        RoomStatusesTodayBasicVO basic = new RoomStatusesTodayBasicVO();
        int preComeNum = 0;
        int liveNum = 0;
        int preLeaveNum = 0;
        int arrangeSameRoomNum = 0;
        int orderRemarkNum = 0;
        int ltNum = 0;
        int debtNum = 0;
        int hourRoomOrderNum = 0;
        int roomNum = 0;
        int soldNum = 0;
        int idleNum = 0;
        int occNum = 0;
        int idleCleanNum = 0;
        int idleDirtyNum = 0;
        int liveCleanNum = 0;
        int liveDirtyNum = 0;
        int extendStayNum = 0;

        for (RoomStatusesTodayRowVO row : rows) {
            DerivedRoomState state = deriveState(row, targetDate);
            roomNum++;
            preComeNum += state.isPreCome();
            liveNum += state.isLive();
            preLeaveNum += state.isPreLeave();
            arrangeSameRoomNum += state.isArrangeSameRoom();
            orderRemarkNum += state.isOrderRemark();
            ltNum += state.isLt();
            debtNum += state.isDebt();
            hourRoomOrderNum += state.isHourRoomOrder();
            soldNum += state.soldCount();
            idleNum += state.isIdle();
            occNum += state.isOcc();
            idleCleanNum += state.idleCleanCount();
            idleDirtyNum += state.idleDirtyCount();
            liveCleanNum += state.liveCleanCount();
            liveDirtyNum += state.liveDirtyCount();
            extendStayNum += state.isExtendStay();
        }

        basic.setPreComeNum(preComeNum);
        basic.setLiveNum(liveNum);
        basic.setPreLeaveNum(preLeaveNum);
        basic.setArrangeSameRoomNum(arrangeSameRoomNum);
        basic.setOrderRemarkNum(orderRemarkNum);
        basic.setLtNum(ltNum);
        basic.setDebtNum(debtNum);
        basic.setHourRoomOrderNum(hourRoomOrderNum);
        basic.setRoomNum(roomNum);
        basic.setSoldNum(soldNum);
        basic.setIdleNum(idleNum);
        basic.setOccNum(occNum);
        basic.setOcc(0);
        basic.setIdleCleanNum(idleCleanNum);
        basic.setIdleDirtyNum(idleDirtyNum);
        basic.setLiveCleanNum(liveCleanNum);
        basic.setLiveDirtyNum(liveDirtyNum);
        basic.setExtendStayNum(extendStayNum);
        return basic;
    }

    private List<RoomStatusesTodayCategoryVO> buildRoomCategories(List<RoomStatusesTodayRowVO> rows, LocalDate targetDate) {
        Map<String, RoomStatusesTodayCategoryVO> categories = new LinkedHashMap<>();
        for (RoomStatusesTodayRowVO row : rows) {
            RoomStatusesTodayCategoryVO category = categories.computeIfAbsent(row.getRoomCategoryId(), ignored -> {
                RoomStatusesTodayCategoryVO item = new RoomStatusesTodayCategoryVO();
                item.setRoomCategoryId(row.getRoomCategoryId());
                item.setRoomCategoryName(row.getRoomCategoryName());
                item.setRoomCategorySeq(row.getRoomCategorySeq());
                item.setRooms(new ArrayList<>());
                item.setRoomNum(0);
                item.setSoldNum(0);
                item.setLiveNum(0);
                item.setIdleNum(0);
                item.setOccNum(0);
                return item;
            });
            DerivedRoomState state = deriveState(row, targetDate);
            category.getRooms().add(toRoomView(row, state));
            category.setRoomNum(category.getRoomNum() + 1);
            category.setSoldNum(category.getSoldNum() + state.soldCount());
            category.setLiveNum(category.getLiveNum() + state.isLive());
            category.setIdleNum(category.getIdleNum() + state.isIdle());
            category.setOccNum(category.getOccNum() + state.isOcc());
        }
        return new ArrayList<>(categories.values());
    }

    private List<RoomStatusesTodayRoomVO> buildRoomViews(List<RoomStatusesTodayRowVO> rows, LocalDate targetDate) {
        return rows.stream()
                .map(row -> toRoomView(row, deriveState(row, targetDate)))
                .toList();
    }

    private List<RoomStatusesTodayFloorVO> buildFloorViews(List<RoomStatusesTodayRowVO> rows, LocalDate targetDate) {
        Map<String, RoomStatusesTodayFloorVO> floors = new LinkedHashMap<>();
        for (RoomStatusesTodayRowVO row : rows) {
            if (row.getFloorId() == null || row.getFloorId().isBlank()) {
                continue;
            }
            RoomStatusesTodayFloorVO floor = floors.computeIfAbsent(row.getFloorId(), ignored -> {
                RoomStatusesTodayFloorVO item = new RoomStatusesTodayFloorVO();
                item.setFloorId(row.getFloorId());
                item.setFloorName(row.getFloorName());
                item.setRooms(new ArrayList<>());
                return item;
            });
            floor.getRooms().add(toRoomView(row, deriveState(row, targetDate)));
        }
        return new ArrayList<>(floors.values());
    }

    private RoomStatusesTodayRoomVO toRoomView(RoomStatusesTodayRowVO row, DerivedRoomState state) {
        RoomStatusesTodayRoomVO roomView = new RoomStatusesTodayRoomVO();
        roomView.setRoomId(row.getRoomId());
        roomView.setRoomName(row.getRoomName());
        roomView.setRoomSeq(row.getRoomSeq());
        roomView.setIsDirty(state.isDirty());
        roomView.setRoomCategoryId(row.getRoomCategoryId());
        roomView.setRoomCategoryName(row.getRoomCategoryName());
        roomView.setFloorId(row.getFloorId());
        roomView.setIsOcc(state.isOcc());
        roomView.setIsLive(state.isLive());
        roomView.setIsIdle(state.isIdle());
        roomView.setIsPreCome(state.isPreCome());
        roomView.setIsPreLeave(state.isPreLeave());
        roomView.setIsLinkCardDevice(0);
        roomView.setIsLinkPasswordDevice(0);
        roomView.setPasswordDeviceId(null);
        roomView.setIsArrangeSameRoom(state.isArrangeSameRoom());
        roomView.setIsOrderRemark(state.isOrderRemark());
        roomView.setIsLt(state.isLt());
        roomView.setIsDebt(state.isDebt());
        roomView.setIsHourRoomOrder(state.isHourRoomOrder());
        roomView.setIsExtendStay(state.isExtendStay());
        roomView.setIsInvitationExtendStay(0);
        roomView.setOccupationType(state.occupationType());
        roomView.setOccupationRemark(row.getOrderRemark() == null ? "" : row.getOrderRemark());
        roomView.setGuestName(row.getGuestName() == null ? "" : row.getGuestName());
        roomView.setOrders(buildOrders(row));
        roomView.setRoomCategorySeq(row.getRoomCategorySeq());
        return roomView;
    }

    private List<RoomStatusesTodayOrderVO> buildOrders(RoomStatusesTodayRowVO row) {
        if (row.getOrderId() == null || row.getOrderId().isBlank()) {
            return List.of();
        }
        RoomStatusesTodayOrderVO order = new RoomStatusesTodayOrderVO();
        order.setOrderId(row.getOrderId());
        order.setChannelId(row.getChannelId());
        order.setChannelName(row.getChannelName());
        order.setGuestName(row.getGuestName());
        order.setGuestMobile(row.getGuestMobile());
        order.setStatus(row.getOrderStatus());
        order.setRemark(row.getOrderRemark());
        order.setCheckInDate(toEpochMilli(row.getCheckInAt()));
        order.setCheckOutDate(toEpochMilli(row.getCheckOutAt()));
        order.setTotalPriceCent(row.getTotalPriceCent());
        order.setTotalPayPriceCent(row.getTotalPayPriceCent());
        return List.of(order);
    }

    private DerivedRoomState deriveState(RoomStatusesTodayRowVO row, LocalDate targetDate) {
        int isDirty = "dirty".equalsIgnoreCase(row.getCleanStatus()) ? 1 : 0;
        boolean hasOrder = row.getOrderId() != null && !row.getOrderId().isBlank();
        boolean isCheckedIn = hasOrder && "checked_in".equalsIgnoreCase(row.getOrderStatus());
        boolean isBooked = hasOrder && ("booked".equalsIgnoreCase(row.getOrderStatus()) || "pending".equalsIgnoreCase(row.getOrderStatus()));
        int isPreCome = isBooked && isSameDate(row.getCheckInAt(), targetDate) ? 1 : 0;
        int isPreLeave = isCheckedIn && isSameDate(row.getCheckOutAt(), targetDate) ? 1 : 0;
        int isLive = isCheckedIn ? 1 : 0;
        int isOcc = hasOrder ? 1 : 0;
        int isIdle = hasOrder ? 0 : 1;
        int isOrderRemark = row.getOrderRemark() == null || row.getOrderRemark().isBlank() ? 0 : 1;
        int isDebt = hasOrder && row.getPaymentStatus() != null && !"paid".equalsIgnoreCase(row.getPaymentStatus()) ? 1 : 0;
        int isHourRoomOrder = "hourly".equalsIgnoreCase(row.getSaleType()) ? 1 : 0;
        int isExtendStay = row.getOrderRemark() != null && row.getOrderRemark().contains("续住") ? 1 : 0;
        Set<String> labels = new LinkedHashSet<>();
        if (isPreCome == 1) {
            labels.add("预抵");
        }
        if (isLive == 1) {
            labels.add("在住");
        }
        if (isPreLeave == 1) {
            labels.add("预离");
        }
        if (isIdle == 1 && isDirty == 0) {
            labels.add("空净");
        }
        if (isIdle == 1 && isDirty == 1) {
            labels.add("空脏");
        }
        if (isOcc == 1 && isDirty == 0) {
            labels.add("住净");
        }
        if (isOcc == 1 && isDirty == 1) {
            labels.add("住脏");
        }
        if (isOrderRemark == 1) {
            labels.add("备注");
        }
        return new DerivedRoomState(
                isDirty,
                isOcc,
                isLive,
                isIdle,
                isPreCome,
                isPreLeave,
                0,
                isOrderRemark,
                0,
                isDebt,
                isHourRoomOrder,
                isExtendStay,
                hasOrder ? 1 : null,
                labels
        );
    }

    private boolean isSameDate(LocalDateTime value, LocalDate targetDate) {
        return value != null && value.toLocalDate().isEqual(targetDate);
    }

    private boolean hasFloor(List<RoomStatusesTodayRowVO> rows) {
        return rows.stream().anyMatch(row -> row.getFloorId() != null && !row.getFloorId().isBlank());
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
            throw new BusinessException(40301, "无权访问当前门店今日房态");
        }
        return requestedCampId;
    }

    private LocalDate resolveTargetDate(Object value) {
        if (value == null) {
            return LocalDate.now(SHANGHAI_ZONE);
        }
        if (value instanceof Number number) {
            return Instant.ofEpochMilli(number.longValue()).atZone(SHANGHAI_ZONE).toLocalDate();
        }
        String text = trimToNull(String.valueOf(value));
        if (text == null) {
            return LocalDate.now(SHANGHAI_ZONE);
        }
        if (text.matches("^-?\\d+$")) {
            return Instant.ofEpochMilli(Long.parseLong(text)).atZone(SHANGHAI_ZONE).toLocalDate();
        }
        return LocalDate.parse(text.substring(0, 10));
    }

    private Integer resolveViewCode(Object queryCode, String viewMode) {
        String normalizedViewMode = trimToNull(viewMode);
        if (normalizedViewMode != null) {
            if (normalizedViewMode.contains("房型")) {
                return VIEW_BY_ROOM_CATEGORY;
            }
            if (normalizedViewMode.contains("房间号")) {
                return VIEW_BY_ROOM;
            }
            if (normalizedViewMode.contains("楼层")) {
                return VIEW_BY_FLOOR;
            }
        }

        String queryCodeText = trimToNull(queryCode == null ? null : String.valueOf(queryCode));
        if (isViewCode(queryCodeText)) {
            return Integer.parseInt(queryCodeText);
        }
        return VIEW_BY_ROOM_CATEGORY;
    }

    private boolean isViewCode(String value) {
        return "1".equals(value) || "2".equals(value) || "3".equals(value);
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private List<String> normalizeStatusFilters(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private CleanState resolveCleanState(Object value) {
        if (value == null) {
            return null;
        }
        String normalized = trimToNull(String.valueOf(value));
        if (normalized == null) {
            return null;
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        if ("1".equals(lower) || lower.contains("dirty") || lower.contains("脏")) {
            return CleanState.DIRTY;
        }
        if ("0".equals(lower) || lower.contains("clean") || lower.contains("净")) {
            return CleanState.CLEAN;
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long parseLongOrNull(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null || !trimmed.matches("^-?\\d+$")) {
            return null;
        }
        return Long.parseLong(trimmed);
    }

    private boolean containsIgnoreCase(String text, String keyword) {
        if (text == null || keyword == null) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private Long toEpochMilli(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(SHANGHAI_ZONE).toInstant().toEpochMilli();
    }

    private enum CleanState {
        CLEAN,
        DIRTY
    }

    private record NormalizedQuery(
            LocalDate targetDate,
            LocalDateTime dayStart,
            LocalDateTime nextDayStart,
            Integer viewCode,
            List<Long> roomCategoryGroupIds,
            List<Long> roomCategoryIds,
            List<Long> poiIds,
            List<Long> channelIds,
            CleanState cleanState,
            List<String> statusFilters,
            String keyword,
            String channelKeyword,
            String roomTypeKeyword,
            boolean remarkOnly
    ) {
    }

    private record DerivedRoomState(
            int isDirty,
            int isOcc,
            int isLive,
            int isIdle,
            int isPreCome,
            int isPreLeave,
            int isArrangeSameRoom,
            int isOrderRemark,
            int isLt,
            int isDebt,
            int isHourRoomOrder,
            int isExtendStay,
            Integer occupationType,
            Set<String> labels
    ) {
        int soldCount() {
            return isOcc == 1 && isLive == 0 ? 1 : 0;
        }

        int idleCleanCount() {
            return isIdle == 1 && isDirty == 0 ? 1 : 0;
        }

        int idleDirtyCount() {
            return isIdle == 1 && isDirty == 1 ? 1 : 0;
        }

        int liveCleanCount() {
            return isLive == 1 && isDirty == 0 ? 1 : 0;
        }

        int liveDirtyCount() {
            return isLive == 1 && isDirty == 1 ? 1 : 0;
        }
    }
}
