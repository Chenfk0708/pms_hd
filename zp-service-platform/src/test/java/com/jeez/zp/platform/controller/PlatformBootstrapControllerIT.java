package com.jeez.zp.platform.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlatformBootstrapControllerIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Timeout(60)
    void campsGet_shouldReturnRealCampRows() throws Exception {
        mockMvc.perform(post("/camps/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.camps[0].campId").value(10001))
                .andExpect(jsonPath("$.data.camps[0].name").value("路客云演示租户"))
                .andExpect(jsonPath("$.data.camps[0].poiId").value(11001))
                .andExpect(jsonPath("$.data.camps[0].poiName").value("路客云演示门店"));
    }

    @Test
    @Timeout(60)
    void userOwnGet_shouldReturnCurrentUserBundle() throws Exception {
        mockMvc.perform(post("/user/own/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").value(12001))
                .andExpect(jsonPath("$.data.memberId").value(14001))
                .andExpect(jsonPath("$.data.campId").value(10001))
                .andExpect(jsonPath("$.data.poiId").value(11001))
                .andExpect(jsonPath("$.data.roleCode").value("admin"))
                .andExpect(jsonPath("$.data.user.nickName").value("系统管理员"))
                .andExpect(jsonPath("$.data.member.name").value("系统管理员"))
                .andExpect(jsonPath("$.data.camp.name").value("路客云演示租户"))
                .andExpect(jsonPath("$.data.poi.poiName").value("路客云演示门店"))
                .andExpect(jsonPath("$.data.permissionCodes[0]").value("dashboard:view"));
    }

    @Test
    @Timeout(60)
    void campGet_shouldReturnCampDetail() throws Exception {
        mockMvc.perform(post("/camp/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.campId").value(10001))
                .andExpect(jsonPath("$.data.name").value("路客云演示租户"))
                .andExpect(jsonPath("$.data.cityName").value("深圳市"))
                .andExpect(jsonPath("$.data.address").value("深圳市南山区科技园演示地址 1 号"))
                .andExpect(jsonPath("$.data.contactNumber").value("13800000001"))
                .andExpect(jsonPath("$.data.camp.campId").value(10001));
    }

    @Test
    @Timeout(60)
    void menusProjectGet_shouldReturnProjectMenuTree() throws Exception {
        mockMvc.perform(post("/menus/project/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","projectMenuId":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.projectMenuId").value(1))
                .andExpect(jsonPath("$.data.menus[0].code").value("dashboard"))
                .andExpect(jsonPath("$.data.menuTree[1].code").value("room"));
    }

    @Test
    @Timeout(60)
    void menuOptionJsonsGet_shouldReturnVersionModals() throws Exception {
        mockMvc.perform(post("/menu/optionJsons/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"menuIds":["1848317056370487297"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.optionJsonViews[0].menuId").value("1848317056370487297"))
                .andExpect(jsonPath("$.data.optionJsonViews[0].optionJson.versionModals.title").value("畅享版全新上线"))
                .andExpect(jsonPath("$.data.optionJsonViews[0].optionJson.versionModals.buttons[0].buttonText").value("立即开通"));
    }

    @Test
    @Timeout(60)
    void systemConfigsGet_shouldReturnConfigList() throws Exception {
        mockMvc.perform(post("/systemConfigs/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.configs[0].configKey").value("order.auto_cancel"))
                .andExpect(jsonPath("$.data.configs[0].configValue").exists());
    }

    @Test
    @Timeout(60)
    void channelsGet_shouldReturnChannelRows() throws Exception {
        mockMvc.perform(post("/channels/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.channels[0].channelId").value(1))
                .andExpect(jsonPath("$.data.channels[0].channelName").value("美团"))
                .andExpect(jsonPath("$.data.channels[0].accountId").value(25301))
                .andExpect(jsonPath("$.data.channels[0].poiId").value(11001))
                .andExpect(jsonPath("$.data.channels[0].syncStatus").value("success"));
    }

    @Test
    @Timeout(60)
    void editionResourceGet_shouldReturnEditionFields() throws Exception {
        mockMvc.perform(post("/edition/resource/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.editionId").value("9"))
                .andExpect(jsonPath("$.data.editionName").value("畅享版"))
                .andExpect(jsonPath("$.data.resourceName").value("全域雷达"))
                .andExpect(jsonPath("$.data.expireDateRange").exists())
                .andExpect(jsonPath("$.data.priceText").value("¥0 / 演示环境"))
                .andExpect(jsonPath("$.data.connectorProgress").value("2/2 已连接"));
    }

    @Test
    @Timeout(60)
    void versionSubscriptionOrderSubmit_shouldReturnRedirectContract() throws Exception {
        mockMvc.perform(post("/version/subscription/order/submit")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "editionId":"9",
                                  "duration":"2y",
                                  "quantity":2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.message").value("\u7545\u4eab\u7248\u8d2d\u4e70\u4fe1\u606f\u5df2\u751f\u6210"))
                .andExpect(jsonPath("$.data.redirectTo").value("/version/applicationPayment/detail?plan=delight&duration=2y"))
                .andExpect(jsonPath("$.data.orderNo").exists());
    }

    @Test
    @Timeout(60)
    void versionSubscriptionOrderSubmit_shouldRejectForeignCamp() throws Exception {
        mockMvc.perform(post("/version/subscription/order/submit")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "editionId":"9",
                                  "duration":"2y",
                                  "quantity":2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

}
