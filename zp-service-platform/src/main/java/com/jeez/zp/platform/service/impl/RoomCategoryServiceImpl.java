package com.jeez.zp.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jeez.zp.platform.dto.request.RoomCategorySaveRequest;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.RoomCategoryMapper;
import com.jeez.zp.platform.service.RoomCategoryService;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.PoiViewVO;
import com.jeez.zp.platform.vo.RoomCategoryDetailChannelPriceRowVO;
import com.jeez.zp.platform.vo.RoomCategoryDetailChannelPriceVO;
import com.jeez.zp.platform.vo.RoomCategoryDetailResponseVO;
import com.jeez.zp.platform.vo.RoomCategoryDetailRowVO;
import com.jeez.zp.platform.vo.RoomCategoryEditDraftVO;
import com.jeez.zp.platform.vo.RoomCategoryEditFormVO;
import com.jeez.zp.platform.vo.RoomCategoryLinkageCandidateVO;
import com.jeez.zp.platform.vo.RoomCategoryLinkageResponseVO;
import com.jeez.zp.platform.vo.RoomCategoryMutationResultVO;
import com.jeez.zp.platform.vo.RoomCategoryPageItemVO;
import com.jeez.zp.platform.vo.RoomCategoryPageResponseVO;
import com.jeez.zp.platform.vo.RoomCategoryPhotoVO;
import com.jeez.zp.platform.vo.RoomCategoryProductInfoVO;
import com.jeez.zp.platform.vo.RoomCategoryRoomViewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomCategoryServiceImpl implements RoomCategoryService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int DEFAULT_SALE_TYPE = 1;
    private static final int DEFAULT_CANCEL_POLICY = 0;
    private static final int DEFAULT_BREAKFAST_COUNT = 0;
    private static final int DEFAULT_IS_PERFECT_PRODUCT = 1;
    private static final List<String> EDIT_STEPS = List.of("基础信息", "位置信息", "房型设施", "详细介绍", "照片信息");
    private static final String LINKAGE_DESCRIPTION = "设置联动关房后，当前房型关房将联动关联的房型全部关房，关联的房型任一关房，将联动当前房型关房。适用于整租/包栋场景；";

    private final RoomCategoryMapper roomCategoryMapper;
    private final PlatformBootstrapMapper platformBootstrapMapper;

    @Override
    public RoomCategoryPageResponseVO getPage(
            Long campId,
            Long userId,
            Long poiId,
            Long roomCategoryGroupId,
            String roomCategoryName,
            String keyword,
            Long channelId,
            Integer pageNum,
            Integer pageSize
    ) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        int resolvedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int resolvedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        long offset = (long) (resolvedPageNum - 1) * resolvedPageSize;

        long total = roomCategoryMapper.countPage(
                resolvedCampId,
                poiId,
                roomCategoryGroupId,
                trimToNull(roomCategoryName),
                trimToNull(keyword),
                normalizeChannelId(channelId)
        );

        List<RoomCategoryPageItemVO> items = total == 0
                ? List.of()
                : roomCategoryMapper.selectPage(
                resolvedCampId,
                poiId,
                roomCategoryGroupId,
                trimToNull(roomCategoryName),
                trimToNull(keyword),
                normalizeChannelId(channelId),
                offset,
                resolvedPageSize
        );

        hydrateNestedViews(items);

        int pages = total == 0 ? 0 : (int) Math.ceil((double) total / resolvedPageSize);

        RoomCategoryPageResponseVO response = new RoomCategoryPageResponseVO();
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

    @Override
    public RoomCategoryDetailResponseVO getDetail(Long roomCategoryId, Long userId) {
        if (roomCategoryId == null) {
            throw new BusinessException(40001, "房型ID不能为空");
        }

        Long resolvedCampId = resolveAccessibleCampId(null, userId);
        RoomCategoryDetailRowVO detailRow = roomCategoryMapper.selectDetail(resolvedCampId, roomCategoryId);
        if (detailRow == null) {
            throw new BusinessException(40404, "房型不存在");
        }

        int inventory = detailRow.getInventory() == null ? 0 : detailRow.getInventory();
        int staying = detailRow.getStaying() == null ? 0 : detailRow.getStaying();
        int pendingOrders = detailRow.getPendingOrders() == null ? 0 : detailRow.getPendingOrders();
        int occupancyRate = inventory <= 0 ? 0 : (int) Math.round((double) staying * 100 / inventory);

        List<RoomCategoryDetailChannelPriceVO> channelPrices = roomCategoryMapper
                .selectDetailChannelPriceRows(resolvedCampId, roomCategoryId)
                .stream()
                .map(this::toChannelPrice)
                .toList();

        RoomCategoryDetailResponseVO response = new RoomCategoryDetailResponseVO();
        response.setRoomName(detailRow.getRoomName());
        response.setOccupancyRate(occupancyRate);
        response.setInventory(inventory);
        response.setStaying(staying);
        response.setPendingOrders(pendingOrders);
        response.setChannelPrices(channelPrices);
        response.setGuidance(buildGuidance(detailRow.getRoomName(), inventory, staying, occupancyRate, pendingOrders, channelPrices.size()));
        return response;
    }

    @Override
    public RoomCategoryEditDraftVO getEditDetail(Long campId, Long userId, Long roomCategoryId, String mode) {
        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        boolean createMode = "create".equalsIgnoreCase(mode);
        if (!createMode && roomCategoryId == null) {
            throw new BusinessException(40001, "房型ID不能为空");
        }

        RoomCategoryPageItemVO row = createMode ? null : roomCategoryMapper.selectEditDetail(resolvedCampId, roomCategoryId);
        if (!createMode && row == null) {
            throw new BusinessException(40404, "房型不存在");
        }

        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        List<RoomCategoryPhotoVO> photos = createMode ? List.of() : roomCategoryMapper.selectPhotos(resolvedCampId, roomCategoryId);
        RoomCategoryEditDraftVO draft = new RoomCategoryEditDraftVO();
        draft.setMode(createMode ? "create" : "detail");
        draft.setTitle(createMode ? "新增房型" : "房型详情");
        draft.setSteps(EDIT_STEPS);
        draft.setForm(buildEditForm(row, bundle, photos));
        return draft;
    }

    @Override
    public RoomCategoryLinkageResponseVO getLinkage(Long campId, Long userId, Long roomCategoryId) {
        if (roomCategoryId == null) {
            throw new BusinessException(40001, "房型ID不能为空");
        }

        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        RoomCategoryPageItemVO current = roomCategoryMapper.selectEditDetail(resolvedCampId, roomCategoryId);
        if (current == null) {
            throw new BusinessException(40404, "房型不存在");
        }

        Set<Long> selectedIds = new LinkedHashSet<>(roomCategoryMapper.selectLinkedRoomCategoryIds(resolvedCampId, roomCategoryId));
        List<RoomCategoryLinkageCandidateVO> candidates = roomCategoryMapper.selectLinkageCandidates(resolvedCampId, roomCategoryId)
                .stream()
                .map(candidate -> toLinkageCandidate(candidate, selectedIds))
                .toList();

        RoomCategoryLinkageResponseVO response = new RoomCategoryLinkageResponseVO();
        response.setRoomTypeId(String.valueOf(roomCategoryId));
        response.setRoomTypeName(defaultString(current.getRoomCategoryName()));
        response.setDescription(LINKAGE_DESCRIPTION);
        response.setCandidates(candidates);
        return response;
    }

    @Override
    @Transactional
    public RoomCategoryMutationResultVO saveLinkage(Long campId, Long userId, Long roomCategoryId, List<Long> linkedRoomCategoryIds) {
        if (roomCategoryId == null) {
            throw new BusinessException(40001, "房型ID不能为空");
        }

        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        if (roomCategoryMapper.selectEditDetail(resolvedCampId, roomCategoryId) == null) {
            throw new BusinessException(40404, "房型不存在");
        }

        roomCategoryMapper.disableLinkages(resolvedCampId, roomCategoryId);
        new LinkedHashSet<>(linkedRoomCategoryIds == null ? List.of() : linkedRoomCategoryIds)
                .stream()
                .filter(Objects::nonNull)
                .filter(linkedRoomCategoryId -> !linkedRoomCategoryId.equals(roomCategoryId))
                .forEach(linkedRoomCategoryId -> roomCategoryMapper.insertLinkage(
                        IdWorker.getId(),
                        resolvedCampId,
                        roomCategoryId,
                        linkedRoomCategoryId
                ));
        return RoomCategoryMutationResultVO.of(roomCategoryId, "联动关房已更新");
    }

    @Override
    @Transactional
    public RoomCategoryMutationResultVO saveRoomCategory(Long campId, Long userId, RoomCategorySaveRequest.Form form) {
        if (form == null) {
            throw new BusinessException(40001, "房型表单不能为空");
        }

        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        Long roomCategoryId = firstLong(form.getRoomTypeId(), form.getRoomCategoryId());
        boolean createMode = roomCategoryId == null;
        if (createMode) {
            roomCategoryId = IdWorker.getId();
        }

        String roomName = firstText(form.getRoomTypeName(), form.getRoomCategoryName());
        if (roomName == null) {
            throw new BusinessException(40001, "房型名称不能为空");
        }

        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        Long poiId = firstLong(form.getStoreId(), form.getPoiId());
        if (poiId == null) {
            poiId = bundle == null ? null : bundle.getPoiId();
        }
        if (poiId == null) {
            throw new BusinessException(40001, "所属门店不能为空");
        }

        List<String> roomNos = normalizeRoomNos(form.getRoomNos());
        Integer roomCount = parseInteger(form.getRoomCount());
        if (roomCount == null || roomCount < 1) {
            roomCount = roomNos.isEmpty() ? 1 : roomNos.size();
        }
        if (roomNos.isEmpty()) {
            roomNos = List.of("房间1");
        }
        assertRoomNosAvailable(resolvedCampId, poiId, roomCategoryId, roomNos);

        int affectedRows = createMode
                ? roomCategoryMapper.insertRoomCategory(
                roomCategoryId,
                resolvedCampId,
                poiId,
                firstLong(form.getGroupId(), form.getRoomCategoryGroupId()),
                roomName,
                trimToNull(form.getDisplayName()),
                roomCount,
                trimToNull(form.getRentalType()),
                trimToNull(form.getPropertyType()),
                parseInteger(form.getGuestCount()),
                parseDecimal(form.getSuiteArea()),
                parseInteger(form.getBedroomCount()),
                parseInteger(form.getLivingRoomCount()),
                parseInteger(form.getKitchenCount()),
                parseInteger(form.getBathroomCount()),
                trimToNull(form.getBathroomType()),
                parsePriceCent(form.getWeekdayPrice()),
                parsePriceCent(form.getWeekendPrice()),
                parsePriceCent(form.getHolidayPrice()),
                trimToNull(form.getLocationMode()),
                trimToNull(form.getLocationProvinceCode()),
                trimToNull(form.getLocationProvinceName()),
                trimToNull(form.getLocationCityCode()),
                trimToNull(form.getLocationCityName()),
                trimToNull(form.getLocationDistrictCode()),
                trimToNull(form.getLocationDistrictName()),
                trimToNull(form.getStreetAddress()),
                trimToNull(form.getCommunityName()),
                trimToNull(form.getBuildingUnit()),
                trimToNull(form.getDoorNumber()),
                parseDecimal(form.getLocationLatitude()),
                parseDecimal(form.getLocationLongitude()),
                parseInteger(form.getEarliestCheckIn()),
                parseInteger(form.getLatestCheckIn()),
                parseInteger(form.getLatestCheckOut()),
                joinStringList(form.getSelectedFacilityIds()),
                trimToNull(form.getBedSheetChangePolicy()),
                trimToNull(form.getDecorationStyle()),
                trimToNull(form.getHighlightDescription()),
                trimToNull(form.getNearbyDescription()),
                trimToNull(form.getArticleDescription()),
                userId
        )
                : roomCategoryMapper.updateRoomCategory(
                roomCategoryId,
                resolvedCampId,
                poiId,
                firstLong(form.getGroupId(), form.getRoomCategoryGroupId()),
                roomName,
                trimToNull(form.getDisplayName()),
                roomCount,
                trimToNull(form.getRentalType()),
                trimToNull(form.getPropertyType()),
                parseInteger(form.getGuestCount()),
                parseDecimal(form.getSuiteArea()),
                parseInteger(form.getBedroomCount()),
                parseInteger(form.getLivingRoomCount()),
                parseInteger(form.getKitchenCount()),
                parseInteger(form.getBathroomCount()),
                trimToNull(form.getBathroomType()),
                parsePriceCent(form.getWeekdayPrice()),
                parsePriceCent(form.getWeekendPrice()),
                parsePriceCent(form.getHolidayPrice()),
                trimToNull(form.getLocationMode()),
                trimToNull(form.getLocationProvinceCode()),
                trimToNull(form.getLocationProvinceName()),
                trimToNull(form.getLocationCityCode()),
                trimToNull(form.getLocationCityName()),
                trimToNull(form.getLocationDistrictCode()),
                trimToNull(form.getLocationDistrictName()),
                trimToNull(form.getStreetAddress()),
                trimToNull(form.getCommunityName()),
                trimToNull(form.getBuildingUnit()),
                trimToNull(form.getDoorNumber()),
                parseDecimal(form.getLocationLatitude()),
                parseDecimal(form.getLocationLongitude()),
                parseInteger(form.getEarliestCheckIn()),
                parseInteger(form.getLatestCheckIn()),
                parseInteger(form.getLatestCheckOut()),
                joinStringList(form.getSelectedFacilityIds()),
                trimToNull(form.getBedSheetChangePolicy()),
                trimToNull(form.getDecorationStyle()),
                trimToNull(form.getHighlightDescription()),
                trimToNull(form.getNearbyDescription()),
                trimToNull(form.getArticleDescription()),
                userId
        );

        if (affectedRows == 0) {
            throw new BusinessException(40404, "房型不存在");
        }

        replaceRooms(resolvedCampId, poiId, roomCategoryId, userId, roomNos);
        if (form.getPhotos() != null) {
            replacePhotos(resolvedCampId, roomCategoryId, form.getPhotos());
        }
        return RoomCategoryMutationResultVO.of(roomCategoryId, createMode ? "房型已创建" : "房型信息已保存");
    }

    @Override
    @Transactional
    public RoomCategoryMutationResultVO deleteRoomCategory(Long campId, Long userId, Long roomCategoryId) {
        if (roomCategoryId == null) {
            throw new BusinessException(40001, "房型ID不能为空");
        }

        Long resolvedCampId = resolveAccessibleCampId(campId, userId);
        roomCategoryMapper.disableLinkages(resolvedCampId, roomCategoryId);
        roomCategoryMapper.deleteCleanTasksByCategory(resolvedCampId, roomCategoryId);
        roomCategoryMapper.deleteRoomsByCategory(resolvedCampId, roomCategoryId, userId);
        int affectedRows = roomCategoryMapper.deleteRoomCategory(resolvedCampId, roomCategoryId, userId);
        if (affectedRows == 0) {
            throw new BusinessException(40404, "房型不存在");
        }
        return RoomCategoryMutationResultVO.of(roomCategoryId, "房型已删除");
    }

    private void hydrateNestedViews(List<RoomCategoryPageItemVO> items) {
        if (items.isEmpty()) {
            return;
        }

        List<Long> roomCategoryIds = items.stream()
                .map(RoomCategoryPageItemVO::getRoomCategoryId)
                .filter(Objects::nonNull)
                .map(Long::valueOf)
                .toList();

        Map<String, List<RoomCategoryRoomViewVO>> roomViewsByCategoryId = roomCategoryMapper.selectRoomViews(roomCategoryIds)
                .stream()
                .collect(Collectors.groupingBy(
                        RoomCategoryRoomViewVO::getRoomCategoryId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (RoomCategoryPageItemVO item : items) {
            item.setRoomViews(roomViewsByCategoryId.getOrDefault(item.getRoomCategoryId(), List.of()));
            item.setPoiView(buildPoiView(item));
            item.setRoomCategoryProductInfoViews(List.of(buildProductInfo(item)));
            item.setRoomCategorySystemConfigInfos(List.of());
            item.setLinkRcs(List.of());
            item.setByLinkRcs(List.of());
            item.setChannels(List.of());
            item.setIcsInfoViews(List.of());
            item.setSaleCampId(item.getCampId());
            item.setUpstreamCampId(item.getCampId());
            item.setDownstreamCampId(item.getCampId());
            item.setFromType(0);
            item.setChannelId("0");
            item.setParentId(item.getRoomCategoryId());
            item.setIsTransfer(0);
            item.setChannelOrderTotalNum(0);
            item.setExpectedChannelOrderTotalNum(0);
            item.setIsAvailability(1);
            item.setWaitMappingChannelNum(0);
            item.setIsSupportHotelProduct(1);
            item.setIsCanDelete(1);
            if (item.getInventory() == null && item.getRoomNum() != null) {
                item.setInventory(item.getRoomNum());
            }
        }
    }

    private PoiViewVO buildPoiView(RoomCategoryPageItemVO item) {
        PoiViewVO poiView = new PoiViewVO();
        poiView.setPoiId(item.getPoiId());
        poiView.setName(item.getPoiName());
        return poiView;
    }

    private RoomCategoryProductInfoVO buildProductInfo(RoomCategoryPageItemVO item) {
        RoomCategoryProductInfoVO productInfo = new RoomCategoryProductInfoVO();
        productInfo.setRoomCategoryProductId(item.getRoomCategoryId());
        productInfo.setRoomCategoryProductName(item.getRoomCategoryName());
        productInfo.setSaleType(DEFAULT_SALE_TYPE);
        productInfo.setSerialCheckInTime(null);
        productInfo.setEarliestCheckInTime(item.getEarliestCheckInTime());
        productInfo.setLatestCheckInTime(item.getLatestCheckInTime());
        productInfo.setLatestCheckOutTime(item.getLatestCheckOutTime());
        productInfo.setHourCheckInTime(null);
        productInfo.setHourCheckOutTime(null);
        productInfo.setHourSerialCheckTime(null);
        productInfo.setIsHourLimit(null);
        productInfo.setCancelPolicy(DEFAULT_CANCEL_POLICY);
        productInfo.setBreakfastCount(DEFAULT_BREAKFAST_COUNT);
        productInfo.setIsPerfectRoomCategoryProduct(DEFAULT_IS_PERFECT_PRODUCT);
        productInfo.setNormalPrice(item.getBasePrice());
        productInfo.setWeekendPrice(item.getWeekendPrice());
        productInfo.setHolidayPrice(item.getHolidayPrice());
        productInfo.setStockMode(null);
        return productInfo;
    }

    private RoomCategoryEditFormVO buildEditForm(RoomCategoryPageItemVO row, CurrentUserBundleVO bundle, List<RoomCategoryPhotoVO> photos) {
        RoomCategoryEditFormVO form = new RoomCategoryEditFormVO();
        form.setRoomTypeId(row == null ? "" : defaultString(row.getRoomCategoryId()));
        form.setRoomTypeName(row == null ? "" : defaultString(row.getRoomCategoryName()));
        form.setStoreId(row == null ? defaultString(bundle == null || bundle.getPoiId() == null ? null : String.valueOf(bundle.getPoiId())) : defaultString(row.getPoiId()));
        form.setGroupId(row == null ? "" : defaultString(row.getRoomCategoryGroupId()));
        List<String> roomNos = splitRoomNames(row == null ? "" : row.getRoomNames());
        form.setRoomNos(roomNos.isEmpty() ? List.of("房间1") : roomNos);
        form.setRoomCount(row == null ? "1" : String.valueOf(row.getRoomNum() == null ? form.getRoomNos().size() : row.getRoomNum()));
        form.setWeekdayPrice(toYuanText(row == null ? null : row.getBasePrice()));
        form.setWeekendPrice(toYuanText(row == null ? null : row.getWeekendPrice()));
        form.setHolidayPrice(toYuanText(row == null ? null : row.getHolidayPrice()));
        form.setLocationMode(row == null ? "same-store" : defaultString(row.getLocationMode(), "same-store"));
        form.setLocationProvinceCode(row == null ? "" : defaultString(row.getLocationProvinceCode()));
        form.setLocationProvinceName(row == null ? "" : defaultString(row.getLocationProvinceName()));
        form.setLocationCityCode(row == null ? "" : defaultString(row.getLocationCityCode()));
        form.setLocationCityName(row == null ? "" : defaultString(row.getLocationCityName()));
        form.setLocationDistrictCode(row == null ? "" : defaultString(row.getLocationDistrictCode()));
        form.setLocationDistrictName(row == null ? "" : defaultString(row.getLocationDistrictName()));
        form.setStreetAddress(row == null ? "" : defaultString(row.getStreetAddress()));
        form.setCommunityName(row == null ? "" : defaultString(row.getCommunityName()));
        form.setBuildingUnit(row == null ? "" : defaultString(row.getBuildingUnit()));
        form.setDoorNumber(row == null ? "" : defaultString(row.getDoorNumber()));
        form.setLocationLatitude(row == null ? "" : toPlainText(row.getLocationLatitude()));
        form.setLocationLongitude(row == null ? "" : toPlainText(row.getLocationLongitude()));
        form.setRentalType(row == null ? "entire" : defaultString(row.getRentalType(), "entire"));
        form.setPropertyType(row == null ? "apartment" : defaultString(row.getPropertyType(), "apartment"));
        form.setSuiteArea(row == null ? "" : toPlainText(row.getSuiteArea()));
        form.setGuestCount(row == null || row.getCheckinGuestCount() == null ? "" : String.valueOf(row.getCheckinGuestCount()));
        form.setBedroomCount(row == null || row.getBedroomCount() == null ? "" : String.valueOf(row.getBedroomCount()));
        form.setLivingRoomCount(row == null || row.getLivingRoomCount() == null ? "" : String.valueOf(row.getLivingRoomCount()));
        form.setKitchenCount(row == null || row.getKitchenCount() == null ? "" : String.valueOf(row.getKitchenCount()));
        form.setBathroomCount(row == null || row.getBathroomCount() == null ? "" : String.valueOf(row.getBathroomCount()));
        form.setBathroomType(row == null ? "private" : defaultString(row.getBathroomType(), "private"));
        form.setSelectedFacilityIds(row == null ? List.of() : splitStringList(row.getSelectedFacilityIds()));
        form.setBedSheetChangePolicy(row == null ? "" : defaultString(row.getBedSheetChangePolicy()));
        form.setDecorationStyle(row == null ? "" : defaultString(row.getDecorationStyle()));
        form.setDisplayName(row == null ? "" : defaultString(row.getDisplayName()));
        form.setEarliestCheckIn(row == null || row.getEarliestCheckInTime() == null ? "12" : String.valueOf(row.getEarliestCheckInTime()));
        form.setLatestCheckOut(row == null || row.getLatestCheckOutTime() == null ? "14" : String.valueOf(row.getLatestCheckOutTime()));
        form.setLatestCheckIn(row == null || row.getLatestCheckInTime() == null ? "24" : String.valueOf(row.getLatestCheckInTime()));
        form.setHighlightDescription(row == null ? "" : defaultString(row.getHighlightDescription()));
        form.setNearbyDescription(row == null ? "" : defaultString(row.getNearbyDescription()));
        form.setArticleDescription(row == null ? "" : defaultString(row.getArticleDescription()));
        form.setPhotos(photos == null ? List.of() : photos);
        form.setPhotoCounts(countPhotos(photos));
        return form;
    }

    private RoomCategoryLinkageCandidateVO toLinkageCandidate(RoomCategoryPageItemVO row, Set<Long> selectedIds) {
        RoomCategoryLinkageCandidateVO candidate = new RoomCategoryLinkageCandidateVO();
        candidate.setId(defaultString(row.getRoomCategoryId()));
        candidate.setName(defaultString(row.getRoomCategoryName()));
        Long candidateId = parseLong(row.getRoomCategoryId());
        candidate.setSelected(candidateId != null && selectedIds.contains(candidateId));
        return candidate;
    }

    private void assertRoomNosAvailable(Long campId, Long poiId, Long roomCategoryId, List<String> roomNos) {
        Set<String> normalizedRoomNos = new LinkedHashSet<>(roomNos);
        if (normalizedRoomNos.size() != roomNos.size()) {
            throw new BusinessException(40001, "房间号不能重复");
        }

        List<String> existingRoomNames = roomCategoryMapper.selectExistingActiveRoomNames(
                campId,
                poiId,
                roomCategoryId,
                List.copyOf(normalizedRoomNos)
        );
        if (!existingRoomNames.isEmpty()) {
            throw new BusinessException(40001, "房间号已存在：" + existingRoomNames.get(0));
        }
    }

    private void replaceRooms(Long campId, Long poiId, Long roomCategoryId, Long userId, List<String> roomNos) {
        roomCategoryMapper.deleteRoomsByCategory(campId, roomCategoryId, userId);
        for (int index = 0; index < roomNos.size(); index++) {
            roomCategoryMapper.insertRoom(IdWorker.getId(), campId, poiId, roomCategoryId, roomNos.get(index), index + 1, userId);
        }
    }

    private void replacePhotos(Long campId, Long roomCategoryId, List<RoomCategorySaveRequest.Photo> photos) {
        roomCategoryMapper.disableRoomCategoryMedia(campId, roomCategoryId);
        if (photos.isEmpty()) {
            return;
        }

        Set<Long> seenMediaResourceIds = new LinkedHashSet<>();
        int fallbackSortNo = 1;
        for (RoomCategorySaveRequest.Photo photo : photos) {
            Long mediaResourceId = resolvePhotoMediaResourceId(campId, photo);
            if (mediaResourceId == null) {
                throw new BusinessException(40001, "照片缺少媒体资源ID");
            }
            if (!seenMediaResourceIds.add(mediaResourceId)) {
                continue;
            }
            if (roomCategoryMapper.countMediaResource(campId, mediaResourceId) == 0) {
                throw new BusinessException(40001, "照片资源不存在");
            }

            int sortNo = firstInteger(photo.getSortOrder(), photo.getSort(), fallbackSortNo);
            roomCategoryMapper.insertRoomCategoryMedia(
                    IdWorker.getId(),
                    campId,
                    roomCategoryId,
                    mediaResourceId,
                    firstText(photo.getUrl(), photo.getFileUrl(), photo.getImageUrl(), photo.getPath(), photo.getSrc()),
                    normalizePhotoSection(firstText(photo.getSectionKey(), photo.getType(), photo.getCategory())),
                    sortNo
            );
            fallbackSortNo++;
        }
    }

    private RoomCategoryDetailChannelPriceVO toChannelPrice(RoomCategoryDetailChannelPriceRowVO row) {
        RoomCategoryDetailChannelPriceVO item = new RoomCategoryDetailChannelPriceVO();
        item.setLabel(row.getLabel());
        item.setPrice(resolveChannelPrice(row));
        item.setStatus(resolveChannelPriceStatus(row));
        return item;
    }

    private Integer resolveChannelPrice(RoomCategoryDetailChannelPriceRowVO row) {
        String label = defaultString(row.getLabel());
        Long price = label.contains("美团") ? row.getWeekendPrice() : row.getBasePrice();
        return price == null ? 0 : Math.toIntExact(price);
    }

    private String resolveChannelPriceStatus(RoomCategoryDetailChannelPriceRowVO row) {
        boolean approved = "approved".equalsIgnoreCase(defaultString(row.getAuditStatus()));
        boolean onShelf = "on_shelf".equalsIgnoreCase(defaultString(row.getShelfStatus()));
        return approved && onShelf ? "已同步" : "待同步";
    }

    private List<String> buildGuidance(
            String roomName,
            int inventory,
            int staying,
            int occupancyRate,
            int pendingOrders,
            int channelPriceCount
    ) {
        List<String> guidance = new ArrayList<>();
        guidance.add("当前库存 " + inventory + " 间，在住 " + staying + " 间，入住压力 " + occupancyRate + "%。");
        guidance.add("待处理订单 " + pendingOrders + " 单，建议优先核对 " + roomName + " 的房态与渠道价差。");
        guidance.add("已关联 " + channelPriceCount + " 个渠道价格视图，后续可继续接入更多房型经营指标。");
        return guidance;
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

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long firstLong(String first, String second) {
        Long firstValue = parseLong(first);
        return firstValue == null ? parseLong(second) : firstValue;
    }

    private Long firstLong(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            Long parsed = parseLong(value);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private String firstText(String first, String second) {
        String firstValue = trimToNull(first);
        return firstValue == null ? trimToNull(second) : firstValue;
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String text = trimToNull(value);
            if (text != null) {
                return text;
            }
        }
        return null;
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private int firstInteger(Integer first, Integer second, int fallback) {
        if (first != null && first > 0) {
            return first;
        }
        if (second != null && second > 0) {
            return second;
        }
        return fallback;
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Long parsePriceCent(String value) {
        BigDecimal price = parseDecimal(value);
        if (price == null) {
            return null;
        }
        return price.multiply(BigDecimal.valueOf(100)).longValue();
    }

    private String toYuanText(Long cent) {
        if (cent == null) {
            return "";
        }
        return BigDecimal.valueOf(cent)
                .divide(BigDecimal.valueOf(100))
                .stripTrailingZeros()
                .toPlainString();
    }

    private List<String> normalizeRoomNos(List<String> roomNos) {
        if (roomNos == null || roomNos.isEmpty()) {
            return List.of();
        }
        return roomNos.stream()
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .toList();
    }

    private String joinStringList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        String joined = values.stream()
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(","));
        return joined.isEmpty() ? null : joined;
    }

    private List<String> splitStringList(String value) {
        String text = trimToNull(value);
        if (text == null) {
            return List.of();
        }
        return List.of(text.split(","))
                .stream()
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<String> splitRoomNames(String roomNames) {
        String text = trimToNull(roomNames);
        if (text == null) {
            return List.of();
        }
        return List.of(text.split("[、,，\\s]+"))
                .stream()
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<String, Integer> defaultPhotoCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("cover", 0);
        counts.put("livingRoom", 0);
        counts.put("kitchen", 0);
        counts.put("other", 0);
        counts.put("bathroom", 0);
        counts.put("building", 0);
        counts.put("entertainment", 0);
        counts.put("uncategorized", 0);
        return counts;
    }

    private Map<String, Integer> countPhotos(List<RoomCategoryPhotoVO> photos) {
        Map<String, Integer> counts = defaultPhotoCounts();
        if (photos == null) {
            return counts;
        }
        for (RoomCategoryPhotoVO photo : photos) {
            String sectionKey = normalizePhotoSection(photo == null ? null : photo.getSectionKey());
            counts.put(sectionKey, counts.getOrDefault(sectionKey, 0) + 1);
        }
        return counts;
    }

    private Long resolvePhotoMediaResourceId(Long campId, RoomCategorySaveRequest.Photo photo) {
        if (photo == null) {
            return null;
        }
        Long mediaResourceId = firstLong(photo.getMediaResourceId(), photo.getId(), photo.getPhotoId(), photo.getFileId());
        if (mediaResourceId != null) {
            return mediaResourceId;
        }
        String url = firstText(photo.getUrl(), photo.getFileUrl(), photo.getImageUrl(), photo.getPath(), photo.getSrc());
        return url == null ? null : roomCategoryMapper.selectMediaResourceIdByUrl(campId, url);
    }

    private String normalizePhotoSection(String sectionKey) {
        if (sectionKey == null || sectionKey.isBlank()) {
            return "uncategorized";
        }
        return defaultPhotoCounts().containsKey(sectionKey) ? sectionKey : "uncategorized";
    }

    private String toPlainText(BigDecimal value) {
        if (value == null) {
            return "";
        }
        return value.stripTrailingZeros().toPlainString();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String defaultString(String value, String fallback) {
        String text = trimToNull(value);
        return text == null ? fallback : text;
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
            throw new BusinessException(40301, "无权访问当前门店房型数据");
        }
        return requestedCampId;
    }
}
