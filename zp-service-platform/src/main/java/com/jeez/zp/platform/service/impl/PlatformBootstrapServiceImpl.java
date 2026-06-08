package com.jeez.zp.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeez.zp.platform.dto.request.CampSaveRequest;
import com.jeez.zp.platform.dto.request.VersionSubscriptionOrderSubmitRequest;
import com.jeez.zp.platform.entity.PmsCamp;
import com.jeez.zp.platform.entity.PmsPoi;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PmsCampMapper;
import com.jeez.zp.platform.mapper.PmsPoiMapper;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.service.PlatformBootstrapService;
import com.jeez.zp.platform.vo.CampDetailVO;
import com.jeez.zp.platform.vo.CampsResponseVO;
import com.jeez.zp.platform.vo.ChannelVO;
import com.jeez.zp.platform.vo.ChannelsResponseVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.EditionResourceVO;
import com.jeez.zp.platform.vo.MenuNodeVO;
import com.jeez.zp.platform.vo.MenuOptionJsonsVO;
import com.jeez.zp.platform.vo.MenuProjectVO;
import com.jeez.zp.platform.vo.SystemConfigItemVO;
import com.jeez.zp.platform.vo.SystemConfigsResponseVO;
import com.jeez.zp.platform.vo.UserOwnVO;
import com.jeez.zp.platform.vo.VersionSubscriptionOrderSubmitVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PlatformBootstrapServiceImpl implements PlatformBootstrapService {

    private static final long DEFAULT_PROJECT_MENU_ID = 1L;
    private static final String DEFAULT_MENU_ID = "1848317056370487297";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PlatformBootstrapMapper platformBootstrapMapper;
    private final PmsCampMapper pmsCampMapper;
    private final PmsPoiMapper pmsPoiMapper;
    private final ObjectMapper objectMapper;

    @Override
    public CampsResponseVO getCamps() {
        return new CampsResponseVO(platformBootstrapMapper.selectAvailableCamps());
    }

    @Override
    public CurrentUserBundleVO getCurrentUserBundle(Long userId) {
        return platformBootstrapMapper.selectCurrentUserBundle(userId);
    }

    @Override
    public UserOwnVO getOwnUser(Long userId) {
        CurrentUserBundleVO bundle = requireUserBundle(userId);
        List<String> permissionCodes = platformBootstrapMapper.selectAuthorityCodesByRoleId(bundle.getRoleId());

        UserOwnVO response = new UserOwnVO();
        response.setUserId(bundle.getUserId());
        response.setMemberId(bundle.getMemberId());
        response.setCampId(bundle.getCampId());
        response.setPoiId(bundle.getPoiId());
        response.setRoleId(bundle.getRoleId());
        response.setRoleCode(bundle.getRoleCode());
        response.setRoleName(bundle.getRoleName());
        response.setPermissionCodes(permissionCodes);
        response.setUser(new LinkedHashMap<>(Map.of(
                "userId", bundle.getUserId(),
                "mobile", bundle.getMobile(),
                "email", bundle.getEmail(),
                "nickName", bundle.getNickName()
        )));
        response.setMember(new LinkedHashMap<>(Map.of(
                "memberId", bundle.getMemberId(),
                "name", bundle.getMemberName(),
                "campId", bundle.getCampId(),
                "roleId", bundle.getRoleId()
        )));
        response.setCamp(new LinkedHashMap<>(Map.of(
                "campId", bundle.getCampId(),
                "campName", bundle.getCampName(),
                "name", bundle.getCampName()
        )));
        response.setPoi(new LinkedHashMap<>(Map.of(
                "poiId", bundle.getPoiId(),
                "poiName", bundle.getPoiName()
        )));
        response.setRole(new LinkedHashMap<>(Map.of(
                "roleId", bundle.getRoleId(),
                "roleCode", bundle.getRoleCode(),
                "roleName", bundle.getRoleName()
        )));
        return response;
    }

    @Override
    public CampDetailVO getCamp(Long campId, Long poiId, Long userId) {
        Long resolvedCampId = resolveCampId(campId, userId);
        CampDetailVO detail = platformBootstrapMapper.selectCampDetail(resolvedCampId, poiId);
        if (detail == null || (poiId != null && detail.getPoiId() == null)) {
            throw new BusinessException(40404, "门店不存在");
        }

        List<String> tags = parseTags(detail.getTagsJson());
        detail.setTags(tags);
        detail.setPhone(detail.getContactNumber());
        detail.setPoiName(firstText(detail.getPoiName(), detail.getName()));
        detail.setTypeName(firstText(detail.getTypeName(), detail.getPoiType()));
        detail.setCampTypeName(firstText(detail.getCampTypeName(), detail.getTypeName()));
        detail.setCityPath(firstText(detail.getCityPath(), detail.getCityName()));
        detail.setFullAddress(firstText(detail.getFullAddress(), detail.getAddress()));
        detail.setPhotoCount(detail.getPhotoCount() == null ? 0 : detail.getPhotoCount());

        String campName = firstText(detail.getCampName(), detail.getName());
        Map<String, Object> camp = new LinkedHashMap<>();
        camp.put("campId", detail.getCampId());
        camp.put("campName", campName);
        camp.put("name", campName);
        camp.put("poiId", detail.getPoiId());
        camp.put("poiName", detail.getPoiName());
        camp.put("typeName", detail.getTypeName());
        camp.put("campTypeName", detail.getCampTypeName());
        camp.put("cityName", detail.getCityName());
        camp.put("cityPath", detail.getCityPath());
        camp.put("address", detail.getAddress());
        camp.put("streetAddress", detail.getStreetAddress());
        camp.put("communityName", detail.getCommunityName());
        camp.put("unitNo", detail.getUnitNo());
        camp.put("fullAddress", detail.getFullAddress());
        camp.put("contactNumber", detail.getContactNumber());
        camp.put("phone", detail.getPhone());
        camp.put("tags", tags);
        camp.put("plainIntro", detail.getPlainIntro());
        camp.put("richIntro", detail.getRichIntro());
        camp.put("coverImageDataUrl", detail.getCoverImageDataUrl());
        camp.put("photoCount", detail.getPhotoCount());
        detail.setCamp(camp);
        return detail;
    }

    @Override
    @Transactional
    public CampDetailVO saveCamp(CampSaveRequest request, Long userId) {
        if (request == null) {
            throw new BusinessException(40001, "门店保存参数不能为空");
        }
        Long requestedCampId = parseLong(request.getCampId());
        Long resolvedCampId = resolveAccessibleCampId(requestedCampId, userId);
        String campName = requireText(firstText(request.getCampName(), request.getName()), "门店名称不能为空");
        String cityName = firstText(request.getCityName(), request.getCityPath());
        String address = firstText(request.getFullAddress(), request.getAddress(), request.getStreetAddress());
        String contactNumber = firstText(request.getContactNumber(), request.getPhone());
        String poiType = firstText(request.getTypeName(), request.getCampTypeName());
        List<String> tags = normalizeTags(request.getTags());
        String tagsJson = serializeConfigValue(tags);
        LocalDateTime now = LocalDateTime.now();

        String requestedPoiRef = firstText(request.getPoiId(), request.getStoreId());
        Long requestedPoiId = parseOptionalLong(requestedPoiRef);
        PmsPoi existingPoi = requestedPoiId == null
                ? (requestedPoiRef == null ? findFirstActivePoi(resolvedCampId) : null)
                : findAccessiblePoi(resolvedCampId, requestedPoiId);
        Long savedPoiId = existingPoi == null ? IdWorker.getId() : existingPoi.getPoiId();

        PmsPoi poi = new PmsPoi();
        poi.setPoiId(savedPoiId);
        poi.setCampId(resolvedCampId);
        poi.setPoiName(campName);
        poi.setPoiType(poiType);
        poi.setIsAvailability(1);
        poi.setAddress(address);
        poi.setContactNumber(contactNumber);
        poi.setCityName(cityName);
        poi.setCityPath(firstText(request.getCityPath(), cityName));
        poi.setStreetAddress(firstText(request.getStreetAddress(), request.getAddress(), address));
        poi.setCommunityName(firstText(request.getCommunityName()));
        poi.setUnitNo(firstText(request.getUnitNo()));
        poi.setFullAddress(firstText(request.getFullAddress(), address));
        poi.setTagsJson(tagsJson);
        poi.setPlainIntro(firstText(request.getPlainIntro()));
        poi.setRichIntro(firstText(request.getRichIntro()));
        poi.setCoverImageDataUrl(firstText(request.getCoverImageDataUrl()));
        poi.setPhotoCount(request.getPhotoCount() == null ? 0 : Math.max(request.getPhotoCount(), 0));
        poi.setStatus(1);
        poi.setUpdatedBy(userId);
        poi.setUpdatedAt(now);

        if (existingPoi == null) {
            poi.setSortNo(nextPoiSortNo(resolvedCampId));
            poi.setCreatedBy(userId);
            poi.setCreatedAt(now);
            poi.setIsDeleted(0);
            poi.setVersionNo(0);
            pmsPoiMapper.insert(poi);
        } else {
            pmsPoiMapper.updateById(poi);
        }

        if (shouldMirrorToCamp(resolvedCampId, savedPoiId)) {
            PmsCamp camp = new PmsCamp();
            camp.setCampId(resolvedCampId);
            camp.setName(campName);
            camp.setCityName(cityName);
            camp.setAddress(address);
            camp.setContactNumber(contactNumber);
            camp.setUpdatedBy(userId);
            camp.setUpdatedAt(now);
            pmsCampMapper.updateById(camp);
        }

        return getCamp(resolvedCampId, savedPoiId, userId);
    }

    @Override
    public MenuProjectVO getProjectMenus(Long campId, Long projectMenuId) {
        long resolvedProjectMenuId = projectMenuId == null ? DEFAULT_PROJECT_MENU_ID : projectMenuId;
        List<MenuNodeVO> menus = List.of(
                new MenuNodeVO("dashboard", "首页", "/dashboard", List.of()),
                new MenuNodeVO("room", "房态管理", "/houseManage/months", List.of()),
                new MenuNodeVO("order", "订单管理", "/order/orderAll", List.of()),
                new MenuNodeVO("finance", "财务管理", "/finance/overview", List.of())
        );

        MenuProjectVO response = new MenuProjectVO();
        response.setProjectMenuId(resolvedProjectMenuId);
        response.setMenus(menus);
        response.setMenuTree(menus);
        return response;
    }

    @Override
    public MenuOptionJsonsVO getMenuOptionJsons(List<String> menuIds) {
        List<String> resolvedMenuIds = CollectionUtils.isEmpty(menuIds) ? List.of(DEFAULT_MENU_ID) : menuIds;

        Map<String, Object> versionModals = new LinkedHashMap<>();
        versionModals.put("title", "畅享版全新上线");
        versionModals.put("info", "升级后可获得更完整的会话设置、快捷键与版本能力编排。");
        versionModals.put("buttons", List.of(
                new LinkedHashMap<>(Map.of(
                        "buttonText", "立即开通",
                        "type", "primary",
                        "action", "/setting/myBenefit"
                )),
                new LinkedHashMap<>(Map.of(
                        "buttonText", "稍后再说",
                        "type", "default",
                        "action", "close"
                ))
        ));

        List<Map<String, Object>> optionJsonViews = resolvedMenuIds.stream()
                .map(menuId -> {
                    Map<String, Object> optionJson = new LinkedHashMap<>();
                    optionJson.put("versionModals", versionModals);

                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("menuId", menuId);
                    row.put("optionJson", optionJson);
                    return row;
                })
                .toList();

        MenuOptionJsonsVO response = new MenuOptionJsonsVO();
        response.setOptionJsonViews(optionJsonViews);
        return response;
    }

    @Override
    public SystemConfigsResponseVO getSystemConfigs(Long campId, Long userId) {
        Long resolvedCampId = resolveCampId(campId, userId);
        List<SystemConfigItemVO> configs = platformBootstrapMapper.selectSystemConfigs(resolvedCampId, userId);
        configs.forEach(item -> item.setConfigValue(parseJsonIfPossible(item.getConfigValue())));

        SystemConfigsResponseVO response = new SystemConfigsResponseVO();
        response.setConfigs(configs);
        return response;
    }

    @Override
    public ChannelsResponseVO getChannels(Long campId, Long userId) {
        Long resolvedCampId = resolveCampId(campId, userId);
        List<ChannelVO> channels = platformBootstrapMapper.selectChannelsByCampId(resolvedCampId);

        ChannelsResponseVO response = new ChannelsResponseVO();
        response.setChannels(channels);
        return response;
    }

    @Override
    public EditionResourceVO getEditionResource(Long campId, Long userId) {
        Long resolvedCampId = resolveCampId(campId, userId);
        CampDetailVO campDetail = getCamp(resolvedCampId, null, userId);
        List<ChannelVO> channels = platformBootstrapMapper.selectChannelsByCampId(resolvedCampId);

        long connectedChannels = channels.stream()
                .filter(channel -> "success".equalsIgnoreCase(channel.getSyncStatus()))
                .count();
        long totalChannels = channels.size();

        EditionResourceVO response = new EditionResourceVO();
        response.setEditionId("9");
        response.setEditionName("畅享版");
        response.setEditionType(1);
        response.setResourceName("全域雷达");
        response.setExpireDateRange(buildExpireDateRange());
        response.setPriceText("¥0 / 演示环境");
        response.setConnectorProgress(connectedChannels + "/" + totalChannels + " 已连接");
        response.setResourceGetViews(List.of(buildStoreQuotaResource(campDetail)));
        response.setValueAddServices(null);
        return response;
    }

    @Override
    public VersionSubscriptionOrderSubmitVO submitVersionSubscriptionOrder(VersionSubscriptionOrderSubmitRequest request, Long userId) {
        Long resolvedCampId = resolveAccessibleCampId(parseLong(request == null ? null : request.getCampId()), userId);
        String editionId = requireText(request == null ? null : request.getEditionId(), "editionId 不能为空");
        String duration = requireSupportedDuration(request == null ? null : request.getDuration());
        EditionPlan editionPlan = resolveEditionPlan(editionId);

        VersionSubscriptionOrderSubmitVO response = new VersionSubscriptionOrderSubmitVO();
        response.setMessage(editionPlan.displayName + "购买信息已生成");
        response.setRedirectTo("/version/applicationPayment/detail?plan=" + editionPlan.planId + "&duration=" + duration);
        response.setOrderNo(buildVersionSubscriptionOrderNo(resolvedCampId));
        return response;
    }

    private CurrentUserBundleVO requireUserBundle(Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }
        return bundle;
    }

    private Long resolveCampId(Long campId, Long userId) {
        if (campId != null) {
            return campId;
        }
        return requireUserBundle(userId).getCampId();
    }

    private Long resolveAccessibleCampId(Long requestedCampId, Long userId) {
        CurrentUserBundleVO bundle = requireUserBundle(userId);
        if (requestedCampId == null) {
            return bundle.getCampId();
        }
        if (!Objects.equals(requestedCampId, bundle.getCampId())) {
            throw new BusinessException(40301, "无权访问当前门店数据");
        }
        return requestedCampId;
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(40001, message);
        }
        return value.trim();
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private PmsPoi findFirstActivePoi(Long campId) {
        return pmsPoiMapper.selectOne(new LambdaQueryWrapper<PmsPoi>()
                .eq(PmsPoi::getCampId, campId)
                .eq(PmsPoi::getIsDeleted, 0)
                .eq(PmsPoi::getStatus, 1)
                .orderByAsc(PmsPoi::getSortNo, PmsPoi::getPoiId)
                .last("LIMIT 1"));
    }

    private PmsPoi findAccessiblePoi(Long campId, Long poiId) {
        PmsPoi poi = pmsPoiMapper.selectOne(new LambdaQueryWrapper<PmsPoi>()
                .eq(PmsPoi::getCampId, campId)
                .eq(PmsPoi::getPoiId, poiId)
                .eq(PmsPoi::getIsDeleted, 0)
                .last("LIMIT 1"));
        if (poi == null) {
            throw new BusinessException(40404, "门店不存在");
        }
        return poi;
    }

    private int nextPoiSortNo(Long campId) {
        Long count = pmsPoiMapper.selectCount(new LambdaQueryWrapper<PmsPoi>()
                .eq(PmsPoi::getCampId, campId)
                .eq(PmsPoi::getIsDeleted, 0));
        return count == null ? 0 : count.intValue() + 1;
    }

    private boolean shouldMirrorToCamp(Long campId, Long poiId) {
        PmsPoi firstPoi = findFirstActivePoi(campId);
        return firstPoi != null && Objects.equals(firstPoi.getPoiId(), poiId);
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>();
        for (String tag : tags) {
            String nextTag = firstText(tag);
            if (nextTag != null && !normalized.contains(nextTag)) {
                normalized.add(nextTag);
            }
        }
        return normalized;
    }

    private String requireSupportedDuration(String duration) {
        String normalizedDuration = requireText(duration, "duration 不能为空");
        if (!List.of("1y", "2y", "forever").contains(normalizedDuration)) {
            throw new BusinessException(40002, "不支持的版本订阅时长");
        }
        return normalizedDuration;
    }

    private EditionPlan resolveEditionPlan(String editionId) {
        return switch (editionId) {
            case "1" -> new EditionPlan("standard", "标准版");
            case "9" -> new EditionPlan("delight", "畅享版");
            case "2" -> new EditionPlan("advanced", "高级版");
            case "3" -> new EditionPlan("professional", "专业版");
            case "4" -> new EditionPlan("flagship", "旗舰版");
            case "5" -> new EditionPlan("custom", "定制版");
            default -> new EditionPlan("custom", "版本订阅");
        };
    }

    private String buildVersionSubscriptionOrderNo(Long campId) {
        return "VS" + LocalDateTime.now().format(ORDER_NO_FORMATTER) + campId;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
    }

    private Long parseOptionalLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private record EditionPlan(String planId, String displayName) {
    }

    private Object parseJsonIfPossible(Object rawValue) {
        if (!(rawValue instanceof String textValue)) {
            return rawValue;
        }
        try {
            return objectMapper.readValue(textValue, Object.class);
        } catch (Exception ignored) {
            return textValue;
        }
    }

    private List<String> parseTags(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return normalizeTags(objectMapper.readValue(rawValue, new TypeReference<List<String>>() {
            }));
        } catch (Exception ignored) {
            return normalizeTags(List.of(rawValue.split("/")));
        }
    }

    private String serializeConfigValue(Object configValue) {
        try {
            return objectMapper.writeValueAsString(configValue);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(500, "门店标签序列化失败");
        }
    }

    private String buildExpireDateRange() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusYears(1).minusDays(1);
        return start.format(DATE_FORMATTER) + " 至 " + end.format(DATE_FORMATTER);
    }

    private Map<String, Object> buildStoreQuotaResource(CampDetailVO campDetail) {
        Map<String, Object> usedQuotaView = new LinkedHashMap<>();
        usedQuotaView.put("usedQuotaNum", 1);
        usedQuotaView.put("campUsedQuotaViews", List.of(new LinkedHashMap<>(Map.of(
                "campId", campDetail.getCampId(),
                "campName", campDetail.getName(),
                "usedQuotaNum", 1
        ))));

        Map<String, Object> resourceFrom = new LinkedHashMap<>();
        resourceFrom.put("editionNum", 1);
        resourceFrom.put("isUnlimitedEditionNum", 0);
        resourceFrom.put("expandQuotaNum", 0);
        resourceFrom.put("expandQuotaDetailView", List.of());

        Map<String, Object> resource = new LinkedHashMap<>();
        resource.put("resourceName", "门店");
        resource.put("quotaNum", 1);
        resource.put("isUnlimitedQuotaNum", 0);
        resource.put("usedQuotaView", usedQuotaView);
        resource.put("resourceFrom", resourceFrom);
        resource.put("editionStatus", 1);
        resource.put("goodsType", 2);
        resource.put("expirationDate", null);
        resource.put("isLongTerm", null);
        return resource;
    }
}
