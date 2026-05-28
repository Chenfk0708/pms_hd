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

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WorkspaceDashboardQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CURRENT_USER_ID = 12001L;
    private static final long ORIGINAL_CAMP_ID = 10001L;
    private static final long ISOLATED_CAMP_ID = 19101L;
    private static final long ISOLATED_POI_ID = 19121L;
    private static final long ROOM_CATEGORY_ID = 19221L;
    private static final long ROOM_ID_1 = 19321L;
    private static final long ROOM_ID_2 = 19322L;
    private static final long ROOM_ID_3 = 19323L;
    private static final long ROOM_ID_4 = 19324L;
    private static final long ROOM_ID_5 = 19325L;
    private static final long CHANNEL_ACCOUNT_ID_1 = 19421L;
    private static final long CHANNEL_ACCOUNT_ID_2 = 19422L;
    private static final long CHANNEL_ACCOUNT_ID_3 = 19423L;
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void reportHomePageV2_shouldReturnWorkspaceSummary() throws Exception {
        seedWorkspaceScene();

        mockMvc.perform(post("/report/homePage/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19101"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.nowPredictCheckIn").value(1))
                .andExpect(jsonPath("$.data.nowAlreadyCheckIn").value(2))
                .andExpect(jsonPath("$.data.nowPredictCheckOut").value(1))
                .andExpect(jsonPath("$.data.nowOnSaleNum").value(1))
                .andExpect(jsonPath("$.data.userBusyRepairNum").value(1))
                .andExpect(jsonPath("$.data.dirtyNum").value(1))
                .andExpect(jsonPath("$.data.exceptionOrderNum").value(2))
                .andExpect(jsonPath("$.data.nowIncome").value(43200));
    }

    @Test
    @Timeout(60)
    void reportHomePageV2_shouldFallbackInvalidCampIdAndRejectForeignCamp() throws Exception {
        seedWorkspaceScene();

        mockMvc.perform(post("/report/homePage/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"mock-camp-workspace"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nowPredictCheckIn").value(1));

        mockMvc.perform(post("/report/homePage/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Timeout(60)
    void reportAccommodationManagementAnalysisGet_shouldReturnRevenueTrendAndOrigins() throws Exception {
        seedWorkspaceScene();

        mockMvc.perform(post("/report/accommodation/management/analysis/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19101",
                                  "startDate":"2026-05-11",
                                  "endDate":"2026-05-13",
                                  "predictStartDate":"2026-05-11",
                                  "predictEndDate":"2026-05-14"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.businessIncome").value(closeTo(810.00, 0.001)))
                .andExpect(jsonPath("$.data.roomFeePriceIncludingCommission").value(closeTo(990.00, 0.001)))
                .andExpect(jsonPath("$.data.writeDownIncome").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.otherOrderExpense").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.occ").value(closeTo(26.67, 0.001)))
                .andExpect(jsonPath("$.data.adr").value(closeTo(202.50, 0.001)))
                .andExpect(jsonPath("$.data.revPar").value(closeTo(54.00, 0.001)))
                .andExpect(jsonPath("$.data.openRoomCount").value(4))
                .andExpect(jsonPath("$.data.roomCount").value(15))
                .andExpect(jsonPath("$.data.allDayOpenRoomCount").value(4))
                .andExpect(jsonPath("$.data.hourOpenRoomCount").value(0))
                .andExpect(jsonPath("$.data.growthTrendAnalysisList.length()").value(3))
                .andExpect(jsonPath("$.data.growthTrendAnalysisList[0].date").value("2026-05-11"))
                .andExpect(jsonPath("$.data.growthTrendAnalysisList[0].businessIncome").value(closeTo(180.00, 0.001)))
                .andExpect(jsonPath("$.data.growthTrendAnalysisList[0].occ").value(closeTo(20.00, 0.001)))
                .andExpect(jsonPath("$.data.growthTrendAnalysisList[1].date").value("2026-05-12"))
                .andExpect(jsonPath("$.data.growthTrendAnalysisList[1].businessIncome").value(closeTo(360.00, 0.001)))
                .andExpect(jsonPath("$.data.growthTrendAnalysisList[1].openRoomCount").value(2))
                .andExpect(jsonPath("$.data.growthTrendAnalysisList[2].date").value("2026-05-13"))
                .andExpect(jsonPath("$.data.growthTrendAnalysisList[2].businessIncome").value(closeTo(270.00, 0.001)))
                .andExpect(jsonPath("$.data.orderOriginAnalysisList.length()").value(2))
                .andExpect(jsonPath("$.data.orderOriginAnalysisList[0].channelName").value("携程"))
                .andExpect(jsonPath("$.data.orderOriginAnalysisList[0].orderCount").value(2))
                .andExpect(jsonPath("$.data.orderOriginAnalysisList[1].channelName").value("路客云聚合"))
                .andExpect(jsonPath("$.data.orderOriginAnalysisList[1].orderCount").value(1));
    }

    @Test
    @Timeout(60)
    void campFlowGet_shouldReturnConnectedChannels() throws Exception {
        seedWorkspaceScene();

        mockMvc.perform(post("/campFlow/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19101"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.isOpenFlow").value(1))
                .andExpect(jsonPath("$.data.channelInfos.length()").value(3))
                .andExpect(jsonPath("$.data.channelInfos[0].channelName").value("路客云聚合"))
                .andExpect(jsonPath("$.data.channelInfos[0].isApplyOpen").value(1))
                .andExpect(jsonPath("$.data.channelInfos[1].channelName").value("携程"))
                .andExpect(jsonPath("$.data.channelInfos[1].isApplyOpen").value(1))
                .andExpect(jsonPath("$.data.channelInfos[2].channelName").value("木鸟"))
                .andExpect(jsonPath("$.data.channelInfos[2].isApplyOpen").value(0));
    }

    @Test
    @Timeout(60)
    void ordersGet_shouldReturnWorkspaceOrdersByTabAndKeyword() throws Exception {
        seedWorkspaceScene();

        mockMvc.perform(post("/orders/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19101",
                                  "orderType":"11",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "keyword":""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.data.pagination.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].guestMobile").value("13900000001"))
                .andExpect(jsonPath("$.data.list[0].roomName").value("A-101"))
                .andExpect(jsonPath("$.data.list[0].dayNum").value(1))
                .andExpect(jsonPath("$.data.list[0].startTime").isNumber())
                .andExpect(jsonPath("$.data.list[0].endTime").isNumber())
                .andExpect(jsonPath("$.data.list[0].orderDetailDisplayStateName").exists());

        mockMvc.perform(post("/orders/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19101",
                                  "orderType":"12",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "keyword":"在住"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].roomName").value("A-103"));
    }

    @Test
    @Timeout(60)
    void ordersGet_shouldFallbackInvalidCampIdAndRejectForeignCamp() throws Exception {
        seedWorkspaceScene();

        mockMvc.perform(post("/orders/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"mock-camp-workspace",
                                  "orderType":"13",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].roomName").value("A-102"));

        mockMvc.perform(post("/orders/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "orderType":"11",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Timeout(60)
    void memoPageGet_shouldReturnEmptyWorkspaceShape() throws Exception {
        seedWorkspaceScene();

        mockMvc.perform(post("/memo/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19101",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "isHandle":0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.data.pagination.total").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(0));
    }

    @Test
    @Timeout(60)
    void backlogsGet_shouldReturnDerivedWorkspaceTodoItems() throws Exception {
        seedWorkspaceScene();

        mockMvc.perform(post("/backlogs/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19101"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].content").value(containsString("\"title\"")))
                .andExpect(jsonPath("$.data[0].content").value(containsString("\"button\"")))
                .andExpect(jsonPath("$.data[1].content").value(containsString("\"title\"")));
    }

    private void seedWorkspaceScene() {
        insertCamp();
        rebindCurrentUserCamp();
        insertPoi();
        insertRoomCategory();
        insertRoom(ROOM_ID_1, "A-101", "normal", "clean", 1);
        insertRoom(ROOM_ID_2, "A-102", "normal", "dirty", 2);
        insertRoom(ROOM_ID_3, "A-103", "normal", "clean", 3);
        insertRoom(ROOM_ID_4, "A-104", "repair", "clean", 4);
        insertRoom(ROOM_ID_5, "A-105", "normal", "clean", 5);

        insertChannelAccount(CHANNEL_ACCOUNT_ID_1, 17L, "路客云聚合", "authorized");
        insertChannelAccount(CHANNEL_ACCOUNT_ID_2, 5L, "携程", "authorized");
        insertChannelAccount(CHANNEL_ACCOUNT_ID_3, 21L, "木鸟", "disabled");

        LocalDate today = LocalDate.now(SHANGHAI_ZONE);
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate futureDay = today.plusDays(3);

        insertOrderMain(19621L, CHANNEL_ACCOUNT_ID_1, ROOM_ID_1, "booked", "paid",
                "今日预抵客人", "13900000001", today, tomorrow, today.atTime(8, 30),
                30000, 30000, 0, 3000, 27000, "channel", "workspace summary booked");
        insertOrderMain(19622L, CHANNEL_ACCOUNT_ID_2, ROOM_ID_2, "checked_in", "paid",
                "今日预离客人", "13900000002", yesterday, today, yesterday.atTime(9, 20),
                24000, 24000, 0, 2400, 21600, "channel", "workspace summary checkout");
        insertOrderMain(19623L, CHANNEL_ACCOUNT_ID_2, ROOM_ID_3, "checked_in", "paid",
                "在住客人", "13900000003", yesterday, tomorrow, yesterday.atTime(10, 10),
                36000, 36000, 0, 3600, 32400, "channel", "workspace summary staying");

        insertOrderMain(19631L, CHANNEL_ACCOUNT_ID_1, ROOM_ID_1, "checked_in", "paid",
                "趋势客人A", "13900000101", LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 13), LocalDateTime.of(2026, 5, 10, 12, 0),
                40000, 40000, 0, 4000, 36000, "channel", "workspace analysis order A");
        insertOrderMain(19632L, CHANNEL_ACCOUNT_ID_2, ROOM_ID_2, "checked_in", "paid",
                "趋势客人B", "13900000102", LocalDate.of(2026, 5, 12), LocalDate.of(2026, 5, 13), LocalDateTime.of(2026, 5, 11, 14, 0),
                20000, 20000, 0, 2000, 18000, "channel", "workspace analysis order B");
        insertOrderMain(19633L, CHANNEL_ACCOUNT_ID_2, ROOM_ID_3, "booked", "paid",
                "趋势客人C", "13900000103", LocalDate.of(2026, 5, 13), LocalDate.of(2026, 5, 14), LocalDateTime.of(2026, 5, 12, 16, 0),
                30000, 30000, 0, 3000, 27000, "channel", "workspace analysis order C");

        insertOrderMain(19641L, null, null, "refunding", "paid",
                "异常客人A", "13900000201", futureDay, futureDay.plusDays(1), today.atTime(11, 0),
                18000, 18000, 6000, 0, 18000, "frontdesk", "workspace exception refunding");
        insertOrderMain(19642L, null, null, "cancelled", "cancelled",
                "异常客人B", "13900000202", futureDay.plusDays(1), futureDay.plusDays(2), today.atTime(12, 0),
                0, 0, 0, 0, 0, "phone", "workspace exception cancelled");
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
                "TDD首页门店",
                1,
                "深圳",
                "南山区测试路 8 号",
                "0755-1910101",
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
                "TDD首页门店",
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
                "TDD首页房型",
                "TDD首页房型",
                5,
                2,
                18800L,
                20800L,
                22800L,
                14,
                23,
                12,
                "首页分析房型",
                "近地铁",
                "workspace query test room category",
                1,
                1,
                0
        );
    }

    private void insertRoom(long roomId, String roomName, String lockStatus, String cleanStatus, int sortNo) {
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
                lockStatus,
                "overnight",
                cleanStatus,
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
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                CURRENT_USER_ID,
                CURRENT_USER_ID
        );
    }
}
