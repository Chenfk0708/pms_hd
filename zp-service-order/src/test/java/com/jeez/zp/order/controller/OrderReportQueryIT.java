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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderReportQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void orderServicePing_shouldNotRequireGatewayAuth() throws Exception {
        mockMvc.perform(get("/order-service/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("zp-service-order"))
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    @Timeout(60)
    void orderReportGet_shouldReturnDashboardCountsFromRealOrderMain() throws Exception {
        seedOrders();
        ExpectedOrderReport expected = expectedReport();

        mockMvc.perform(post("/order/report/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.todayNewOrder").value(expected.todayNewOrder()))
                .andExpect(jsonPath("$.data.todayPredictCheckIn").value(expected.todayPredictCheckIn()))
                .andExpect(jsonPath("$.data.staying").value(expected.staying()))
                .andExpect(jsonPath("$.data.todayPredictCheckOut").value(expected.todayPredictCheckOut()))
                .andExpect(jsonPath("$.data.tomorrowCheckIn").value(expected.tomorrowCheckIn()))
                .andExpect(jsonPath("$.data.tomorrowCheckOut").value(expected.tomorrowCheckOut()))
                .andExpect(jsonPath("$.data.pending").value(expected.pending()))
                .andExpect(jsonPath("$.data.refunding").value(expected.refunding()))
                .andExpect(jsonPath("$.data.exception").value(expected.exception()));
    }

    @Test
    @Timeout(60)
    void orderReportGet_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedOrders();
        ExpectedOrderReport expected = expectedReport();

        mockMvc.perform(post("/order/report/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":""}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todayPredictCheckIn").value(expected.todayPredictCheckIn()));

        mockMvc.perform(post("/order/report/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10002"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    private void seedOrders() {
        resetOrders();
        LocalDate today = LocalDate.now(SHANGHAI_ZONE);
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate dayAfterTomorrow = today.plusDays(2);
        LocalDate threeDaysLater = today.plusDays(3);

        insertOrderMain(39001L, "booked", "paid", "Order Report A", today, tomorrow, today.atTime(9, 0), 26800, 26800, 0, "today check-in");
        insertOrderMain(39002L, "checked_in", "paid", "Order Report B", yesterday, tomorrow, yesterday.atTime(10, 0), 69600, 69600, 0, "staying and tomorrow check-out");
        insertOrderMain(39003L, "booked", "paid", "Order Report C", tomorrow, dayAfterTomorrow, yesterday.atTime(11, 0), 32800, 32800, 0, "tomorrow check-in");
        insertOrderMain(39004L, "checked_in", "paid", "Order Report D", yesterday, today, yesterday.atTime(12, 0), 28800, 28800, 0, "today check-out");
        insertOrderMain(39005L, "pending", "unpaid", "Order Report E", today, tomorrow, yesterday.atTime(13, 0), 46800, 0, 0, "pending");
        insertOrderMain(39006L, "refunding", "paid", "Order Report F", today, tomorrow, yesterday.atTime(14, 0), 46800, 46800, 1000, "refunding");
        insertOrderMain(39007L, "cancelled", "cancelled", "Order Report G", threeDaysLater, threeDaysLater.plusDays(1), yesterday.atTime(15, 0), 0, 0, 0, "cancelled");
    }

    private void resetOrders() {
        jdbcTemplate.update("DELETE FROM order_payment_record WHERE camp_id = ? AND order_id BETWEEN 39001 AND 39007", CAMP_ID);
        jdbcTemplate.update("DELETE FROM distribution_order WHERE camp_id = ? AND source_order_id BETWEEN 39001 AND 39007", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_guest WHERE order_id BETWEEN 39001 AND 39007");
        jdbcTemplate.update("DELETE FROM ledger_entry WHERE camp_id = ? AND order_id BETWEEN 39001 AND 39007", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_main WHERE camp_id = ? AND order_id BETWEEN 39001 AND 39007", CAMP_ID);
    }

    private void insertOrderMain(
            long orderId,
            String status,
            String paymentStatus,
            String guestName,
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
                null,
                null,
                "ORDER-REPORT-" + orderId,
                null,
                "daily_room",
                status,
                guestName,
                "1390000" + orderId,
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
                "frontdesk",
                remark,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                12001L,
                12001L
        );
    }

    private ExpectedOrderReport expectedReport() {
        LocalDate today = LocalDate.now(SHANGHAI_ZONE);
        LocalDate tomorrow = today.plusDays(1);
        return new ExpectedOrderReport(
                countOrders("DATE(created_at) = ?", today.toString()),
                countOrders("status = 'booked' AND DATE(start_at) = ?", today.toString()),
                countOrders("status = 'checked_in' AND DATE(start_at) <= ? AND DATE(end_at) > ?", today.toString(), today.toString()),
                countOrders("status = 'checked_in' AND DATE(end_at) = ?", today.toString()),
                countOrders("status = 'booked' AND DATE(start_at) = ?", tomorrow.toString()),
                countOrders("status = 'checked_in' AND DATE(end_at) = ?", tomorrow.toString()),
                countOrders("status = 'pending'"),
                countOrders("status = 'refunding'"),
                countOrders("status IN ('refunding', 'cancelled', 'refunded')")
        );
    }

    private int countOrders(String condition, Object... args) {
        Object[] sqlArgs = new Object[args.length + 1];
        sqlArgs[0] = CAMP_ID;
        System.arraycopy(args, 0, sqlArgs, 1, args.length);
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM order_main WHERE camp_id = ? AND is_deleted = 0 AND " + condition,
                Integer.class,
                sqlArgs
        );
        return count == null ? 0 : count;
    }

    private record ExpectedOrderReport(
            int todayNewOrder,
            int todayPredictCheckIn,
            int staying,
            int todayPredictCheckOut,
            int tomorrowCheckIn,
            int tomorrowCheckOut,
            int pending,
            int refunding,
            int exception
    ) {
    }
}
