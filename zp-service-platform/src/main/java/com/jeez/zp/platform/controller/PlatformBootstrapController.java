package com.jeez.zp.platform.controller;

import com.jeez.zp.platform.api.HudsonResponse;
import com.jeez.zp.platform.api.TraceIdFactory;
import com.jeez.zp.platform.dto.request.CampIdRequest;
import com.jeez.zp.platform.dto.request.CampSaveRequest;
import com.jeez.zp.platform.dto.request.MenuOptionJsonRequest;
import com.jeez.zp.platform.dto.request.MenuProjectRequest;
import com.jeez.zp.platform.dto.request.VersionSubscriptionOrderSubmitRequest;
import com.jeez.zp.platform.security.LoginUserContext;
import com.jeez.zp.platform.service.PlatformBootstrapService;
import com.jeez.zp.platform.vo.CampDetailVO;
import com.jeez.zp.platform.vo.CampsResponseVO;
import com.jeez.zp.platform.vo.ChannelsResponseVO;
import com.jeez.zp.platform.vo.EditionResourceVO;
import com.jeez.zp.platform.vo.MenuOptionJsonsVO;
import com.jeez.zp.platform.vo.MenuProjectVO;
import com.jeez.zp.platform.vo.SystemConfigsResponseVO;
import com.jeez.zp.platform.vo.UserOwnVO;
import com.jeez.zp.platform.vo.VersionSubscriptionOrderSubmitVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlatformBootstrapController {

    private final PlatformBootstrapService platformBootstrapService;

    @PostMapping("/camps/get")
    public HudsonResponse<CampsResponseVO> getCamps() {
        return HudsonResponse.success(platformBootstrapService.getCamps(), TraceIdFactory.next("camps-get"));
    }

    @PostMapping("/user/own/get")
    public HudsonResponse<UserOwnVO> getOwnUser() {
        return HudsonResponse.success(
                platformBootstrapService.getOwnUser(LoginUserContext.requiredUserId()),
                TraceIdFactory.next("user-own-get")
        );
    }

    @PostMapping("/camp/get")
    public HudsonResponse<CampDetailVO> getCamp(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                platformBootstrapService.getCamp(
                        parseLong(request.getCampId()),
                        parseLong(firstText(request.getPoiId(), request.getStoreId())),
                        LoginUserContext.requiredUserId()
                ),
                TraceIdFactory.next("camp-get")
        );
    }

    @PostMapping("/camp/save")
    public HudsonResponse<CampDetailVO> saveCamp(@RequestBody CampSaveRequest request) {
        return HudsonResponse.success(
                platformBootstrapService.saveCamp(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("camp-save")
        );
    }

    @PostMapping("/menus/project/get")
    public HudsonResponse<MenuProjectVO> getProjectMenus(@RequestBody MenuProjectRequest request) {
        return HudsonResponse.success(
                platformBootstrapService.getProjectMenus(parseLong(request.getCampId()), request.getProjectMenuId()),
                TraceIdFactory.next("menus-project-get")
        );
    }

    @PostMapping("/menu/optionJsons/get")
    public HudsonResponse<MenuOptionJsonsVO> getMenuOptionJsons(@RequestBody MenuOptionJsonRequest request) {
        return HudsonResponse.success(
                platformBootstrapService.getMenuOptionJsons(request.getMenuIds()),
                TraceIdFactory.next("menu-option-jsons-get")
        );
    }

    @PostMapping("/systemConfigs/get")
    public HudsonResponse<SystemConfigsResponseVO> getSystemConfigs(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                platformBootstrapService.getSystemConfigs(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("system-configs-get")
        );
    }

    @PostMapping("/channels/get")
    public HudsonResponse<ChannelsResponseVO> getChannels(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                platformBootstrapService.getChannels(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("channels-get")
        );
    }

    @PostMapping("/edition/resource/get")
    public HudsonResponse<EditionResourceVO> getEditionResource(@RequestBody CampIdRequest request) {
        return HudsonResponse.success(
                platformBootstrapService.getEditionResource(parseLong(request.getCampId()), LoginUserContext.requiredUserId()),
                TraceIdFactory.next("edition-resource-get")
        );
    }

    @PostMapping("/version/subscription/order/submit")
    public HudsonResponse<VersionSubscriptionOrderSubmitVO> submitVersionSubscriptionOrder(
            @RequestBody VersionSubscriptionOrderSubmitRequest request
    ) {
        return HudsonResponse.success(
                platformBootstrapService.submitVersionSubscriptionOrder(request, LoginUserContext.requiredUserId()),
                TraceIdFactory.next("version-subscription-order-submit")
        );
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.valueOf(value);
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
}
