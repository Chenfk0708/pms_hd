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
class StrongReminderPageQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final long CAMP_ID = 10001L;
    private static final long CHANNEL_ACCOUNT_ID = 29451L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void ordersStrongReminderPageGet_shouldReturnReminderPageFromRealOrders() throws Exception {
        seedOrders();

        mockMvc.perform(post("/orders/strongReminder/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "keyword":"Strong Reminder"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.pagination.pageNum").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.data.pagination.total").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].id").value("order-39901"))
                .andExpect(jsonPath("$.data.list[0].campId").value("10001"))
                .andExpect(jsonPath("$.data.list[0].level").value("high"))
                .andExpect(jsonPath("$.data.list[0].title").value("待确认订单"))
                .andExpect(jsonPath("$.data.list[0].guestName").value("Strong Reminder Pending"))
                .andExpect(jsonPath("$.data.list[0].roomName").value(OrderTestCatalogFixture.STANDARD_ROOM_NAME))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("ORDER-STRONG-39901"))
                .andExpect(jsonPath("$.data.list[0].dueAt").value("14:00"))
                .andExpect(jsonPath("$.data.list[0].channel").value("meituan"))
                .andExpect(jsonPath("$.data.list[0].status").value("pending"))
                .andExpect(jsonPath("$.data.list[0].primaryAction").value("order"))
                .andExpect(jsonPath("$.data.list[0].summary").value("订单待确认/待支付，请及时跟进客人入住信息。"))
                .andExpect(jsonPath("$.data.list[1].id").value("order-39902"))
                .andExpect(jsonPath("$.data.list[1].level").value("high"))
                .andExpect(jsonPath("$.data.list[1].title").value("退款处理提醒"))
                .andExpect(jsonPath("$.data.list[1].guestName").value("Strong Reminder Refunding"))
                .andExpect(jsonPath("$.data.list[1].roomName").exists())
                .andExpect(jsonPath("$.data.list[1].orderNo").value("ORDER-STRONG-39902"))
                .andExpect(jsonPath("$.data.list[1].primaryAction").value("order"));
    }

    @Test
    @Timeout(60)
    void ordersStrongReminderPageGet_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedOrders();

        mockMvc.perform(post("/orders/strongReminder/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "keyword":"Strong Reminder"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.pagination.total").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(2));

        mockMvc.perform(post("/orders/strongReminder/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "keyword":"Strong Reminder"
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

        insertOrderMain(
                39901L,
                23001L,
                "pending",
                "unpaid",
                "Strong Reminder Pending",
                "13939901001",
                today,
                tomorrow,
                today.atTime(8, 0),
                26800,
                0,
                0
        );
        insertOrderMain(
                39902L,
                null,
                "refunding",
                "paid",
                "Strong Reminder Refunding",
                "13939902002",
                today,
                tomorrow,
                today.atTime(9, 0),
                32800,
                32800,
                1000
        );
        insertOrderMain(
                39903L,
                23001L,
                "completed",
                "paid",
                "Strong Reminder Completed",
                "13939903003",
                today.minusDays(2),
                today.minusDays(1),
                today.atTime(7, 0),
                29800,
                29800,
                0
        );
    }

    private void resetOrders() {
        jdbcTemplate.update("DELETE FROM order_payment_record WHERE camp_id = ? AND order_id BETWEEN 39901 AND 39903", CAMP_ID);
        jdbcTemplate.update("DELETE FROM distribution_order WHERE camp_id = ? AND source_order_id BETWEEN 39901 AND 39903", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_guest WHERE order_id BETWEEN 39901 AND 39903");
        jdbcTemplate.update("DELETE FROM ledger_entry WHERE camp_id = ? AND order_id BETWEEN 39901 AND 39903", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_main WHERE camp_id = ? AND order_id BETWEEN 39901 AND 39903", CAMP_ID);
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
                2L,
                "Meituan Reminder",
                "Strong Reminder Test Account",
                "TEST-STRONG-REMINDER-29451"
        );
    }

    private void insertOrderMain(
            long orderId,
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
            long refundPriceCent
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
                roomId,
                CHANNEL_ACCOUNT_ID,
                null,
                "ORDER-STRONG-" + orderId,
                "OUT-STRONG-" + orderId,
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
                "strong reminder page test",
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                12001L,
                12001L
        );
    }
}
