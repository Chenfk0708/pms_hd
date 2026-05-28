package com.jeez.zp.platform.controller;

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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AiGlobalDataQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CURRENT_USER_ID = 12001L;
    private static final long ORIGINAL_CAMP_ID = 10001L;
    private static final long ISOLATED_CAMP_ID = 29101L;
    private static final long ISOLATED_POI_ID = 29121L;
    private static final long ROOM_CATEGORY_ID = 29131L;
    private static final long ROOM_ID_1 = 29141L;
    private static final long ROOM_ID_2 = 29142L;
    private static final long CHANNEL_ACCOUNT_ID_1 = 29151L;
    private static final long CHANNEL_ACCOUNT_ID_2 = 29152L;
    private static final long CHANNEL_ACCOUNT_ID_3 = 29153L;
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void ordersStrongReminderPageGet_shouldReturnDerivedReminderPage() throws Exception {
        seedAiGlobalScene();

        mockMvc.perform(post("/orders/strongReminder/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"29101",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.pagination.pageNum").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.data.pagination.total").value(3))
                .andExpect(jsonPath("$.data.list.length()").value(3))
                .andExpect(jsonPath("$.data.list[0].id").value("29172"))
                .andExpect(jsonPath("$.data.list[0].campId").value("29101"))
                .andExpect(jsonPath("$.data.list[0].level").value("high"))
                .andExpect(jsonPath("$.data.list[0].title").value("待处理提醒"))
                .andExpect(jsonPath("$.data.list[0].guestName").value("待接单客人"))
                .andExpect(jsonPath("$.data.list[0].roomName").value("A-102"))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("OUT-ORDER-29172"))
                .andExpect(jsonPath("$.data.list[0].dueAt").value("19:10"))
                .andExpect(jsonPath("$.data.list[0].channel").value("meituan"))
                .andExpect(jsonPath("$.data.list[0].status").value("pending"))
                .andExpect(jsonPath("$.data.list[0].primaryAction").value("order"))
                .andExpect(jsonPath("$.data.list[2].title").value("退款跟进"))
                .andExpect(jsonPath("$.data.list[2].primaryAction").value("status"));
    }

    @Test
    @Timeout(60)
    void radarConfigShopGet_shouldReturnCurrentCampConnectorStatus() throws Exception {
        seedAiGlobalScene();

        mockMvc.perform(post("/radarConfig/shop/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"29101",
                                  "status":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("29101"))
                .andExpect(jsonPath("$.data[0].campId").value("29101"))
                .andExpect(jsonPath("$.data[0].name").value("AI雷达联调门店"))
                .andExpect(jsonPath("$.data[0].connectorStatus").value("warning"))
                .andExpect(jsonPath("$.data[0].radarStatus").value("delay"))
                .andExpect(jsonPath("$.data[0].authorizedChannels.length()").value(2))
                .andExpect(jsonPath("$.data[0].authorizedChannels[0]").value("携程酒店"))
                .andExpect(jsonPath("$.data[0].authorizedChannels[1]").value("美团酒店"))
                .andExpect(jsonPath("$.data[0].updatedAt").isString());
    }

    @Test
    @Timeout(60)
    void aiGlobalDataEndpoints_shouldFallbackInvalidCampIdAndRejectForeignCampAccess() throws Exception {
        seedAiGlobalScene();

        mockMvc.perform(post("/orders/strongReminder/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"camp-up-valley",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pagination.total").value(3));

        mockMvc.perform(post("/radarConfig/shop/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "status":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Timeout(60)
    void roomCategoriesDetailGet_shouldReturnAiGlobalRoomDetailShape() throws Exception {
        seedAiGlobalScene();

        mockMvc.perform(post("/roomCategories/detail/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomCategoryId":"29131"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.roomName").value("AI雷达标准房"))
                .andExpect(jsonPath("$.data.inventory").value(2))
                .andExpect(jsonPath("$.data.staying").value(0))
                .andExpect(jsonPath("$.data.pendingOrders").value(3))
                .andExpect(jsonPath("$.data.occupancyRate").value(0))
                .andExpect(jsonPath("$.data.channelPrices.length()").value(2))
                .andExpect(jsonPath("$.data.channelPrices[0].label").value("携程酒店"))
                .andExpect(jsonPath("$.data.channelPrices[0].price").value(18800))
                .andExpect(jsonPath("$.data.channelPrices[0].status").value("已同步"))
                .andExpect(jsonPath("$.data.channelPrices[1].label").value("美团酒店"))
                .andExpect(jsonPath("$.data.channelPrices[1].price").value(20800))
                .andExpect(jsonPath("$.data.channelPrices[1].status").value("待同步"))
                .andExpect(jsonPath("$.data.guidance.length()").value(3));
    }

    private void seedAiGlobalScene() {
        insertCamp();
        rebindCurrentUserCamp();
        insertPoi();
        insertRoomCategory();
        insertRoom(ROOM_ID_1, "A-101", 1);
        insertRoom(ROOM_ID_2, "A-102", 2);

        insertChannelAccount(CHANNEL_ACCOUNT_ID_1, 5L, "携程酒店", "authorized");
        insertChannelAccount(CHANNEL_ACCOUNT_ID_2, 7L, "美团酒店", "authorized");
        insertChannelAccount(CHANNEL_ACCOUNT_ID_3, 21L, "木鸟民宿", "disabled");

        insertChannelPoiRelation(29161L, CHANNEL_ACCOUNT_ID_1, ISOLATED_POI_ID, "OUT-POI-29121", "synced");
        insertChannelPoiRelation(29162L, CHANNEL_ACCOUNT_ID_2, ISOLATED_POI_ID, "OUT-POI-29121-MT", "delayed");
        insertChannelPoiRelation(29163L, CHANNEL_ACCOUNT_ID_3, ISOLATED_POI_ID, "OUT-POI-29121-MN", "disabled");
        insertChannelRoomCategoryRelation(29164L, CHANNEL_ACCOUNT_ID_1, ROOM_CATEGORY_ID, "OUT-RC-29131-CTRIP", "on_shelf", "approved");
        insertChannelRoomCategoryRelation(29165L, CHANNEL_ACCOUNT_ID_2, ROOM_CATEGORY_ID, "OUT-RC-29131-MT", "off_shelf", "pending");

        LocalDate today = LocalDate.now(SHANGHAI_ZONE);
        insertOrderMain(29171L, CHANNEL_ACCOUNT_ID_1, ROOM_ID_1, "booked", "paid",
                "待入住客人", "13900001001", today, today.plusDays(1), LocalDateTime.of(today, LocalTime.of(18, 40)),
                32800, 32800, 0, 3280, 29520, "channel", "ai global reminder booked");
        insertOrderMain(29172L, CHANNEL_ACCOUNT_ID_2, ROOM_ID_2, "pending", "unpaid",
                "待接单客人", "13900001002", today, today.plusDays(1), LocalDateTime.of(today, LocalTime.of(19, 10)),
                36800, 0, 0, 0, 0, "channel", "ai global reminder pending");
        insertOrderMain(29173L, CHANNEL_ACCOUNT_ID_2, null, "refunding", "paid",
                "退款客人", "13900001003", today.plusDays(1), today.plusDays(2), LocalDateTime.of(today, LocalTime.of(17, 30)),
                28800, 28800, 1000, 2880, 25920, "channel", "ai global reminder refunding");
    }

    private void rebindCurrentUserCamp() {
        jdbcTemplate.update("UPDATE pms_member SET camp_id = ? WHERE user_id = ?", ISOLATED_CAMP_ID, CURRENT_USER_ID);
    }

    private void insertCamp() {
        jdbcTemplate.update("""
                        INSERT INTO pms_camp (
                            camp_id,
                            name,
                            type,
                            city_name,
                            address,
                            contact_number,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                ISOLATED_CAMP_ID,
                "AI雷达联调门店",
                1,
                "深圳",
                "南山区联调路 29 号",
                "0755-2910101",
                1,
                0
        );
    }

    private void insertPoi() {
        jdbcTemplate.update("""
                        INSERT INTO pms_poi (
                            poi_id,
                            camp_id,
                            poi_name,
                            is_availability,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                ISOLATED_POI_ID,
                ISOLATED_CAMP_ID,
                "AI雷达联调门店",
                1,
                1,
                1,
                0
        );
    }

    private void insertRoomCategory() {
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
                            highlight_description,
                            nearby_description,
                            article_description,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                ROOM_CATEGORY_ID,
                ISOLATED_CAMP_ID,
                ISOLATED_POI_ID,
                "AI雷达标准房",
                "AI雷达标准房",
                2,
                2,
                18800L,
                20800L,
                22800L,
                14,
                23,
                12,
                "AI雷达房型",
                "地铁 5 分钟",
                "ai global query test room category",
                1,
                1,
                0
        );
    }

    private void insertRoom(long roomId, String roomName, int sortNo) {
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
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                roomId,
                ISOLATED_CAMP_ID,
                ISOLATED_POI_ID,
                ROOM_CATEGORY_ID,
                roomName,
                "normal",
                "overnight",
                "clean",
                1,
                sortNo,
                0
        );
    }

    private void insertChannelAccount(long accountId, long channelId, String channelName, String status) {
        jdbcTemplate.update("""
                        INSERT INTO channel_account (
                            account_id,
                            camp_id,
                            channel_id,
                            channel_name,
                            account_name,
                            out_account_id,
                            status
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                accountId,
                ISOLATED_CAMP_ID,
                channelId,
                channelName,
                channelName + "账号",
                "OUT-" + accountId,
                status
        );
    }

    private void insertChannelPoiRelation(long id, long accountId, long poiId, String outPoiId, String syncStatus) {
        jdbcTemplate.update("""
                        INSERT INTO channel_poi_rel (
                            id,
                            camp_id,
                            account_id,
                            poi_id,
                            out_poi_id,
                            sync_status
                        ) VALUES (?, ?, ?, ?, ?, ?)
                        """,
                id,
                ISOLATED_CAMP_ID,
                accountId,
                poiId,
                outPoiId,
                syncStatus
        );
    }

    private void insertChannelRoomCategoryRelation(
            long id,
            long accountId,
            long roomCategoryId,
            String outRoomCategoryId,
            String shelfStatus,
            String auditStatus
    ) {
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
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                id,
                ISOLATED_CAMP_ID,
                accountId,
                roomCategoryId,
                outRoomCategoryId,
                "calendar_room",
                shelfStatus,
                auditStatus
        );
    }

    private void insertOrderMain(
            long orderId,
            Long channelAccountId,
            Long roomId,
            String status,
            String paymentStatus,
            String guestName,
            String guestMobile,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createdAt,
            long totalPriceCent,
            long totalPayPriceCent,
            long refundPriceCent,
            long commissionPriceCent,
            long settlementAmountCent,
            String sourceType,
            String remark
    ) {
        jdbcTemplate.update("""
                        INSERT INTO order_main (
                            order_id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            room_id,
                            channel_id,
                            goods_id,
                            order_no,
                            out_order_no,
                            order_type,
                            status,
                            guest_name,
                            guest_mobile,
                            start_at,
                            end_at,
                            day_num,
                            total_price_cent,
                            discount_price_cent,
                            total_pay_price_cent,
                            refund_price_cent,
                            commission_price_cent,
                            payment_fee_cent,
                            platform_service_fee_cent,
                            distribution_commission_cent,
                            settlement_amount_cent,
                            payment_status,
                            payment_type_id,
                            payment_way_id,
                            source_type,
                            remark,
                            created_at,
                            updated_at,
                            created_by,
                            updated_by,
                            is_deleted,
                            version_no
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0)
                        """,
                orderId,
                ISOLATED_CAMP_ID,
                ISOLATED_POI_ID,
                ROOM_CATEGORY_ID,
                roomId,
                channelAccountId,
                null,
                "ORDER-" + orderId,
                "OUT-ORDER-" + orderId,
                "daily_room",
                status,
                guestName,
                guestMobile,
                Timestamp.valueOf(startDate.atTime(LocalTime.of(14, 0))),
                Timestamp.valueOf(endDate.atTime(LocalTime.of(12, 0))),
                Math.max(1, (int) (endDate.toEpochDay() - startDate.toEpochDay())),
                totalPriceCent,
                0,
                totalPayPriceCent,
                refundPriceCent,
                commissionPriceCent,
                0,
                0,
                commissionPriceCent,
                settlementAmountCent,
                paymentStatus,
                17101L,
                17202L,
                sourceType,
                remark,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(15)),
                CURRENT_USER_ID,
                CURRENT_USER_ID
        );
    }
}
