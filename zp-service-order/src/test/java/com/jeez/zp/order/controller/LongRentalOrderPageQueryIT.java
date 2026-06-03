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
class LongRentalOrderPageQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final long CAMP_ID = 10001L;
    private static final long CHANNEL_ACCOUNT_ID = 29421L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void ordersHouseLongRentalPageGet_shouldReturnLongRentalRowsWithFrontendFields() throws Exception {
        SeedContext seed = seedOrders();

        mockMvc.perform(post("/orders/houseLongRental/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1,
                                  "orderType":"11",
                                  "keyword":"Long Rental"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(20))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.pages").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].orderId").value("39401"))
                .andExpect(jsonPath("$.data.list[0].outOrderId").value("OUT-LONG-RENTAL-39401"))
                .andExpect(jsonPath("$.data.list[0].channelId").value("19"))
                .andExpect(jsonPath("$.data.list[0].orderChannelId").value("19"))
                .andExpect(jsonPath("$.data.list[0].channelName").value("Long Rental OTA"))
                .andExpect(jsonPath("$.data.list[0].guestName").value("Long Rental Living"))
                .andExpect(jsonPath("$.data.list[0].guestMobile").value("13939401001"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value("22001"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").exists())
                .andExpect(jsonPath("$.data.list[0].roomName").value(OrderTestCatalogFixture.STANDARD_ROOM_NAME))
                .andExpect(jsonPath("$.data.list[0].poiId").value("11001"))
                .andExpect(jsonPath("$.data.list[0].poiName").exists())
                .andExpect(jsonPath("$.data.list[0].checkInTime").value(seed.livingCheckInTime()))
                .andExpect(jsonPath("$.data.list[0].checkOutTime").value(seed.livingCheckOutTime()))
                .andExpect(jsonPath("$.data.list[0].liveStatusName").value("入住中"))
                .andExpect(jsonPath("$.data.list[0].liveStatusCode").value("living"))
                .andExpect(jsonPath("$.data.list[0].orderState").value(2))
                .andExpect(jsonPath("$.data.list[0].orderType").value("11"))
                .andExpect(jsonPath("$.data.list[0].ltGrossRevenuePrice").value(88000))
                .andExpect(jsonPath("$.data.list[0].ltGrossProceedPrice").value(80000))
                .andExpect(jsonPath("$.data.list[0].ltOtherPrice").value(0))
                .andExpect(jsonPath("$.data.list[0].ltDepositPrice").value(0))
                .andExpect(jsonPath("$.data.list[0].orderTotalIncomePrice").value(88000))
                .andExpect(jsonPath("$.data.list[0].ltRentStartDate").value(seed.livingStartDate().toString()))
                .andExpect(jsonPath("$.data.list[0].ltRentEndDate").value(seed.livingEndDate().toString()))
                .andExpect(jsonPath("$.data.list[0].ltPeriodOfContract").value("30天"))
                .andExpect(jsonPath("$.data.list[0].paymentWayName").value("微信支付"))
                .andExpect(jsonPath("$.data.list[0].paymentTime").value(seed.livingCreatedAt().toLocalDate().toString()))
                .andExpect(jsonPath("$.data.list[0].createTimeText").value(seed.livingCreateTimeText()))
                .andExpect(jsonPath("$.data.list[0].isOccupyStock").value(1))
                .andExpect(jsonPath("$.data.list[0].arrangeRoomStatusName").value("已排房"))
                .andExpect(jsonPath("$.data.list[0].includeStatisticsName").value("计入统计"))
                .andExpect(jsonPath("$.data.list[0].contractNo").value("HT-LR-39401"))
                .andExpect(jsonPath("$.data.list[0].nextPaymentAmount").value(0))
                .andExpect(jsonPath("$.data.list[0].nextPaymentDate").value(seed.livingEndDate().toString()));
    }

    @Test
    @Timeout(60)
    void ordersPageGet_shouldKeepIsLtOneLegacyAliasForLongRentalFrontend() throws Exception {
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
                                  "current":1,
                                  "orderType":"11",
                                  "isLt":1,
                                  "keyword":"Long Rental Living",
                                  "channelId":"19",
                                  "roomCategoryId":"22001",
                                  "liveStatus":"living",
                                  "poiId":"11001",
                                  "orderStatus":"2"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].orderId").value("39401"))
                .andExpect(jsonPath("$.data.list[0].guestName").value("Long Rental Living"));
    }

    @Test
    @Timeout(60)
    void ordersHouseLongRentalPageGet_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedOrders();

        mockMvc.perform(post("/orders/houseLongRental/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1,
                                  "orderType":"11",
                                  "keyword":"Long Rental"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(2));

        mockMvc.perform(post("/orders/houseLongRental/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1,
                                  "orderType":"11",
                                  "keyword":"Long Rental"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    private SeedContext seedOrders() {
        resetOrders();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        insertChannelAccount();

        LocalDate today = LocalDate.now(SHANGHAI_ZONE);
        LocalDate livingStartDate = today.minusDays(2);
        LocalDate livingEndDate = today.plusDays(28);
        LocalDate cancelledStartDate = today.plusDays(5);
        LocalDate cancelledEndDate = today.plusDays(35);
        LocalDateTime livingCreatedAt = today.minusDays(3).atTime(10, 30);

        insertOrderMain(
                39401L,
                22001L,
                23001L,
                "checked_in",
                "paid",
                "Long Rental Living",
                "13939401001",
                livingStartDate,
                livingEndDate,
                livingCreatedAt,
                80000,
                80000,
                0,
                8000,
                72000
        );
        insertDistributionOrder(39501L, 39401L, 8000);

        insertOrderMain(
                39402L,
                22002L,
                null,
                "cancelled",
                "cancelled",
                "Long Rental Cancelled",
                "13939402002",
                cancelledStartDate,
                cancelledEndDate,
                today.minusDays(1).atTime(8, 45),
                60000,
                0,
                0,
                0,
                0
        );

        return new SeedContext(livingStartDate, livingEndDate, livingCreatedAt);
    }

    private void resetOrders() {
        jdbcTemplate.update("DELETE FROM order_payment_record WHERE camp_id = ? AND order_id BETWEEN 39401 AND 39402", CAMP_ID);
        jdbcTemplate.update("DELETE FROM distribution_order WHERE camp_id = ? AND source_order_id BETWEEN 39401 AND 39402", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_guest WHERE order_id BETWEEN 39401 AND 39402");
        jdbcTemplate.update("DELETE FROM ledger_entry WHERE camp_id = ? AND order_id BETWEEN 39401 AND 39402", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_main WHERE camp_id = ? AND order_id BETWEEN 39401 AND 39402", CAMP_ID);
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
                19L,
                "Long Rental OTA",
                "Long Rental Test Account",
                "TEST-LONG-RENTAL-29421"
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

    private void insertOrderMain(
            long orderId,
            long roomCategoryId,
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
            long settlementAmountCent
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
                roomCategoryId,
                roomId,
                CHANNEL_ACCOUNT_ID,
                null,
                "ORDER-LONG-RENTAL-" + orderId,
                "OUT-LONG-RENTAL-" + orderId,
                "long_rental",
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
                "channel",
                "long rental page test",
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                12001L,
                12001L
        );
    }

    private record SeedContext(LocalDate livingStartDate, LocalDate livingEndDate, LocalDateTime livingCreatedAt) {

        private String livingCheckInTime() {
            return livingStartDate + " 14:00";
        }

        private String livingCheckOutTime() {
            return livingEndDate + " 12:00";
        }

        private String livingCreateTimeText() {
            return livingCreatedAt.toString().replace('T', ' ');
        }
    }
}
