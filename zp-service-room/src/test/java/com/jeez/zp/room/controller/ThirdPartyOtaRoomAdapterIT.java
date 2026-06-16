package com.jeez.zp.room.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "jeez.channel.callback.test-token=channel-callback-test-token")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ThirdPartyOtaRoomAdapterIT {

    private static final String TOKEN_HEADER = "X-Channel-Test-Token";
    private static final String OPERATOR_HEADER = "X-Channel-Operator-Id";
    private static final String TEST_TOKEN = "channel-callback-test-token";
    private static final String CHANNEL_CODE = "meituan_hotel";
    private static final long CAMP_ID = 10001L;
    private static final long POI_ID = 11001L;
    private static final long ROOM_CATEGORY_ID = 168001L;
    private static final long ROOM_ID = 168101L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void thirdPartyCatalogAndStatusEndpoints_shouldReturnRoomDataWithoutInternalGatewayHeaders() throws Exception {
        seedOtaRoomData();

        mockMvc.perform(post("/channelCallbacks/{channelCode}/pois/page/get", CHANNEL_CODE)
                        .header(TOKEN_HEADER, TEST_TOKEN)
                        .header(OPERATOR_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].poiId").value(String.valueOf(POI_ID)));

        mockMvc.perform(post("/channelCallbacks/{channelCode}/roomCategories/page/get", CHANNEL_CODE)
                        .header(TOKEN_HEADER, TEST_TOKEN)
                        .header(OPERATOR_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "poiId":"11001",
                                  "keyword":"OTA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value(String.valueOf(ROOM_CATEGORY_ID)));

        mockMvc.perform(post("/channelCallbacks/{channelCode}/rooms/page/get", CHANNEL_CODE)
                        .header(TOKEN_HEADER, TEST_TOKEN)
                        .header(OPERATOR_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "roomCategoryIds":["168001"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].roomId").value(String.valueOf(ROOM_ID)));

        mockMvc.perform(post("/channelCallbacks/{channelCode}/inventory/get", CHANNEL_CODE)
                        .header(TOKEN_HEADER, TEST_TOKEN)
                        .header(OPERATOR_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "startDate":"2026-06-20",
                                  "days":2,
                                  "roomCategoryIds":["168001"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value(String.valueOf(ROOM_CATEGORY_ID)))
                .andExpect(jsonPath("$.data.list[0].inventory").value(3));

        mockMvc.perform(post("/channelCallbacks/{channelCode}/rates/get", CHANNEL_CODE)
                        .header(TOKEN_HEADER, TEST_TOKEN)
                        .header(OPERATOR_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "channelIds":["31"],
                                  "roomCategoryIds":["168001"],
                                  "date":"2026-06-20",
                                  "days":2,
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value(String.valueOf(ROOM_CATEGORY_ID)))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("OTA\u9002\u914D\u623F\u578B"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryProductName").value("OTA\u9002\u914D\u623F\u578B<\u65E0\u65E9>"))
                .andExpect(jsonPath("$.data.list[0].channelId").value("31"))
                .andExpect(jsonPath("$.data.list[0].channelName").value("Meituan Hotel"))
                .andExpect(jsonPath("$.data.list[0].expressValue").value("*0.9"))
                .andExpect(jsonPath("$.data.list[0].normalPrice").value(28800))
                .andExpect(jsonPath("$.data.list[0].normalActualSalePrice").value(25920))
                .andExpect(jsonPath("$.data.list[0].statusViews[0].date").value("2026-06-20"))
                .andExpect(jsonPath("$.data.list[0].statusViews[0].totalStock").value(1))
                .andExpect(jsonPath("$.data.list[0].statusViews[0].price").value(28800))
                .andExpect(jsonPath("$.data.list[0].statusViews[0].salePrice").value(26600))
                .andExpect(jsonPath("$.data.list[0].statusViews[1].date").value("2026-06-21"))
                .andExpect(jsonPath("$.data.list[0].statusViews[1].totalStock").value(1))
                .andExpect(jsonPath("$.data.list[0].statusViews[1].price").value(28800))
                .andExpect(jsonPath("$.data.list[0].statusViews[1].salePrice").value(25920));
    }

    @Test
    @Timeout(60)
    void thirdPartyRoomAdapter_shouldRejectMissingToken() throws Exception {
        mockMvc.perform(post("/channelCallbacks/{channelCode}/rooms/page/get", CHANNEL_CODE)
                        .header(OPERATOR_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001","pageNum":1,"pageSize":10}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("第三方渠道回调认证失败"));
    }

    private void seedOtaRoomData() {
        jdbcTemplate.update("DELETE FROM room_price_snapshot WHERE id IN (?, ?)", 168301L, 168302L);
        jdbcTemplate.update("DELETE FROM channel_product_price_coefficient WHERE camp_id = ? AND room_category_id = ?", CAMP_ID, ROOM_CATEGORY_ID);
        jdbcTemplate.update("DELETE FROM room_status_daily WHERE id IN (?, ?)", 168201L, 168202L);
        jdbcTemplate.update("DELETE FROM room WHERE room_id = ?", ROOM_ID);
        jdbcTemplate.update("DELETE FROM channel_room_category_rel WHERE id = ?", 168401L);
        jdbcTemplate.update("DELETE FROM channel_poi_rel WHERE id = ?", 168402L);
        jdbcTemplate.update("DELETE FROM channel_account WHERE account_id = ?", 168501L);
        jdbcTemplate.update("DELETE FROM room_category WHERE room_category_id = ?", ROOM_CATEGORY_ID);

        jdbcTemplate.update("""
                        INSERT INTO room_category (
                            room_category_id,
                            camp_id,
                            poi_id,
                            name,
                            display_name,
                            room_count,
                            guest_count,
                            weekday_price_cent,
                            weekend_price_cent,
                            holiday_price_cent,
                            earliest_check_in_hour,
                            latest_check_in_hour,
                            latest_check_out_hour,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 0)
                        """,
                ROOM_CATEGORY_ID,
                CAMP_ID,
                POI_ID,
                "OTA适配房型",
                "OTA适配房型",
                1,
                2,
                28800L,
                30800L,
                32800L,
                14,
                23,
                12,
                1
        );
        jdbcTemplate.update("""
                        INSERT INTO room (
                            room_id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            room_name,
                            lock_status,
                            sale_type,
                            clean_status,
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, 'online', 'normal', 'clean', 1, 1, 0)
                        """,
                ROOM_ID,
                CAMP_ID,
                POI_ID,
                ROOM_CATEGORY_ID,
                "OTA-801"
        );
        jdbcTemplate.update("""
                        INSERT INTO room_status_daily (
                            id,
                            camp_id,
                            poi_id,
                            biz_date,
                            room_category_id,
                            availability_count,
                            open_room_count,
                            room_sale_count,
                            close_room_count,
                            user_busy_count,
                            retain_count,
                            repair_count,
                            vacant_count,
                            pre_come_count,
                            live_count,
                            pre_leave_count,
                            clean_count,
                            dirty_count
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 0, 0, ?, 0, 0, 0, ?, 0)
                        """,
                168201L,
                CAMP_ID,
                POI_ID,
                "2026-06-20",
                ROOM_CATEGORY_ID,
                3,
                3,
                0,
                3,
                3
        );
        jdbcTemplate.update("""
                        INSERT INTO room_status_daily (
                            id,
                            camp_id,
                            poi_id,
                            biz_date,
                            room_category_id,
                            availability_count,
                            open_room_count,
                            room_sale_count,
                            close_room_count,
                            user_busy_count,
                            retain_count,
                            repair_count,
                            vacant_count,
                            pre_come_count,
                            live_count,
                            pre_leave_count,
                            clean_count,
                            dirty_count
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, 0, 0, 0, ?, 0, 0, 0, ?, 0)
                        """,
                168202L,
                CAMP_ID,
                POI_ID,
                "2026-06-21",
                ROOM_CATEGORY_ID,
                2,
                2,
                0,
                2,
                2
        );
        jdbcTemplate.update("""
                        INSERT INTO channel_account (
                            account_id,
                            camp_id,
                            channel_id,
                            channel_name,
                            account_name,
                            out_account_id,
                            status,
                            config_json
                        ) VALUES (?, ?, ?, ?, ?, ?, 'authorized', CAST(? AS JSON))
                        """,
                168501L,
                CAMP_ID,
                31L,
                "Meituan Hotel",
                "Meituan OTA Adapter Test",
                "OUT-OTA-168501",
                "{\"channelCode\":\"meituan_hotel\"}"
        );
        jdbcTemplate.update("""
                        INSERT INTO channel_poi_rel (
                            id,
                            camp_id,
                            account_id,
                            poi_id,
                            out_poi_id,
                            sync_status
                        ) VALUES (?, ?, ?, ?, ?, 'synced')
                        """,
                168402L,
                CAMP_ID,
                168501L,
                POI_ID,
                "OUT-OTA-POI-11001"
        );
        jdbcTemplate.update("""
                        INSERT INTO channel_room_category_rel (
                            id,
                            camp_id,
                            account_id,
                            room_category_id,
                            out_room_category_id,
                            project_type,
                            shelf_status,
                            audit_status
                        ) VALUES (?, ?, ?, ?, ?, 'calendar_room', 'on_shelf', 'approved')
                        """,
                168401L,
                CAMP_ID,
                168501L,
                ROOM_CATEGORY_ID,
                "OUT-OTA-RC-168001"
        );
        jdbcTemplate.update("""
                        INSERT INTO channel_product_price_coefficient (
                            id,
                            camp_id,
                            room_category_id,
                            channel_id,
                            product_name,
                            operator_type,
                            coefficient_value,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, ?, ?, ?, '*', ?, NOW(), NOW())
                        """,
                168601L,
                CAMP_ID,
                ROOM_CATEGORY_ID,
                31L,
                "OTA适配房型<无早>",
                "0.9"
        );
        jdbcTemplate.update("""
                        INSERT INTO room_price_snapshot (
                            id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            biz_date,
                            price_type,
                            channel_id,
                            price_cent,
                            currency,
                            status,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, ?, ?, ?, 'channel', ?, ?, 'CNY', 'active', NOW(), NOW())
                        """,
                168301L,
                CAMP_ID,
                POI_ID,
                ROOM_CATEGORY_ID,
                "2026-06-20",
                31L,
                26600L
        );
    }
}
