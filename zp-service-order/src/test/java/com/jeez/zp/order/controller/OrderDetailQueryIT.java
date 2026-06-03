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
class OrderDetailQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final long CAMP_ID = 10001L;
    private static final long ORDER_ID = 39601L;
    private static final long CHANNEL_ACCOUNT_ID = 29431L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void ordersDetailGet_shouldReturnAggregatedOrderDetail() throws Exception {
        SeedContext seed = seedOrder();

        mockMvc.perform(post("/orders/detail/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "orderId":"39601"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.orderId").value("39601"))
                .andExpect(jsonPath("$.data.orderNo").value("ORDER-DETAIL-39601"))
                .andExpect(jsonPath("$.data.outOrderId").value("OUT-DETAIL-39601"))
                .andExpect(jsonPath("$.data.orderType").value("daily_room"))
                .andExpect(jsonPath("$.data.status").value("booked"))
                .andExpect(jsonPath("$.data.statusName").value("待入住"))
                .andExpect(jsonPath("$.data.paymentStatus").value("paid"))
                .andExpect(jsonPath("$.data.channelId").value("20"))
                .andExpect(jsonPath("$.data.channelName").value("Detail OTA"))
                .andExpect(jsonPath("$.data.guestName").value("Detail Guest"))
                .andExpect(jsonPath("$.data.guestMobile").value("13939601001"))
                .andExpect(jsonPath("$.data.poiId").value("11001"))
                .andExpect(jsonPath("$.data.poiName").exists())
                .andExpect(jsonPath("$.data.roomCategoryId").value("22001"))
                .andExpect(jsonPath("$.data.roomCategoryName").exists())
                .andExpect(jsonPath("$.data.roomId").value("23001"))
                .andExpect(jsonPath("$.data.roomName").value(OrderTestCatalogFixture.STANDARD_ROOM_NAME))
                .andExpect(jsonPath("$.data.checkInTime").value(seed.checkInTime()))
                .andExpect(jsonPath("$.data.checkOutTime").value(seed.checkOutTime()))
                .andExpect(jsonPath("$.data.dayNum").value(1))
                .andExpect(jsonPath("$.data.totalPrice").value(26800))
                .andExpect(jsonPath("$.data.totalPayPrice").value(26800))
                .andExpect(jsonPath("$.data.refundPrice").value(0))
                .andExpect(jsonPath("$.data.commissionPrice").value(2680))
                .andExpect(jsonPath("$.data.debtPrice").value(0))
                .andExpect(jsonPath("$.data.paymentWayId").value("17202"))
                .andExpect(jsonPath("$.data.paymentWayName").value("微信支付"))
                .andExpect(jsonPath("$.data.remark").value("detail test order"))
                .andExpect(jsonPath("$.data.createdAt").value(seed.createdAtText()))
                .andExpect(jsonPath("$.data.guests.length()").value(2))
                .andExpect(jsonPath("$.data.guests[0].guestName").value("Detail Guest"))
                .andExpect(jsonPath("$.data.guests[0].guestMobile").value("13939601001"))
                .andExpect(jsonPath("$.data.guests[0].guestIdCard").value("ID39601001"))
                .andExpect(jsonPath("$.data.guests[0].guestType").value("adult"))
                .andExpect(jsonPath("$.data.guests[1].guestName").value("Detail Companion"))
                .andExpect(jsonPath("$.data.paymentRecords.length()").value(1))
                .andExpect(jsonPath("$.data.paymentRecords[0].paymentRecordId").value("39701"))
                .andExpect(jsonPath("$.data.paymentRecords[0].paymentWayId").value("17202"))
                .andExpect(jsonPath("$.data.paymentRecords[0].paymentWayName").value("微信支付"))
                .andExpect(jsonPath("$.data.paymentRecords[0].amount").value(26800))
                .andExpect(jsonPath("$.data.paymentRecords[0].debtAmount").value(0))
                .andExpect(jsonPath("$.data.paymentRecords[0].operatorName").value("测试操作员"))
                .andExpect(jsonPath("$.data.paymentRecords[0].paymentTime").value(seed.paymentTimeText()));
    }

    @Test
    @Timeout(60)
    void ordersDetailGet_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedOrder();

        mockMvc.perform(post("/orders/detail/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "orderId":"39601"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderId").value("39601"));

        mockMvc.perform(post("/orders/detail/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "orderId":"39601"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    private SeedContext seedOrder() {
        resetOrder();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        insertChannelAccount();

        LocalDate today = LocalDate.now(SHANGHAI_ZONE);
        LocalDate tomorrow = today.plusDays(1);
        LocalDateTime createdAt = today.atTime(7, 30);
        LocalDateTime paymentTime = today.atTime(7, 45);

        insertOrderMain(today, tomorrow, createdAt);
        insertDistributionOrder();
        insertGuest(39801L, "Detail Guest", "13939601001", "ID39601001", "adult");
        insertGuest(39802L, "Detail Companion", "13939601002", "ID39601002", "adult");
        insertPaymentRecord(paymentTime);

        return new SeedContext(today, tomorrow, createdAt, paymentTime);
    }

    private void resetOrder() {
        jdbcTemplate.update("DELETE FROM order_payment_record WHERE camp_id = ? AND order_id = ?", CAMP_ID, ORDER_ID);
        jdbcTemplate.update("DELETE FROM distribution_order WHERE camp_id = ? AND source_order_id = ?", CAMP_ID, ORDER_ID);
        jdbcTemplate.update("DELETE FROM order_guest WHERE order_id = ?", ORDER_ID);
        jdbcTemplate.update("DELETE FROM ledger_entry WHERE camp_id = ? AND order_id = ?", CAMP_ID, ORDER_ID);
        jdbcTemplate.update("DELETE FROM order_main WHERE camp_id = ? AND order_id = ?", CAMP_ID, ORDER_ID);
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
                20L,
                "Detail OTA",
                "Detail Test Account",
                "TEST-DETAIL-29431"
        );
    }

    private void insertOrderMain(LocalDate startDate, LocalDate endDate, LocalDateTime createdAt) {
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
                ORDER_ID,
                CAMP_ID,
                11001L,
                22001L,
                23001L,
                CHANNEL_ACCOUNT_ID,
                null,
                "ORDER-DETAIL-39601",
                "OUT-DETAIL-39601",
                "daily_room",
                "booked",
                "Detail Guest",
                "13939601001",
                Timestamp.valueOf(startDate.atTime(LocalTime.of(14, 0))),
                Timestamp.valueOf(endDate.atTime(LocalTime.of(12, 0))),
                1,
                26800,
                0,
                26800,
                0,
                0,
                0,
                0,
                0,
                26800,
                "paid",
                17101L,
                17202L,
                "channel",
                "detail test order",
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                12001L,
                12001L
        );
    }

    private void insertDistributionOrder() {
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
                39711L,
                CAMP_ID,
                ORDER_ID,
                CHANNEL_ACCOUNT_ID,
                2680L
        );
    }

    private void insertGuest(long id, String guestName, String mobile, String idCard, String guestType) {
        jdbcTemplate.update("""
                        INSERT INTO order_guest (
                            id,
                            order_id,
                            guest_name,
                            guest_mobile,
                            guest_id_card,
                            guest_type
                        ) VALUES (?, ?, ?, ?, ?, ?)
                        """,
                id,
                ORDER_ID,
                guestName,
                mobile,
                idCard,
                guestType
        );
    }

    private void insertPaymentRecord(LocalDateTime paymentTime) {
        jdbcTemplate.update("""
                        INSERT INTO order_payment_record (
                            order_payment_record_id,
                            camp_id,
                            poi_id,
                            order_id,
                            payment_type_id,
                            payment_way_id,
                            is_income,
                            amount_cent,
                            debt_amount_cent,
                            payment_no,
                            payment_time,
                            operator_id,
                            operator_name,
                            remark
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                39701L,
                CAMP_ID,
                11001L,
                ORDER_ID,
                17101L,
                17202L,
                1,
                26800L,
                0L,
                "PAY-DETAIL-39701",
                Timestamp.valueOf(paymentTime),
                12001L,
                "测试操作员",
                "detail payment"
        );
    }

    private record SeedContext(LocalDate startDate, LocalDate endDate, LocalDateTime createdAt, LocalDateTime paymentTime) {

        private String checkInTime() {
            return startDate + " 14:00";
        }

        private String checkOutTime() {
            return endDate + " 12:00";
        }

        private String createdAtText() {
            return createdAt.toString().replace('T', ' ');
        }

        private String paymentTimeText() {
            return paymentTime.toString().replace('T', ' ');
        }
    }
}
