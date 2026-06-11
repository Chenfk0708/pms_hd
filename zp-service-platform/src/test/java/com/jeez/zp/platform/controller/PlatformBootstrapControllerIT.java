package com.jeez.zp.platform.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PlatformBootstrapControllerIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetSeedCampAndPoi() {
        jdbcTemplate.update("""
                        UPDATE pms_camp
                        SET name = ?,
                            city_name = ?,
                            address = ?,
                            contact_number = ?,
                            status = ?,
                            is_deleted = ?
                        WHERE camp_id = ?
                        """,
                "路客云演示租户",
                "深圳市",
                "深圳市南山区科技园演示地址 1 号",
                "13800000001",
                1,
                0,
                10001L
        );
        jdbcTemplate.update("""
                        UPDATE pms_poi
                        SET poi_name = ?,
                            poi_type = ?,
                            is_availability = ?,
                            sort_no = ?,
                            address = ?,
                            contact_number = ?,
                            city_name = NULL,
                            city_path = NULL,
                            street_address = NULL,
                            community_name = NULL,
                            unit_no = NULL,
                            full_address = NULL,
                            tags_json = NULL,
                            plain_intro = NULL,
                            rich_intro = NULL,
                            cover_image_data_url = NULL,
                            photo_count = ?,
                            status = ?,
                            is_deleted = ?
                        WHERE poi_id = ?
                        """,
                "路客云演示门店",
                "hotel",
                1,
                1,
                "深圳市南山区科技园演示地址 1 号",
                "13800000001",
                0,
                1,
                0,
                11001L
        );
    }

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
    @Transactional
    void campSave_shouldPersistEditableCampFields() throws Exception {
        jdbcTemplate.update(
                "DELETE FROM system_config WHERE camp_id = ? AND config_key = ? AND config_scope = 'camp'",
                10001L,
                "hudson.campInfo.tags"
        );

        mockMvc.perform(post("/camp/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "campName":"联调门店-已保存",
                                  "name":"联调门店-已保存",
                                  "phone":"13900001111",
                                  "contactNumber":"13900001111",
                                  "cityName":"深圳市",
                                  "cityPath":"深圳市",
                                  "address":"深圳市南山区联调路 88 号",
                                  "fullAddress":"深圳市南山区联调路 88 号",
                                  "tags":["联调标签","阳台观景"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.campId").value(10001))
                .andExpect(jsonPath("$.data.name").value("联调门店-已保存"))
                .andExpect(jsonPath("$.data.contactNumber").value("13900001111"))
                .andExpect(jsonPath("$.data.address").value("深圳市南山区联调路 88 号"))
                .andExpect(jsonPath("$.data.tags", hasItem("联调标签")));

        mockMvc.perform(post("/camp/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("联调门店-已保存"))
                .andExpect(jsonPath("$.data.contactNumber").value("13900001111"))
                .andExpect(jsonPath("$.data.address").value("深圳市南山区联调路 88 号"))
                .andExpect(jsonPath("$.data.tags", hasItem("联调标签")));
    }

    @Test
    @Timeout(60)
    void campSave_shouldRejectInvalidContactPhoneBeforePersisting() throws Exception {
        mockMvc.perform(post("/camp/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "campName":"非法联系电话门店",
                                  "name":"非法联系电话门店",
                                  "phone":"12000000000",
                                  "contactNumber":"12000000000",
                                  "cityName":"深圳市",
                                  "address":"深圳市南山区联调路 88 号"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("联系电话格式不正确"));

        org.junit.jupiter.api.Assertions.assertEquals("路客云演示门店", jdbcTemplate.queryForObject("""
                SELECT poi_name
                FROM pms_poi
                WHERE poi_id = 11001
                """, String.class));
        org.junit.jupiter.api.Assertions.assertEquals("13800000001", jdbcTemplate.queryForObject("""
                SELECT contact_number
                FROM pms_poi
                WHERE poi_id = 11001
                """, String.class));
    }

    @Test
    @Timeout(60)
    @Transactional
    void campSave_shouldAcceptPrefixedMainlandMobileContactPhoneBeforePersisting() throws Exception {
        mockMvc.perform(post("/camp/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "storeId":"11001",
                                  "campName":"前缀电话门店",
                                  "name":"前缀电话门店",
                                  "phone":"+86-18123941382",
                                  "contactNumber":"+86-18123941382",
                                  "cityName":"深圳市",
                                  "address":"深圳市南山区联调路 88 号"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.contactNumber").value("+86-18123941382"));

        org.junit.jupiter.api.Assertions.assertEquals("+86-18123941382", jdbcTemplate.queryForObject("""
                SELECT contact_number
                FROM pms_poi
                WHERE poi_id = 11001
                """, String.class));
    }

    @Test
    @Timeout(60)
    @Transactional
    void campSave_shouldPersistPoiSpecificEditableFields() throws Exception {
        mockMvc.perform(post("/camp/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "storeId":"11001",
                                  "campName":"TDD Store Saved",
                                  "name":"TDD Store Saved",
                                  "typeName":"Apartment",
                                  "campTypeName":"Apartment",
                                  "phone":"13900001111",
                                  "contactNumber":"13900001111",
                                  "cityName":"Shenzhen",
                                  "cityPath":"Guangdong/Shenzhen/Nanshan",
                                  "address":"TDD Road 88",
                                  "streetAddress":"TDD Road",
                                  "communityName":"TDD Garden",
                                  "unitNo":"8-808",
                                  "fullAddress":"TDD Road 88",
                                  "tags":["integration","balcony"],
                                  "plainIntro":"Plain intro saved",
                                  "richIntro":"<p>Rich intro saved</p>",
                                  "coverImageDataUrl":"data:image/png;base64,abc123",
                                  "photoCount":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.campId").value(10001))
                .andExpect(jsonPath("$.data.poiId").value(11001))
                .andExpect(jsonPath("$.data.name").value("TDD Store Saved"))
                .andExpect(jsonPath("$.data.poiName").value("TDD Store Saved"))
                .andExpect(jsonPath("$.data.typeName").value("Apartment"))
                .andExpect(jsonPath("$.data.contactNumber").value("13900001111"))
                .andExpect(jsonPath("$.data.cityName").value("Shenzhen"))
                .andExpect(jsonPath("$.data.cityPath").value("Guangdong/Shenzhen/Nanshan"))
                .andExpect(jsonPath("$.data.address").value("TDD Road 88"))
                .andExpect(jsonPath("$.data.streetAddress").value("TDD Road"))
                .andExpect(jsonPath("$.data.communityName").value("TDD Garden"))
                .andExpect(jsonPath("$.data.unitNo").value("8-808"))
                .andExpect(jsonPath("$.data.fullAddress").value("TDD Road 88"))
                .andExpect(jsonPath("$.data.tags", hasItem("integration")))
                .andExpect(jsonPath("$.data.plainIntro").value("Plain intro saved"))
                .andExpect(jsonPath("$.data.richIntro").value("<p>Rich intro saved</p>"))
                .andExpect(jsonPath("$.data.coverImageDataUrl").value("data:image/png;base64,abc123"))
                .andExpect(jsonPath("$.data.photoCount").value(1));

        mockMvc.perform(post("/camp/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","storeId":"11001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.poiId").value(11001))
                .andExpect(jsonPath("$.data.name").value("TDD Store Saved"))
                .andExpect(jsonPath("$.data.poiName").value("TDD Store Saved"))
                .andExpect(jsonPath("$.data.typeName").value("Apartment"))
                .andExpect(jsonPath("$.data.contactNumber").value("13900001111"))
                .andExpect(jsonPath("$.data.cityPath").value("Guangdong/Shenzhen/Nanshan"))
                .andExpect(jsonPath("$.data.streetAddress").value("TDD Road"))
                .andExpect(jsonPath("$.data.communityName").value("TDD Garden"))
                .andExpect(jsonPath("$.data.unitNo").value("8-808"))
                .andExpect(jsonPath("$.data.fullAddress").value("TDD Road 88"))
                .andExpect(jsonPath("$.data.tags", hasItem("integration")))
                .andExpect(jsonPath("$.data.plainIntro").value("Plain intro saved"))
                .andExpect(jsonPath("$.data.richIntro").value("<p>Rich intro saved</p>"))
                .andExpect(jsonPath("$.data.coverImageDataUrl").value("data:image/png;base64,abc123"))
                .andExpect(jsonPath("$.data.photoCount").value(1));

        mockMvc.perform(post("/select/poi/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageSize":999,
                                  "pageNum":1,
                                  "isAvailability":"1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].poiId").value("11001"))
                .andExpect(jsonPath("$.data.list[0].poiName").value("TDD Store Saved"))
                .andExpect(jsonPath("$.data.list[0].poiType").value("Apartment"))
                .andExpect(jsonPath("$.data.list[0].address").value("TDD Road 88"))
                .andExpect(jsonPath("$.data.list[0].contactNumber").value("13900001111"))
                .andExpect(jsonPath("$.data.list[0].cityPath").value("Guangdong/Shenzhen/Nanshan"))
                .andExpect(jsonPath("$.data.list[0].coverImageDataUrl").value("data:image/png;base64,abc123"))
                .andExpect(jsonPath("$.data.list[0].photoCount").value(1))
                .andExpect(jsonPath("$.data.list[0].tagsJson").value("[\"integration\",\"balcony\"]"));
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
