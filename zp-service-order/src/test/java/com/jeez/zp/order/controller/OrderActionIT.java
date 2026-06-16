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
import java.util.Map;

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
    private static final long MISSING_ROOM_ORDER_ID = 40004L;
    private static final long OCCUPIED_ORDER_ID = 40005L;
    private static final long OCCUPIED_CREATE_ORDER_ID = 40006L;
    private static final long CLOSED_CREATE_ORDER_ID = 40007L;
    private static final long CHANGE_ROOM_ORDER_ID = 40008L;
    private static final long CHANGE_ROOM_OCCUPIED_ORDER_ID = 40009L;
    private static final long SKIP_STOCK_ORDER_ID = 40010L;
    private static final long INVALID_CREATE_ORDER_ID = 40011L;
    private static final long INVALID_GUEST_ORDER_ID = 40012L;
    private static final long NO_SHOW_ORDER_ID = 40013L;
    private static final long FUTURE_NO_SHOW_ORDER_ID = 40014L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void ordersCreate_shouldInsertRealOrderAndInitialGuests() throws Exception {
        ensureOrderLifecycleColumns();
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
                                      "guestIdCard":"110105199001010010",
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
                "SELECT poi_id, room_category_id, room_id, source_label_snapshot FROM order_main WHERE camp_id = ? AND order_id = ?",
                CAMP_ID,
                CREATE_ORDER_ID
        );
        assertThat(orderCount).isEqualTo(1);
        assertThat(guestCount).isEqualTo(1);
        assertThat(((Number) order.get("poi_id")).longValue()).isEqualTo(OrderTestCatalogFixture.POI_ID);
        assertThat(((Number) order.get("room_category_id")).longValue()).isEqualTo(OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID);
        assertThat(((Number) order.get("room_id")).longValue()).isEqualTo(OrderTestCatalogFixture.STANDARD_ROOM_ID);
        assertThat(order.get("source_label_snapshot")).isEqualTo("宿银平台");

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
                .andExpect(jsonPath("$.data.list[0].channelName").value("宿银平台"))
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
        ensureOrderLifecycleColumns();
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
        assertThat(String.valueOf(order.get("room_snapshot_json"))).contains("\"roomCategoryId\": \"22001\"");
        assertThat(String.valueOf(order.get("room_snapshot_json"))).contains("\"roomId\": \"23001\"");
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
    void ordersCreate_shouldRejectMissingRoomSelectionForStayOrders() throws Exception {
        ensureOrderLifecycleColumns();
        resetOrders();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        LocalDate tomorrow = LocalDate.now(SHANGHAI_ZONE).plusDays(1);

        mockMvc.perform(post("/orders/create")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId":"40004",
                                  "campId":"10001",
                                  "poiId":"11001",
                                  "guestName":"Missing Room Guest",
                                  "guestMobile":"13940004001",
                                  "checkInDate":"%s",
                                  "checkOutDate":"%s",
                                  "totalPrice":28800,
                                  "totalPayPrice":0
                                }
                                """.formatted(tomorrow, tomorrow.plusDays(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.success").value(false));

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM order_main WHERE camp_id = ? AND order_id = ?",
                Integer.class,
                CAMP_ID,
                MISSING_ROOM_ORDER_ID
        );
        assertThat(orderCount).isZero();
    }

    @Test
    @Timeout(60)
    void ordersCreate_shouldRejectOccupiedRoomInRequestedRange() throws Exception {
        ensureOrderLifecycleColumns();
        resetOrders();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        LocalDate tomorrow = LocalDate.now(SHANGHAI_ZONE).plusDays(1);
        insertOrderMain(
                OCCUPIED_ORDER_ID,
                "Existing Occupied Guest",
                "13940005001",
                tomorrow,
                tomorrow.plusDays(1),
                tomorrow.atTime(8, 0)
        );

        mockMvc.perform(post("/orders/create")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId":"40006",
                                  "campId":"10001",
                                  "poiId":"11001",
                                  "roomCategoryId":"22001",
                                  "roomId":"23001",
                                  "guestName":"Duplicate Room Guest",
                                  "guestMobile":"13940006001",
                                  "checkInDate":"%s",
                                  "checkOutDate":"%s",
                                  "totalPrice":28800,
                                  "totalPayPrice":0
                                }
                                """.formatted(tomorrow, tomorrow.plusDays(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.success").value(false));

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM order_main WHERE camp_id = ? AND order_id = ?",
                Integer.class,
                CAMP_ID,
                OCCUPIED_CREATE_ORDER_ID
        );
        assertThat(orderCount).isZero();
    }

    @Test
    @Timeout(60)
    void ordersCreate_shouldRejectClosedRoomInRequestedRange() throws Exception {
        ensureOrderLifecycleColumns();
        resetOrders();
        ensureRoomStatusBlockTable();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        LocalDate tomorrow = LocalDate.now(SHANGHAI_ZONE).plusDays(1);
        insertRoomStatusBlock(440001L, tomorrow, "订单创建测试关房");

        mockMvc.perform(post("/orders/create")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId":"40007",
                                  "campId":"10001",
                                  "poiId":"11001",
                                  "roomCategoryId":"22001",
                                  "roomId":"23001",
                                  "guestName":"Closed Room Guest",
                                  "guestMobile":"13940007001",
                                  "checkInDate":"%s",
                                  "checkOutDate":"%s",
                                  "totalPrice":28800,
                                  "totalPayPrice":0
                                }
                                """.formatted(tomorrow, tomorrow.plusDays(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("所选房间在该时间段已关房，不能录单"));

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM order_main WHERE camp_id = ? AND order_id = ?",
                Integer.class,
                CAMP_ID,
                CLOSED_CREATE_ORDER_ID
        );
        assertThat(orderCount).isZero();
    }

    @Test
    @Timeout(60)
    void ordersCreate_shouldRejectInvalidGuestNameAndMobileBeforeInsert() throws Exception {
        ensureOrderLifecycleColumns();
        resetOrders();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        LocalDate tomorrow = LocalDate.now(SHANGHAI_ZONE).plusDays(1);

        mockMvc.perform(post("/orders/create")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId":"40011",
                                  "campId":"10001",
                                  "poiId":"11001",
                                  "roomCategoryId":"22001",
                                  "roomId":"23001",
                                  "guestName":"1234",
                                  "guestMobile":"12345",
                                  "checkInDate":"%s",
                                  "checkOutDate":"%s",
                                  "totalPrice":28800,
                                  "totalPayPrice":0
                                }
                                """.formatted(tomorrow, tomorrow.plusDays(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("姓名格式不正确，请输入 2-30 个中文或英文字母"));

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM order_main WHERE camp_id = ? AND order_id = ?",
                Integer.class,
                CAMP_ID,
                INVALID_CREATE_ORDER_ID
        );
        assertThat(orderCount).isZero();
    }

    @Test
    @Timeout(60)
    void ordersGuestsSave_shouldRejectInvalidMobileAndResidentIdBeforeSaving() throws Exception {
        ensureOrderLifecycleColumns();
        seedBookedOrder(INVALID_GUEST_ORDER_ID, "Invalid Guest Holder");

        mockMvc.perform(post("/orders/{id}/guests/save", INVALID_GUEST_ORDER_ID)
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "guests":[
                                    {
                                      "guestId":"40131",
                                      "guestName":"非法证件客人",
                                      "guestMobile":"12345",
                                      "guestIdCardType":"居民身份证",
                                      "guestIdCard":"P40001009",
                                      "guestType":"adult"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("手机号格式不正确"));

        assertThat(guestCount(INVALID_GUEST_ORDER_ID)).isZero();
        Timestamp guestRegisteredAt = jdbcTemplate.queryForObject(
                "SELECT guest_registered_at FROM order_main WHERE camp_id = ? AND order_id = ?",
                Timestamp.class,
                CAMP_ID,
                INVALID_GUEST_ORDER_ID
        );
        assertThat(guestRegisteredAt).isNull();
    }

    @Test
    @Timeout(60)
    void orderLifecycleActions_shouldCheckInSaveGuestsAndCheckOut() throws Exception {
        ensureOrderLifecycleColumns();
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
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请先登记入住人"));

        assertThat(orderStatus(FLOW_ORDER_ID)).isEqualTo("booked");

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
                                      "guestIdCard":"110105199002020026",
                                      "guestType":"adult"
                                    },
                                    {
                                      "guestId":"40122",
                                      "guestName":"Action Flow Companion",
                                      "guestMobile":"13940002002",
                                      "guestIdCard":"110105199003030031",
                                      "guestType":"adult"
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderId").value("40002"))
                .andExpect(jsonPath("$.data.guestCount").value(2))
                .andExpect(jsonPath("$.data.guestRegisteredAt").exists())
                .andExpect(jsonPath("$.data.message").value("入住人保存成功"));

        assertThat(guestCount(FLOW_ORDER_ID)).isEqualTo(2);
        Timestamp guestRegisteredAt = jdbcTemplate.queryForObject(
                "SELECT guest_registered_at FROM order_main WHERE camp_id = ? AND order_id = ?",
                Timestamp.class,
                CAMP_ID,
                FLOW_ORDER_ID
        );
        assertThat(guestRegisteredAt).isNotNull();
        String storedIdCard = jdbcTemplate.queryForObject(
                "SELECT guest_id_card FROM order_guest WHERE order_id = ? AND guest_name = ?",
                String.class,
                FLOW_ORDER_ID,
                "Action Flow Guest"
        );
        assertThat(storedIdCard).isNotEqualTo("110105199002020026");
        assertThat(storedIdCard).startsWith("enc:v1:");

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

        mockMvc.perform(post("/orders/detail/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "orderId":"40002"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.guests[0].guestIdCard").value("110105199002020026"))
                .andExpect(jsonPath("$.data.guests[0].guestIdCardType").value("居民身份证"));

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
                .andExpect(jsonPath("$.data.checkedOutAt").exists())
                .andExpect(jsonPath("$.data.message").value("办理退房成功"));

        assertThat(orderStatus(FLOW_ORDER_ID)).isEqualTo("completed");
        Timestamp checkedOutAt = jdbcTemplate.queryForObject(
                "SELECT checked_out_at FROM order_main WHERE camp_id = ? AND order_id = ?",
                Timestamp.class,
                CAMP_ID,
                FLOW_ORDER_ID
        );
        assertThat(checkedOutAt).isNotNull();
        assertThat(checkedOutAt).isAfterOrEqualTo(guestRegisteredAt);

        String roomCleanStatus = jdbcTemplate.queryForObject(
                "SELECT clean_status FROM room WHERE camp_id = ? AND room_id = ?",
                String.class,
                CAMP_ID,
                OrderTestCatalogFixture.STANDARD_ROOM_ID
        );
        assertThat(roomCleanStatus).isEqualTo("dirty");

        var checkoutCleanTasks = jdbcTemplate.queryForList("""
                        SELECT clean_task_id, task_type, task_status, clean_staff_id, deadline_at
                        FROM clean_task
                        WHERE camp_id = ?
                          AND poi_id = ?
                          AND room_id = ?
                          AND room_category_id = ?
                          AND task_type = 'checkout_clean'
                        """,
                CAMP_ID,
                OrderTestCatalogFixture.POI_ID,
                OrderTestCatalogFixture.STANDARD_ROOM_ID,
                OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID
        );
        assertThat(checkoutCleanTasks).hasSize(1);
        Map<String, Object> checkoutCleanTask = checkoutCleanTasks.get(0);
        assertThat(checkoutCleanTask.get("task_status")).isEqualTo("pending");
        assertThat(checkoutCleanTask.get("clean_staff_id")).isNull();
        assertThat(checkoutCleanTask.get("deadline_at")).isNotNull();

        Long cleanTaskId = ((Number) checkoutCleanTask.get("clean_task_id")).longValue();
        Integer cleanLogCount = jdbcTemplate.queryForObject("""
                        SELECT COUNT(1)
                        FROM clean_log
                        WHERE camp_id = ?
                          AND clean_task_id = ?
                          AND action_type = 'checkout_auto_create'
                          AND operator_id = ?
                        """,
                Integer.class,
                CAMP_ID,
                cleanTaskId,
                12001L
        );
        assertThat(cleanLogCount).isEqualTo(1);
    }

    @Test
    @Timeout(60)
    void ordersMarkNoShow_shouldOnlyAllowOverdueBookedOrders() throws Exception {
        ensureOrderLifecycleColumns();
        resetOrders();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        LocalDate yesterday = LocalDate.now(SHANGHAI_ZONE).minusDays(1);
        LocalDate tomorrow = LocalDate.now(SHANGHAI_ZONE).plusDays(1);
        insertOrderMain(
                NO_SHOW_ORDER_ID,
                "No Show Guest",
                "13940013001",
                yesterday,
                yesterday.plusDays(1),
                yesterday.atTime(8, 0)
        );
        insertOrderMain(
                FUTURE_NO_SHOW_ORDER_ID,
                "Future No Show Guest",
                "13940014001",
                tomorrow,
                tomorrow.plusDays(1),
                tomorrow.atTime(8, 0),
                OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID,
                OrderTestCatalogFixture.STANDARD_OCCUPIED_ROOM_ID,
                OrderTestCatalogFixture.STANDARD_OCCUPIED_ROOM_NAME
        );

        mockMvc.perform(post("/orders/{id}/mark-no-show", NO_SHOW_ORDER_ID)
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "reason":"客人超过入住时间未到店"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderId").value("40013"))
                .andExpect(jsonPath("$.data.status").value("no_show"))
                .andExpect(jsonPath("$.data.message").value("已标记为未到店"));

        assertThat(orderStatus(NO_SHOW_ORDER_ID)).isEqualTo("no_show");
        String noShowRemark = jdbcTemplate.queryForObject(
                "SELECT remark FROM order_main WHERE camp_id = ? AND order_id = ?",
                String.class,
                CAMP_ID,
                NO_SHOW_ORDER_ID
        );
        assertThat(noShowRemark).contains("未到店原因：客人超过入住时间未到店");

        mockMvc.perform(post("/orders/{id}/mark-no-show", FUTURE_NO_SHOW_ORDER_ID)
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "reason":"未来订单不能置为未到店"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("未到入住时间，不能标记未到店"));

        assertThat(orderStatus(FUTURE_NO_SHOW_ORDER_ID)).isEqualTo("booked");
    }

    @Test
    @Timeout(60)
    void ordersMarkNoShow_shouldRejectNonBookedOrders() throws Exception {
        ensureOrderLifecycleColumns();
        seedBookedOrder(NO_SHOW_ORDER_ID, "Checked In No Show Guest");
        jdbcTemplate.update(
                "UPDATE order_main SET status = 'checked_in' WHERE camp_id = ? AND order_id = ?",
                CAMP_ID,
                NO_SHOW_ORDER_ID
        );

        mockMvc.perform(post("/orders/{id}/mark-no-show", NO_SHOW_ORDER_ID)
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "reason":"入住中订单不能置为未到店"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("只有待入住订单可以标记未到店"));

        assertThat(orderStatus(NO_SHOW_ORDER_ID)).isEqualTo("checked_in");
    }

    @Test
    @Timeout(60)
    void ordersChangeRoomOptions_shouldReturnSameCategoryVacantRoomsOnly() throws Exception {
        ensureOrderLifecycleColumns();
        resetOrders();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        LocalDate tomorrow = LocalDate.now(SHANGHAI_ZONE).plusDays(1);
        insertOrderMain(
                CHANGE_ROOM_ORDER_ID,
                "Change Room Guest",
                "13940008001",
                tomorrow,
                tomorrow.plusDays(1),
                tomorrow.atTime(8, 0)
        );
        insertOrderMain(
                CHANGE_ROOM_OCCUPIED_ORDER_ID,
                "Change Room Occupied Guest",
                "13940009001",
                tomorrow,
                tomorrow.plusDays(1),
                tomorrow.atTime(8, 30),
                OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID,
                OrderTestCatalogFixture.STANDARD_OCCUPIED_ROOM_ID,
                OrderTestCatalogFixture.STANDARD_OCCUPIED_ROOM_NAME
        );

        mockMvc.perform(post("/orders/{id}/change-room/options", CHANGE_ROOM_ORDER_ID)
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
                .andExpect(jsonPath("$.data.rooms.length()").value(1))
                .andExpect(jsonPath("$.data.rooms[0].roomId").value(String.valueOf(OrderTestCatalogFixture.STANDARD_CHANGE_ROOM_ID)))
                .andExpect(jsonPath("$.data.rooms[0].roomName").value(OrderTestCatalogFixture.STANDARD_CHANGE_ROOM_NAME))
                .andExpect(jsonPath("$.data.rooms[0].roomCategoryId").value(String.valueOf(OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID)));
    }

    @Test
    @Timeout(60)
    void ordersChangeRoom_shouldUpdateRoomSnapshotAndRejectOccupiedTargetRoom() throws Exception {
        ensureOrderLifecycleColumns();
        resetOrders();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        LocalDate tomorrow = LocalDate.now(SHANGHAI_ZONE).plusDays(1);
        insertOrderMain(
                CHANGE_ROOM_ORDER_ID,
                "Change Room Guest",
                "13940008001",
                tomorrow,
                tomorrow.plusDays(1),
                tomorrow.atTime(8, 0)
        );
        insertOrderMain(
                CHANGE_ROOM_OCCUPIED_ORDER_ID,
                "Change Room Occupied Guest",
                "13940009001",
                tomorrow,
                tomorrow.plusDays(1),
                tomorrow.atTime(8, 30),
                OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID,
                OrderTestCatalogFixture.STANDARD_OCCUPIED_ROOM_ID,
                OrderTestCatalogFixture.STANDARD_OCCUPIED_ROOM_NAME
        );

        mockMvc.perform(post("/orders/{id}/change-room", CHANGE_ROOM_ORDER_ID)
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomId":"23002",
                                  "reason":"客户需要换到同房型空房"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderId").value("40008"))
                .andExpect(jsonPath("$.data.roomId").value(String.valueOf(OrderTestCatalogFixture.STANDARD_CHANGE_ROOM_ID)))
                .andExpect(jsonPath("$.data.roomName").value(OrderTestCatalogFixture.STANDARD_CHANGE_ROOM_NAME))
                .andExpect(jsonPath("$.data.message").value("换房成功"));

        var changedOrder = jdbcTemplate.queryForMap(
                "SELECT room_id, room_name_snapshot, room_snapshot_json, remark FROM order_main WHERE camp_id = ? AND order_id = ?",
                CAMP_ID,
                CHANGE_ROOM_ORDER_ID
        );
        assertThat(((Number) changedOrder.get("room_id")).longValue()).isEqualTo(OrderTestCatalogFixture.STANDARD_CHANGE_ROOM_ID);
        assertThat(changedOrder.get("room_name_snapshot")).isEqualTo(OrderTestCatalogFixture.STANDARD_CHANGE_ROOM_NAME);
        assertThat(String.valueOf(changedOrder.get("room_snapshot_json"))).contains("\"roomId\": \"23002\"");
        assertThat(String.valueOf(changedOrder.get("room_snapshot_json"))).contains(OrderTestCatalogFixture.STANDARD_CHANGE_ROOM_NAME);
        assertThat(String.valueOf(changedOrder.get("remark"))).contains("换房原因：客户需要换到同房型空房");

        mockMvc.perform(post("/orders/{id}/change-room", CHANGE_ROOM_ORDER_ID)
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomId":"23003",
                                  "reason":"尝试换到已占用房"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("目标房间在该时间段已被占用"));

        Long roomIdAfterRejectedChange = jdbcTemplate.queryForObject(
                "SELECT room_id FROM order_main WHERE camp_id = ? AND order_id = ?",
                Long.class,
                CAMP_ID,
                CHANGE_ROOM_ORDER_ID
        );
        assertThat(roomIdAfterRejectedChange).isEqualTo(OrderTestCatalogFixture.STANDARD_CHANGE_ROOM_ID);
    }

    @Test
    @Timeout(60)
    void ordersCancel_shouldCancelBookedOrderAndRejectForeignCampAccess() throws Exception {
        ensureOrderLifecycleColumns();
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

    @Test
    @Timeout(60)
    void ordersSkipStock_shouldReleaseInventoryAndCancelRoomArrangement() throws Exception {
        ensureOrderLifecycleColumns();
        resetOrders();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        LocalDate tomorrow = LocalDate.now(SHANGHAI_ZONE).plusDays(1);
        LocalDate dayAfterTomorrow = tomorrow.plusDays(1);
        insertOrderMain(
                SKIP_STOCK_ORDER_ID,
                "Skip Stock Guest",
                "13940010001",
                tomorrow,
                dayAfterTomorrow,
                tomorrow.atTime(8, 0)
        );

        mockMvc.perform(post("/orders/{id}/skip-stock", SKIP_STOCK_ORDER_ID)
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "reason":"订单详情不占库存"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderId").value("40010"))
                .andExpect(jsonPath("$.data.message").value("订单已释放库存并取消排房"));

        var releasedOrder = jdbcTemplate.queryForMap(
                "SELECT room_id, room_name_snapshot, room_snapshot_json, remark FROM order_main WHERE camp_id = ? AND order_id = ?",
                CAMP_ID,
                SKIP_STOCK_ORDER_ID
        );
        assertThat(releasedOrder.get("room_id")).isNull();
        assertThat(releasedOrder.get("room_name_snapshot")).isNull();
        assertThat(String.valueOf(releasedOrder.get("room_snapshot_json"))).doesNotContain("\"roomId\"");
        assertThat(String.valueOf(releasedOrder.get("room_snapshot_json"))).doesNotContain(OrderTestCatalogFixture.STANDARD_ROOM_NAME);
        assertThat(String.valueOf(releasedOrder.get("remark"))).contains("不占库存原因：订单详情不占库存");

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
                                  "guestName":"After Skip Stock Guest",
                                  "guestMobile":"13940010002",
                                  "checkInDate":"%s",
                                  "checkOutDate":"%s",
                                  "totalPrice":28800,
                                  "totalPayPrice":0,
                                  "paymentStatus":"unpaid"
                                }
                                """.formatted(tomorrow, dayAfterTomorrow)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.status").value("booked"));
    }

    private void resetOrders() {
        ensureCleanLogTable();
        jdbcTemplate.update(
                "DELETE FROM clean_log WHERE camp_id = ? AND room_id IN (?, ?, ?, ?)",
                CAMP_ID,
                OrderTestCatalogFixture.STANDARD_ROOM_ID,
                OrderTestCatalogFixture.STANDARD_CHANGE_ROOM_ID,
                OrderTestCatalogFixture.STANDARD_OCCUPIED_ROOM_ID,
                OrderTestCatalogFixture.DELUXE_ROOM_ID
        );
        jdbcTemplate.update(
                "DELETE FROM clean_task WHERE camp_id = ? AND room_id IN (?, ?, ?, ?)",
                CAMP_ID,
                OrderTestCatalogFixture.STANDARD_ROOM_ID,
                OrderTestCatalogFixture.STANDARD_CHANGE_ROOM_ID,
                OrderTestCatalogFixture.STANDARD_OCCUPIED_ROOM_ID,
                OrderTestCatalogFixture.DELUXE_ROOM_ID
        );
        jdbcTemplate.update("DELETE FROM order_payment_record WHERE camp_id = ? AND order_id BETWEEN 40001 AND 40020", CAMP_ID);
        jdbcTemplate.update("DELETE FROM distribution_order WHERE camp_id = ? AND source_order_id BETWEEN 40001 AND 40020", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_guest WHERE order_id BETWEEN 40001 AND 40020");
        jdbcTemplate.update("DELETE FROM ledger_entry WHERE camp_id = ? AND order_id BETWEEN 40001 AND 40020", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_main WHERE camp_id = ? AND order_id BETWEEN 40001 AND 40020", CAMP_ID);
    }

    private void ensureOrderLifecycleColumns() {
        ensureColumn("order_main", "guest_registered_at", "DATETIME NULL COMMENT '登记入住人保存时间'");
        ensureColumn("order_main", "checked_out_at", "DATETIME NULL COMMENT '办理退房时间'");
    }

    private void ensureColumn(String tableName, String columnName, String definition) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM information_schema.columns
                        WHERE table_schema = DATABASE()
                          AND table_name = ?
                          AND column_name = ?
                        """,
                Integer.class,
                tableName,
                columnName
        );
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    private void ensureRoomStatusBlockTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS room_status_block (
                  block_id BIGINT UNSIGNED NOT NULL COMMENT '主键',
                  camp_id BIGINT UNSIGNED NOT NULL COMMENT '租户 ID',
                  poi_id BIGINT UNSIGNED NOT NULL COMMENT '门店 ID',
                  biz_date DATE NOT NULL COMMENT '业务日期',
                  room_category_id BIGINT UNSIGNED NOT NULL COMMENT '房型 ID',
                  room_id BIGINT UNSIGNED NOT NULL COMMENT '房间 ID',
                  reason VARCHAR(255) DEFAULT NULL COMMENT '关房原因',
                  status VARCHAR(32) NOT NULL DEFAULT 'closed' COMMENT '状态',
                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                  created_by BIGINT UNSIGNED DEFAULT NULL COMMENT '创建人',
                  updated_by BIGINT UNSIGNED DEFAULT NULL COMMENT '更新人',
                  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                  PRIMARY KEY (block_id),
                  UNIQUE KEY uk_room_status_block_room_date (camp_id, poi_id, biz_date, room_id),
                  KEY idx_room_status_block_camp_date (camp_id, biz_date),
                  KEY idx_room_status_block_room_date (room_id, biz_date)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='单房关房日历'
                """);
        jdbcTemplate.update(
                "DELETE FROM room_status_block WHERE camp_id = ? AND room_id = ?",
                CAMP_ID,
                OrderTestCatalogFixture.STANDARD_ROOM_ID
        );
    }

    private void ensureCleanLogTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS clean_log (
                  clean_log_id BIGINT UNSIGNED NOT NULL,
                  camp_id BIGINT UNSIGNED NOT NULL,
                  poi_id BIGINT UNSIGNED NOT NULL,
                  room_id BIGINT UNSIGNED NOT NULL,
                  room_category_id BIGINT UNSIGNED NOT NULL,
                  clean_task_id BIGINT UNSIGNED NOT NULL,
                  clean_staff_id BIGINT UNSIGNED DEFAULT NULL,
                  operator_id BIGINT UNSIGNED DEFAULT NULL,
                  action_type VARCHAR(32) NOT NULL,
                  action_detail VARCHAR(255) DEFAULT NULL,
                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  PRIMARY KEY (clean_log_id),
                  KEY idx_clean_log_task_created_at (clean_task_id, created_at),
                  KEY idx_clean_log_camp_created_at (camp_id, created_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
                """);
    }

    private void insertRoomStatusBlock(long blockId, LocalDate bizDate, String reason) {
        jdbcTemplate.update("""
                        INSERT INTO room_status_block (
                            block_id,
                            camp_id,
                            poi_id,
                            biz_date,
                            room_category_id,
                            room_id,
                            reason,
                            status,
                            created_by,
                            updated_by,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                blockId,
                CAMP_ID,
                OrderTestCatalogFixture.POI_ID,
                bizDate,
                OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID,
                OrderTestCatalogFixture.STANDARD_ROOM_ID,
                reason,
                "closed",
                12001L,
                12001L,
                0
        );
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
        insertOrderMain(
                orderId,
                guestName,
                guestMobile,
                startDate,
                endDate,
                createdAt,
                OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID,
                OrderTestCatalogFixture.STANDARD_ROOM_ID,
                OrderTestCatalogFixture.STANDARD_ROOM_NAME
        );
    }

    private void insertOrderMain(
            long orderId,
            String guestName,
            String guestMobile,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createdAt,
            long roomCategoryId,
            long roomId,
            String roomName
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
                            poi_name_snapshot,
                            room_category_name_snapshot,
                            room_name_snapshot,
                            stay_type_snapshot,
                            source_label_snapshot,
                            room_snapshot_json,
                            remark,
                            created_at,
                            updated_at,
                            created_by,
                            updated_by,
                            is_deleted,
                            version_no
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS JSON), ?, ?, ?, ?, ?, 0, 0)
                        """,
                orderId,
                CAMP_ID,
                11001L,
                roomCategoryId,
                roomId,
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
                "订单动作测试门店",
                roomCategoryId == OrderTestCatalogFixture.DELUXE_ROOM_CATEGORY_ID
                        ? OrderTestCatalogFixture.DELUXE_ROOM_CATEGORY_NAME
                        : OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_NAME,
                roomName,
                "daily_room",
                "自来客",
                """
                        [{"roomCategoryId":"%s","roomId":"%s","roomName":"%s"}]
                        """.formatted(roomCategoryId, roomId, roomName),
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
