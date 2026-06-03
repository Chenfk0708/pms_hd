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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderActionIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final long CAMP_ID = 10001L;
    private static final long CREATE_ORDER_ID = 40001L;
    private static final long FLOW_ORDER_ID = 40002L;
    private static final long CANCEL_ORDER_ID = 40003L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void ordersCreate_shouldInsertRealOrderAndInitialGuests() throws Exception {
        resetOrders();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        LocalDate tomorrow = LocalDate.now(SHANGHAI_ZONE).plusDays(1);
        LocalDate dayAfterTomorrow = tomorrow.plusDays(1);

        mockMvc.perform(post("/orders/create")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId":"40001",
                                  "campId":"10001",
                                  "poiId":"11001",
                                  "roomCategoryId":"22001",
                                  "roomId":"23001",
                                  "guestName":"Action Create Guest",
                                  "guestMobile":"13940001001",
                                  "checkInDate":"%s",
                                  "checkOutDate":"%s",
                                  "totalPrice":28800,
                                  "totalPayPrice":0,
                                  "paymentStatus":"unpaid",
                                  "remark":"action create test",
                                  "guests":[
                                    {
                                      "guestId":"40101",
                                      "guestName":"Action Create Guest",
                                      "guestMobile":"13940001001",
                                      "guestIdCard":"ID40001001",
                                      "guestType":"adult"
                                    }
                                  ]
                                }
                                """.formatted(tomorrow, dayAfterTomorrow)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value("40001"))
                .andExpect(jsonPath("$.data.status").value("booked"))
                .andExpect(jsonPath("$.data.message").value("订单创建成功"));

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM order_main WHERE camp_id = ? AND order_id = ? AND status = 'booked' AND guest_name = ?",
                Integer.class,
                CAMP_ID,
                CREATE_ORDER_ID,
                "Action Create Guest"
        );
        Integer guestCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM order_guest WHERE order_id = ? AND guest_name = ?",
                Integer.class,
                CREATE_ORDER_ID,
                "Action Create Guest"
        );
        var order = jdbcTemplate.queryForMap(
                "SELECT poi_id, room_category_id, room_id FROM order_main WHERE camp_id = ? AND order_id = ?",
                CAMP_ID,
                CREATE_ORDER_ID
        );
        assertThat(orderCount).isEqualTo(1);
        assertThat(guestCount).isEqualTo(1);
        assertThat(((Number) order.get("poi_id")).longValue()).isEqualTo(OrderTestCatalogFixture.POI_ID);
        assertThat(((Number) order.get("room_category_id")).longValue()).isEqualTo(OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID);
        assertThat(((Number) order.get("room_id")).longValue()).isEqualTo(OrderTestCatalogFixture.STANDARD_ROOM_ID);

        mockMvc.perform(post("/orders/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "isLt":0,
                                  "searchContent":"Action Create Guest"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].orderId").value("40001"))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].roomCategoryId")
                        .value(String.valueOf(OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID)))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].roomCategoryName")
                        .value(OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_NAME))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].roomId")
                        .value(String.valueOf(OrderTestCatalogFixture.STANDARD_ROOM_ID)))
                .andExpect(jsonPath("$.data.list[0].orderDetailViews[0].roomName")
                        .value(OrderTestCatalogFixture.STANDARD_ROOM_NAME));
    }

    @Test
    @Timeout(60)
    void ordersCreate_shouldAcceptGeneratedIdHourlyTimeAndCredentialType() throws Exception {
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        LocalDate tomorrow = LocalDate.now(SHANGHAI_ZONE).plusDays(1);
        String startAt = tomorrow + " 22:30:00";
        String endAt = tomorrow.plusDays(1) + " 03:30:00";

        mockMvc.perform(post("/orders/create")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "poiId":"11001",
                                  "roomCategoryId":"22001",
                                  "roomId":"23001",
                                  "orderType":"hourly_room",
                                  "stayType":"hourly_room",
                                  "guestName":"Hourly Create Guest",
                                  "guestMobile":"13940001009",
                                  "checkInDate":"%s",
                                  "checkOutDate":"%s",
                                  "totalPrice":73000,
                                  "totalPayPrice":73000,
                                  "commissionPrice":1200,
                                  "paymentStatus":"paid",
                                  "roomCategoryName":"顶层套房",
                                  "roomName":"房间1（净）",
                                  "sourceLabel":"自来客",
                                  "roomChargeStatus":"received",
                                  "roomChargeReceived":73000,
                                  "roomChargeMethod":"platform",
                                  "rooms":[
                                    {
                                      "roomType":"顶层套房",
                                      "roomName":"房间1（净）",
                                      "dateRange":"%s 22:30",
                                      "checkInDate":"%s",
                                      "checkOutDate":"%s",
                                      "price":73000,
                                      "quantity":5,
                                      "guests":2
                                    }
                                  ],
                                  "tags":[{"id":"promotion","text":"促销"}],
                                  "reminders":[{"id":"reminder-1","text":"2026-06-01 20:00 到店前确认"}],
                                  "guests":[
                                    {
                                      "guestName":"Hourly Create Guest",
                                      "guestMobile":"13940001009",
                                      "guestIdCardType":"Passport",
                                      "guestIdCard":"P40001009",
                                      "guestType":"adult"
                                    }
                                  ]
                                }
                                """.formatted(startAt, endAt, tomorrow.toString().replace("-", "."), startAt, endAt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("booked"));

        Long orderId = jdbcTemplate.queryForObject(
                "SELECT order_id FROM order_main WHERE camp_id = ? AND guest_name = ? ORDER BY created_at DESC LIMIT 1",
                Long.class,
                CAMP_ID,
                "Hourly Create Guest"
        );
        assertThat(orderId).isNotNull();

        var order = jdbcTemplate.queryForMap(
                "SELECT order_type, start_at, end_at, day_num, commission_price_cent, room_snapshot_json, order_tags_json, order_reminders_json FROM order_main WHERE order_id = ?",
                orderId
        );
        assertThat(order.get("order_type")).isEqualTo("hourly_room");
        assertThat(order.get("start_at")).isEqualTo(LocalDateTime.parse(startAt.replace(" ", "T")));
        assertThat(order.get("end_at")).isEqualTo(LocalDateTime.parse(endAt.replace(" ", "T")));
        assertThat(((Number) order.get("day_num")).longValue()).isEqualTo(5L);
        assertThat(((Number) order.get("commission_price_cent")).longValue()).isEqualTo(1200L);
        assertThat(String.valueOf(order.get("room_snapshot_json"))).contains("顶层套房");
        assertThat(String.valueOf(order.get("order_tags_json"))).contains("促销");
        assertThat(String.valueOf(order.get("order_reminders_json"))).contains("到店前确认");

        String cardType = jdbcTemplate.queryForObject(
                "SELECT guest_id_card_type FROM order_guest WHERE order_id = ? AND guest_name = ?",
                String.class,
                orderId,
                "Hourly Create Guest"
        );
        assertThat(cardType).isEqualTo("Passport");

        jdbcTemplate.update("DELETE FROM order_guest WHERE order_id = ?", orderId);
        jdbcTemplate.update("DELETE FROM order_main WHERE order_id = ?", orderId);
    }

    @Test
    @Timeout(60)
    void orderLifecycleActions_shouldCheckInSaveGuestsAndCheckOut() throws Exception {
        seedBookedOrder(FLOW_ORDER_ID, "Action Flow Guest");

        mockMvc.perform(post("/orders/{id}/check-in", FLOW_ORDER_ID)
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderId").value("40002"))
                .andExpect(jsonPath("$.data.status").value("checked_in"))
                .andExpect(jsonPath("$.data.message").value("办理入住成功"));

        assertThat(orderStatus(FLOW_ORDER_ID)).isEqualTo("checked_in");

        mockMvc.perform(post("/orders/{id}/guests/save", FLOW_ORDER_ID)
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "guests":[
                                    {
                                      "guestId":"40121",
                                      "guestName":"Action Flow Guest",
                                      "guestMobile":"13940002001",
                                      "guestIdCard":"ID40002001",
                                      "guestType":"adult"
                                    },
                                    {
                                      "guestId":"40122",
                                      "guestName":"Action Flow Companion",
                                      "guestMobile":"13940002002",
                                      "guestIdCard":"ID40002002",
                                      "guestType":"adult"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderId").value("40002"))
                .andExpect(jsonPath("$.data.guestCount").value(2))
                .andExpect(jsonPath("$.data.message").value("入住人保存成功"));

        assertThat(guestCount(FLOW_ORDER_ID)).isEqualTo(2);

        mockMvc.perform(post("/orders/{id}/check-out", FLOW_ORDER_ID)
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderId").value("40002"))
                .andExpect(jsonPath("$.data.status").value("completed"))
                .andExpect(jsonPath("$.data.message").value("办理退房成功"));

        assertThat(orderStatus(FLOW_ORDER_ID)).isEqualTo("completed");
    }

    @Test
    @Timeout(60)
    void ordersCancel_shouldCancelBookedOrderAndRejectForeignCampAccess() throws Exception {
        seedBookedOrder(CANCEL_ORDER_ID, "Action Cancel Guest");

        mockMvc.perform(post("/orders/{id}/cancel", CANCEL_ORDER_ID)
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "reason":"guest request"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderId").value("40003"))
                .andExpect(jsonPath("$.data.status").value("cancelled"))
                .andExpect(jsonPath("$.data.message").value("订单取消成功"));

        assertThat(orderStatus(CANCEL_ORDER_ID)).isEqualTo("cancelled");

        mockMvc.perform(post("/orders/{id}/cancel", CANCEL_ORDER_ID)
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "reason":"foreign camp"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    private void resetOrders() {
        jdbcTemplate.update("DELETE FROM order_payment_record WHERE camp_id = ? AND order_id BETWEEN 40001 AND 40003", CAMP_ID);
        jdbcTemplate.update("DELETE FROM distribution_order WHERE camp_id = ? AND source_order_id BETWEEN 40001 AND 40003", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_guest WHERE order_id BETWEEN 40001 AND 40003");
        jdbcTemplate.update("DELETE FROM ledger_entry WHERE camp_id = ? AND order_id BETWEEN 40001 AND 40003", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_main WHERE camp_id = ? AND order_id BETWEEN 40001 AND 40003", CAMP_ID);
    }

    private void seedBookedOrder(long orderId, String guestName) {
        resetOrders();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        LocalDate today = LocalDate.now(SHANGHAI_ZONE);
        insertOrderMain(
                orderId,
                guestName,
                "139" + orderId,
                today,
                today.plusDays(1),
                today.atTime(8, 0)
        );
    }

    private void insertOrderMain(
            long orderId,
            String guestName,
            String guestMobile,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createdAt
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
                "ORDER-ACTION-" + orderId,
                "OUT-ACTION-" + orderId,
                "daily_room",
                "booked",
                guestName,
                guestMobile,
                Timestamp.valueOf(startDate.atTime(LocalTime.of(14, 0))),
                Timestamp.valueOf(endDate.atTime(LocalTime.of(12, 0))),
                Math.max(1, (int) (endDate.toEpochDay() - startDate.toEpochDay())),
                28800,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                "unpaid",
                17101L,
                17202L,
                "frontdesk",
                "order action test",
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                12001L,
                12001L
        );
    }

    private String orderStatus(long orderId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM order_main WHERE camp_id = ? AND order_id = ?",
                String.class,
                CAMP_ID,
                orderId
        );
    }

    private int guestCount(long orderId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM order_guest WHERE order_id = ?",
                Integer.class,
                orderId
        );
        return count == null ? 0 : count;
    }
}
