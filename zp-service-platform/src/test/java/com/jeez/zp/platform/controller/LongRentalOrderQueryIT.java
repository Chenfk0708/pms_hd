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
class LongRentalOrderQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void ordersPageGet_shouldReturnLongRentalRowsWithCurrentFrontendFields() throws Exception {
        SeedContext seed = seedLongRentalOrders();

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
                                  "isLt":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.pages").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].orderId").value("39001"))
                .andExpect(jsonPath("$.data.list[0].channelId").value("13"))
                .andExpect(jsonPath("$.data.list[0].orderChannelId").value("13"))
                .andExpect(jsonPath("$.data.list[0].channelName").value("贝壳"))
                .andExpect(jsonPath("$.data.list[0].guestName").value("Alice Long"))
                .andExpect(jsonPath("$.data.list[0].guestMobile").value("13900000111"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value("22001"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("标准大床房"))
                .andExpect(jsonPath("$.data.list[0].roomName").value("101"))
                .andExpect(jsonPath("$.data.list[0].poiId").value("11001"))
                .andExpect(jsonPath("$.data.list[0].poiName").value("路客云演示门店"))
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
                .andExpect(jsonPath("$.data.list[0].paymentTime").value(seed.livingPaymentTime()))
                .andExpect(jsonPath("$.data.list[0].createTimeText").value(seed.livingCreateTimeText()))
                .andExpect(jsonPath("$.data.list[0].isOccupyStock").value(1))
                .andExpect(jsonPath("$.data.list[0].arrangeRoomStatusName").value("已排房"))
                .andExpect(jsonPath("$.data.list[0].includeStatisticsName").value("计入统计"))
                .andExpect(jsonPath("$.data.list[0].contractNo").value("HT-LR-39001"))
                .andExpect(jsonPath("$.data.list[0].nextPaymentAmount").value(0))
                .andExpect(jsonPath("$.data.list[0].nextPaymentDate").value(seed.livingEndDate().toString()));
    }

    @Test
    @Timeout(60)
    void ordersPageGet_shouldSupportKeywordAliasAndLongRentalFilters() throws Exception {
        seedLongRentalOrders();

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
                                  "keyword":"Alice",
                                  "searchCode":"Alice",
                                  "channelId":"13",
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
                .andExpect(jsonPath("$.data.list[0].orderId").value("39001"))
                .andExpect(jsonPath("$.data.list[0].guestName").value("Alice Long"))
                .andExpect(jsonPath("$.data.list[0].liveStatusCode").value("living"));
    }

    @Test
    @Timeout(60)
    void longRentalOrdersPageGet_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedLongRentalOrders();

        mockMvc.perform(post("/orders/page/get")
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
                                  "isLt":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(2));

        mockMvc.perform(post("/orders/page/get")
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
                                  "isLt":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301));
    }

    private SeedContext seedLongRentalOrders() {
        resetOrders();

        insertChannelAccount(29401L, 13L, "贝壳");
        insertChannelAccount(29402L, 14L, "58同城");

        LocalDate today = LocalDate.now(SHANGHAI_ZONE);
        LocalDate livingStartDate = today.minusDays(2);
        LocalDate livingEndDate = today.plusDays(28);
        LocalDate cancelledStartDate = today.plusDays(5);
        LocalDate cancelledEndDate = today.plusDays(35);

        insertOrderMain(39001L, 29401L, 22001L, 23001L, "OUT-LR-39001", "checked_in", "paid",
                "Alice Long", "13900000111", livingStartDate, livingEndDate, today.minusDays(3).atTime(10, 30),
                80000, 80000, 0, 8000, 72000, null, "channel", "long rental living");
        insertDistributionOrder(39101L, 39001L, 29401L, 8000);

        insertOrderMain(39002L, 29402L, 22002L, null, "OUT-LR-39002", "cancelled", "cancelled",
                "Bob Cancelled", "13900000222", cancelledStartDate, cancelledEndDate, today.minusDays(1).atTime(8, 45),
                60000, 0, 0, 0, 0, null, "channel", "long rental cancelled");

        return new SeedContext(livingStartDate, livingEndDate, today.minusDays(3).atTime(10, 30));
    }

    private void resetOrders() {
        jdbcTemplate.update("DELETE FROM order_payment_record WHERE camp_id = ?", 10001L);
        jdbcTemplate.update("""
                DELETE le
                FROM ledger_entry le
                JOIN order_main om ON om.order_id = le.order_id
                WHERE om.camp_id = ?
                """, 10001L);
        jdbcTemplate.update("""
                DELETE og
                FROM order_guest og
                JOIN order_main om ON om.order_id = og.order_id
                WHERE om.camp_id = ?
                """, 10001L);
        jdbcTemplate.update("DELETE FROM distribution_order WHERE camp_id = ?", 10001L);
        jdbcTemplate.update("DELETE FROM order_main WHERE camp_id = ?", 10001L);
        jdbcTemplate.update("DELETE FROM channel_account WHERE account_id IN (?, ?)", 29401L, 29402L);
    }

    private void insertChannelAccount(long accountId, long channelId, String channelName) {
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
                accountId,
                10001L,
                channelId,
                channelName,
                channelName + "账号",
                "OUT-" + accountId
        );
    }

    private void insertDistributionOrder(long distributionOrderId, long sourceOrderId, long channelAccountId, long commissionPriceCent) {
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
                10001L,
                sourceOrderId,
                channelAccountId,
                commissionPriceCent
        );
    }

    private void insertOrderMain(
            long orderId,
            Long channelAccountId,
            long roomCategoryId,
            Long roomId,
            String outOrderNo,
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
            Long goodsId,
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
                10001L,
                11001L,
                roomCategoryId,
                roomId,
                channelAccountId,
                goodsId,
                "ORDER-" + orderId,
                outOrderNo,
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
                sourceType,
                remark,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                14001L,
                14001L
        );
    }

    private record SeedContext(LocalDate livingStartDate, LocalDate livingEndDate, LocalDateTime livingCreatedAt) {

        private String livingCheckInTime() {
            return livingStartDate + " 14:00";
        }

        private String livingCheckOutTime() {
            return livingEndDate + " 12:00";
        }

        private String livingPaymentTime() {
            return livingCreatedAt.toLocalDate().toString();
        }

        private String livingCreateTimeText() {
            return livingCreatedAt.toString().replace('T', ' ');
        }
    }
}
