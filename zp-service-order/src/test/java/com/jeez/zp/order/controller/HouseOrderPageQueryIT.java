package com.jeez.zp.order.controller;

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
class HouseOrderPageQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final long CAMP_ID = 10001L;
    private static final long CHANNEL_ACCOUNT_ID = 29401L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void ordersHousePageGet_shouldReturnTodayCheckInOrdersWithNestedDetails() throws Exception {
        seedOrders();

        mockMvc.perform(post("/orders/house/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "orderType":"11",
                                  "isLt":0,
                                  "searchContent":"House Page"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(20))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].orderId").value("39101"))
                .andExpect(jsonPath("$.data.list[0].outOrderId").value("OUT-HOUSE-PAGE-39101"))
                .andExpect(jsonPath("$.data.list[0].channelId").value("17"))
                .andExpect(jsonPath("$.data.list[0].channelName").value("Direct OTA"))
                .andExpect(jsonPath("$.data.list[0].orderChannelName").value("Direct OTA"))
                .andExpect(jsonPath("$.data.list[0].guestName").value("House Page Alpha"))
                .andExpect(jsonPath("$.data.list[0].guestMobile").value("13939101001"))
                .andExpect(jsonPath("$.data.list[0].orderState").value(2))
                .andExpect(jsonPath("$.data.list[0].refundDisplayState").value(0))
                .andExpect(jsonPath("$.data.list[0].totalRoomPrice").value(26800))
                .andExpect(jsonPath("$.data.list[0].totalPayPrice").value(26800))
                .andExpect(jsonPath("$.data.list[0].commissionPrice").value(2680))
                .andExpect(jsonPath("$.data.list[0].debtPrice").value(0))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].poiName").exists())
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].roomCategoryName").exists())
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].roomCategoryProductName").exists())
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].roomName").value(OrderTestCatalogFixture.STANDARD_ROOM_NAME))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].checkInDate").isNumber())
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].checkOutDate").isNumber())
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].orderDetailDisplayState").value(1))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].isArrangeRoom").value(1))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].isOccupation").value(1))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].isStatistics").value(1));
    }

    @Test
    @Timeout(60)
    void ordersPageGetLegacyAlias_shouldReturnHousePageForCurrentFrontend() throws Exception {
        seedOrders();

        mockMvc.perform(post("/orders/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "orderType":"11",
                                  "isLt":0,
                                  "searchContent":"House Page Alpha"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].orderId").value("39101"))
                .andExpect(jsonPath("$.data.list[0].guestName").value("House Page Alpha"));
    }

    @Test
    @Timeout(60)
    void ordersHousePageGet_shouldReturnTargetStatusCodesForCheckedInAndCompletedOrders() throws Exception {
        seedOrders();
        LocalDate today = LocalDate.now(SHANGHAI_ZONE);
        LocalDate tomorrow = today.plusDays(1);
        insertOrderMain(
                39103L,
                "checked_in",
                "paid",
                "House Page Checked In",
                "13939103003",
                today.minusDays(1),
                tomorrow,
                today.atTime(11, 0),
                53600,
                53600,
                0,
                "checked-in order"
        );
        insertOrderMain(
                39104L,
                "completed",
                "paid",
                "House Page Completed",
                "13939104004",
                today.minusDays(2),
                today.minusDays(1),
                today.atTime(12, 0),
                26800,
                26800,
                0,
                "completed order"
        );

        mockMvc.perform(post("/orders/house/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "orderType":"",
                                  "isLt":0,
                                  "searchContent":"House Page Checked In"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].orderId").value("39103"))
                .andExpect(jsonPath("$.data.list[0].orderState").value(3))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].orderDetailDisplayState").value(2));

        mockMvc.perform(post("/orders/house/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "orderType":"",
                                  "isLt":0,
                                  "searchContent":"House Page Completed"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].orderId").value("39104"))
                .andExpect(jsonPath("$.data.list[0].orderState").value(4))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].orderDetailDisplayState").value(3));
    }

    @Test
    @Timeout(60)
    void ordersHousePageGet_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedOrders();

        mockMvc.perform(post("/orders/house/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "orderType":"",
                                  "isLt":0,
                                  "searchContent":"House Page"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(2));

        mockMvc.perform(post("/orders/house/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "orderType":"",
                                  "isLt":0,
                                  "searchContent":"House Page"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    private void seedOrders() {
        resetOrders();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        insertChannelAccount();

        LocalDate today = LocalDate.now(SHANGHAI_ZONE);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);
        LocalDate yesterday = today.minusDays(1);

        insertOrderMain(
                39101L,
                "booked",
                "paid",
                "House Page Alpha",
                "13939101001",
                today,
                tomorrow,
                today.atTime(9, 0),
                26800,
                26800,
                0,
                "today check-in"
        );
        insertDistributionOrder(39201L, 39101L, 2680);

        insertOrderMain(
                39102L,
                "booked",
                "paid",
                "House Page Beta",
                "13939102002",
                tomorrow,
                dayAfterTomorrow,
                yesterday.atTime(10, 0),
                32800,
                32800,
                0,
                "tomorrow check-in"
        );
        insertDistributionOrder(39202L, 39102L, 3280);
    }

    private void resetOrders() {
        jdbcTemplate.update("DELETE FROM order_payment_record WHERE camp_id = ? AND order_id BETWEEN 39101 AND 39102", CAMP_ID);
        jdbcTemplate.update("DELETE FROM distribution_order WHERE camp_id = ? AND source_order_id BETWEEN 39101 AND 39102", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_guest WHERE order_id BETWEEN 39101 AND 39102");
        jdbcTemplate.update("DELETE FROM ledger_entry WHERE camp_id = ? AND order_id BETWEEN 39101 AND 39102", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_main WHERE camp_id = ? AND order_id BETWEEN 39101 AND 39102", CAMP_ID);
        jdbcTemplate.update("DELETE FROM channel_account WHERE account_id = ?", CHANNEL_ACCOUNT_ID);
    }

    private void insertChannelAccount() {
        jdbcTemplate.update("""
                        INSERT INTO channel_account (
                            account_id,
                            camp_id,
                            channel_id,
                            channel_name,
                            account_name,
                            out_account_id,
                            status
                        ) VALUES (?, ?, ?, ?, ?, ?, 'authorized')
                        """,
                CHANNEL_ACCOUNT_ID,
                CAMP_ID,
                17L,
                "Direct OTA",
                "House Page Test Account",
                "TEST-HOUSE-PAGE-29401"
        );
    }

    private void insertOrderMain(
            long orderId,
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
                CAMP_ID,
                11001L,
                22001L,
                23001L,
                CHANNEL_ACCOUNT_ID,
                null,
                "ORDER-HOUSE-PAGE-" + orderId,
                "OUT-HOUSE-PAGE-" + orderId,
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
                0,
                0,
                0,
                0,
                totalPayPriceCent - refundPriceCent,
                paymentStatus,
                17101L,
                17202L,
                "channel",
                remark,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                12001L,
                12001L
        );
    }

    private void insertDistributionOrder(long distributionOrderId, long sourceOrderId, long commissionPriceCent) {
        jdbcTemplate.update("""
                        INSERT INTO distribution_order (
                            distribution_order_id,
                            camp_id,
                            source_order_id,
                            channel_id,
                            commission_price_cent,
                            settlement_status
                        ) VALUES (?, ?, ?, ?, ?, 'pending')
                        """,
                distributionOrderId,
                CAMP_ID,
                sourceOrderId,
                CHANNEL_ACCOUNT_ID,
                commissionPriceCent
        );
    }
}
