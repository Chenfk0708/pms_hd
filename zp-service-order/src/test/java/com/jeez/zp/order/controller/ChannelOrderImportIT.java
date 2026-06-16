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

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "jeez.channel.callback.test-token=channel-callback-test-token")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChannelOrderImportIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String CHANNEL_TEST_TOKEN_HEADER = "X-Channel-Test-Token";
    private static final String CHANNEL_OPERATOR_ID_HEADER = "X-Channel-Operator-Id";
    private static final String CHANNEL_TEST_TOKEN = "channel-callback-test-token";
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final long CAMP_ID = 10001L;
    private static final long ACCOUNT_ID = 25351L;
    private static final long CHANNEL_ID = 31L;
    private static final long HOMESTAY_ACCOUNT_ID = 25352L;
    private static final long HOMESTAY_CHANNEL_ID = 32L;
    private static final String CHANNEL_CODE = "meituan_hotel";
    private static final String HOMESTAY_CHANNEL_CODE = "meituan_homestay";
    private static final String CHANNEL_NAME = "Meituan Hotel";
    private static final String HOMESTAY_CHANNEL_NAME = "Meituan Homestay";
    private static final String OUT_POI_ID = "mt-poi-11001";
    private static final String OUT_ROOM_CATEGORY_ID = "mt-room-22001";
    private static final String OUT_ORDER_NO = "MT202606100001";
    private static final String OUT_ORDER_KEY = ACCOUNT_ID + ":" + OUT_ORDER_NO;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void channelCallbacksOrderImport_shouldCreateOrderWithoutInternalGatewayHeaders() throws Exception {
        prepareChannelMapping();
        LocalDate checkIn = LocalDate.now(SHANGHAI_ZONE).plusDays(26);
        LocalDate checkOut = checkIn.plusDays(1);
        String outOrderNo = "MT-CB-202606100001";

        mockMvc.perform(post("/channelCallbacks/{channelCode}/orders/import", CHANNEL_CODE)
                        .header(CHANNEL_TEST_TOKEN_HEADER, CHANNEL_TEST_TOKEN)
                        .header(CHANNEL_OPERATOR_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(callbackImportPayload(outOrderNo, OUT_ROOM_CATEGORY_ID, checkIn, checkOut)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.outOrderNo").value(outOrderNo))
                .andExpect(jsonPath("$.data.accountId").value(String.valueOf(ACCOUNT_ID)))
                .andExpect(jsonPath("$.data.channelName").value(CHANNEL_NAME))
                .andExpect(jsonPath("$.data.created").value(true));

        var order = jdbcTemplate.queryForMap(
                """
                        SELECT source_type, source_label_snapshot, room_category_id, room_id
                        FROM order_main
                        WHERE camp_id = ? AND out_order_no = ?
                        """,
                CAMP_ID,
                ACCOUNT_ID + ":" + outOrderNo
        );
        assertThat(order.get("source_type")).isEqualTo("channel");
        assertThat(order.get("source_label_snapshot")).isEqualTo(CHANNEL_NAME);
        assertThat(((Number) order.get("room_category_id")).longValue())
                .isEqualTo(OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID);
        assertThat(((Number) order.get("room_id")).longValue())
                .isEqualTo(OrderTestCatalogFixture.STANDARD_ROOM_ID);

        var raw = jdbcTemplate.queryForMap(
                """
                        SELECT import_status, channel_code, raw_payload
                        FROM channel_order_raw
                        WHERE account_id = ? AND out_order_no = ?
                        """,
                ACCOUNT_ID,
                outOrderNo
        );
        assertThat(raw.get("import_status")).isEqualTo("success");
        assertThat(raw.get("channel_code")).isEqualTo(CHANNEL_CODE);
        assertThat(String.valueOf(raw.get("raw_payload"))).contains("third-party-callback-test");
    }

    @Test
    @Timeout(60)
    void channelCallbacksOrderImport_shouldRejectMissingTestTokenWithoutCreatingOrder() throws Exception {
        prepareChannelMapping();
        LocalDate checkIn = LocalDate.now(SHANGHAI_ZONE).plusDays(27);
        LocalDate checkOut = checkIn.plusDays(1);
        String outOrderNo = "MT-CB-202606100401";

        mockMvc.perform(post("/channelCallbacks/{channelCode}/orders/import", CHANNEL_CODE)
                        .header(CHANNEL_OPERATOR_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(callbackImportPayload(outOrderNo, OUT_ROOM_CATEGORY_ID, checkIn, checkOut)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("第三方渠道回调认证失败"));

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM order_main WHERE camp_id = ? AND out_order_no = ?",
                Integer.class,
                CAMP_ID,
                ACCOUNT_ID + ":" + outOrderNo
        );
        Integer rawCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM channel_order_raw WHERE account_id = ? AND out_order_no = ?",
                Integer.class,
                ACCOUNT_ID,
                outOrderNo
        );
        assertThat(orderCount).isZero();
        assertThat(rawCount).isZero();
    }

    @Test
    @Timeout(60)
    void channelOrdersImport_shouldCreateOrderAndAutoArrangeAvailableRoom() throws Exception {
        prepareChannelMapping();
        LocalDate checkIn = LocalDate.now(SHANGHAI_ZONE).plusDays(15);
        LocalDate checkOut = checkIn.plusDays(1);

        mockMvc.perform(post("/channelOrders/import")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importPayload(OUT_ORDER_NO, OUT_ROOM_CATEGORY_ID, checkIn, checkOut)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.outOrderNo").value(OUT_ORDER_NO))
                .andExpect(jsonPath("$.data.accountId").value(String.valueOf(ACCOUNT_ID)))
                .andExpect(jsonPath("$.data.channelName").value(CHANNEL_NAME))
                .andExpect(jsonPath("$.data.status").value("booked"))
                .andExpect(jsonPath("$.data.created").value(true));

        var order = jdbcTemplate.queryForMap(
                """
                        SELECT channel_id,
                               out_order_no,
                               channel_order_no_snapshot,
                               source_type,
                               source_label_snapshot,
                               poi_id,
                               room_category_id,
                               room_id,
                               room_name_snapshot,
                               status,
                               payment_status
                        FROM order_main
                        WHERE camp_id = ? AND out_order_no = ?
                        """,
                CAMP_ID,
                OUT_ORDER_KEY
        );
        assertThat(((Number) order.get("channel_id")).longValue()).isEqualTo(ACCOUNT_ID);
        assertThat(order.get("channel_order_no_snapshot")).isEqualTo(OUT_ORDER_NO);
        assertThat(order.get("source_type")).isEqualTo("channel");
        assertThat(order.get("source_label_snapshot")).isEqualTo(CHANNEL_NAME);
        assertThat(((Number) order.get("poi_id")).longValue()).isEqualTo(OrderTestCatalogFixture.POI_ID);
        assertThat(((Number) order.get("room_category_id")).longValue())
                .isEqualTo(OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID);
        assertThat(((Number) order.get("room_id")).longValue())
                .isEqualTo(OrderTestCatalogFixture.STANDARD_ROOM_ID);
        assertThat(order.get("room_name_snapshot")).isEqualTo(OrderTestCatalogFixture.STANDARD_ROOM_NAME);
        assertThat(order.get("status")).isEqualTo("booked");
        assertThat(order.get("payment_status")).isEqualTo("paid");

        var raw = jdbcTemplate.queryForMap(
                """
                        SELECT out_order_no, pms_order_id, import_status, channel_code, error_message, raw_payload
                        FROM channel_order_raw
                        WHERE account_id = ? AND out_order_no = ?
                        """,
                ACCOUNT_ID,
                OUT_ORDER_NO
        );
        assertThat(raw.get("out_order_no")).isEqualTo(OUT_ORDER_NO);
        assertThat(raw.get("pms_order_id")).isNotNull();
        assertThat(raw.get("import_status")).isEqualTo("success");
        assertThat(raw.get("channel_code")).isEqualTo(CHANNEL_CODE);
        assertThat(raw.get("error_message")).isNull();
        assertThat(String.valueOf(raw.get("raw_payload"))).contains("apifox-demo");
    }

    @Test
    @Timeout(60)
    void channelOrdersImport_shouldCreateOrderByRoomCategoryNameWhenExternalRoomCategoryIdIsOmitted() throws Exception {
        prepareChannelMapping();
        LocalDate checkIn = LocalDate.now(SHANGHAI_ZONE).plusDays(24);
        LocalDate checkOut = checkIn.plusDays(1);
        String outOrderNo = "MT202606100005";

        mockMvc.perform(post("/channelOrders/import")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importPayloadWithRoomCategoryName(
                                outOrderNo,
                                OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_NAME,
                                checkIn,
                                checkOut
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.outOrderNo").value(outOrderNo))
                .andExpect(jsonPath("$.data.created").value(true));

        var order = jdbcTemplate.queryForMap(
                """
                        SELECT room_category_id, room_id, room_name_snapshot, status
                        FROM order_main
                        WHERE camp_id = ? AND out_order_no = ?
                        """,
                CAMP_ID,
                ACCOUNT_ID + ":" + outOrderNo
        );
        assertThat(((Number) order.get("room_category_id")).longValue())
                .isEqualTo(OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID);
        assertThat(((Number) order.get("room_id")).longValue())
                .isEqualTo(OrderTestCatalogFixture.STANDARD_ROOM_ID);
        assertThat(order.get("room_name_snapshot")).isEqualTo(OrderTestCatalogFixture.STANDARD_ROOM_NAME);
        assertThat(order.get("status")).isEqualTo("booked");
    }

    @Test
    @Timeout(60)
    void channelOrdersImport_shouldPersistSeparatedMeituanHomestayChannelCode() throws Exception {
        prepareHomestayChannelMapping();
        LocalDate checkIn = LocalDate.now(SHANGHAI_ZONE).plusDays(16);
        LocalDate checkOut = checkIn.plusDays(1);
        String outOrderNo = "MTH202606100001";

        mockMvc.perform(post("/channelOrders/import")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importPayload(
                                HOMESTAY_CHANNEL_CODE,
                                HOMESTAY_ACCOUNT_ID,
                                outOrderNo,
                                OUT_ROOM_CATEGORY_ID,
                                checkIn,
                                checkOut
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountId").value(String.valueOf(HOMESTAY_ACCOUNT_ID)))
                .andExpect(jsonPath("$.data.channelName").value(HOMESTAY_CHANNEL_NAME));

        String rawChannelCode = jdbcTemplate.queryForObject(
                """
                        SELECT channel_code
                        FROM channel_order_raw
                        WHERE account_id = ? AND out_order_no = ?
                        """,
                String.class,
                HOMESTAY_ACCOUNT_ID,
                outOrderNo
        );
        assertThat(rawChannelCode).isEqualTo(HOMESTAY_CHANNEL_CODE);
    }

    @Test
    @Timeout(60)
    void channelOrdersImport_shouldRejectAmbiguousMeituanChannelCode() throws Exception {
        prepareChannelMapping();
        LocalDate checkIn = LocalDate.now(SHANGHAI_ZONE).plusDays(18);
        LocalDate checkOut = checkIn.plusDays(1);
        String outOrderNo = "MT202606100002";

        mockMvc.perform(post("/channelOrders/import")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importPayload("meituan", ACCOUNT_ID, outOrderNo, OUT_ROOM_CATEGORY_ID, checkIn, checkOut)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("channelCode=meituan 无法区分美团酒店和美团民宿，请使用 meituan_hotel 或 meituan_homestay"));

        Integer rawCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM channel_order_raw WHERE account_id = ? AND out_order_no = ?",
                Integer.class,
                ACCOUNT_ID,
                outOrderNo
        );
        assertThat(rawCount).isZero();
    }

    @Test
    @Timeout(60)
    void channelOrdersImport_shouldBeIdempotentByAccountAndExternalOrderNo() throws Exception {
        prepareChannelMapping();
        LocalDate checkIn = LocalDate.now(SHANGHAI_ZONE).plusDays(17);
        LocalDate checkOut = checkIn.plusDays(1);

        mockMvc.perform(post("/channelOrders/import")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importPayload(OUT_ORDER_NO, OUT_ROOM_CATEGORY_ID, checkIn, checkOut)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.created").value(true));

        String firstOrderId = jdbcTemplate.queryForObject(
                "SELECT CAST(order_id AS CHAR) FROM order_main WHERE camp_id = ? AND out_order_no = ?",
                String.class,
                CAMP_ID,
                OUT_ORDER_KEY
        );

        mockMvc.perform(post("/channelOrders/import")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importPayload(OUT_ORDER_NO, OUT_ROOM_CATEGORY_ID, checkIn, checkOut)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderId").value(firstOrderId))
                .andExpect(jsonPath("$.data.created").value(false));

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM order_main WHERE camp_id = ? AND out_order_no = ?",
                Integer.class,
                CAMP_ID,
                OUT_ORDER_KEY
        );
        Integer rawCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM channel_order_raw WHERE account_id = ? AND out_order_no = ?",
                Integer.class,
                ACCOUNT_ID,
                OUT_ORDER_NO
        );
        assertThat(orderCount).isEqualTo(1);
        assertThat(rawCount).isEqualTo(1);
    }

    @Test
    @Timeout(60)
    void channelOrdersImport_shouldRejectSameExternalOrderNoWithDifferentPayload() throws Exception {
        prepareChannelMapping();
        LocalDate checkIn = LocalDate.now(SHANGHAI_ZONE).plusDays(25);
        LocalDate checkOut = checkIn.plusDays(1);
        String outOrderNo = "MT202606100006";

        mockMvc.perform(post("/channelOrders/import")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importPayload(outOrderNo, OUT_ROOM_CATEGORY_ID, checkIn, checkOut)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.created").value(true));

        mockMvc.perform(post("/channelOrders/import")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importPayloadWithRoomCategoryName(
                                outOrderNo,
                                OrderTestCatalogFixture.DELUXE_ROOM_CATEGORY_NAME,
                                checkIn,
                                checkOut
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("外部订单号已导入，但本次请求内容与原订单不一致，请更换 outOrderNo 或取消原订单后重试"));

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM order_main WHERE camp_id = ? AND out_order_no = ?",
                Integer.class,
                CAMP_ID,
                ACCOUNT_ID + ":" + outOrderNo
        );
        assertThat(orderCount).isEqualTo(1);
    }

    @Test
    @Timeout(60)
    void channelOrdersImport_shouldAutoArrangeExistingImportedOrderWithoutRoomOnIdempotentRetry() throws Exception {
        prepareChannelMapping();
        LocalDate checkIn = LocalDate.now(SHANGHAI_ZONE).plusDays(22);
        LocalDate checkOut = checkIn.plusDays(1);
        String outOrderNo = "MT202606100003";
        String internalOutOrderNo = ACCOUNT_ID + ":" + outOrderNo;

        mockMvc.perform(post("/channelOrders/import")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importPayload(outOrderNo, OUT_ROOM_CATEGORY_ID, checkIn, checkOut)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.created").value(true));

        String orderId = jdbcTemplate.queryForObject(
                "SELECT CAST(order_id AS CHAR) FROM order_main WHERE camp_id = ? AND out_order_no = ?",
                String.class,
                CAMP_ID,
                internalOutOrderNo
        );
        jdbcTemplate.update(
                """
                        UPDATE order_main
                        SET room_id = NULL,
                            room_name_snapshot = NULL,
                            room_snapshot_json = CAST(? AS JSON)
                        WHERE camp_id = ? AND order_id = ?
                        """,
                """
                        [{"roomCategoryId":"%s","roomCategoryName":"%s","quantity":1}]
                        """.formatted(
                        OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID,
                        OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_NAME
                ),
                CAMP_ID,
                Long.valueOf(orderId)
        );

        mockMvc.perform(post("/channelOrders/import")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importPayload(outOrderNo, OUT_ROOM_CATEGORY_ID, checkIn, checkOut)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.created").value(false));

        var order = jdbcTemplate.queryForMap(
                """
                        SELECT room_id, room_name_snapshot, room_snapshot_json
                        FROM order_main
                        WHERE camp_id = ? AND order_id = ?
                        """,
                CAMP_ID,
                Long.valueOf(orderId)
        );
        assertThat(((Number) order.get("room_id")).longValue())
                .isEqualTo(OrderTestCatalogFixture.STANDARD_ROOM_ID);
        assertThat(order.get("room_name_snapshot")).isEqualTo(OrderTestCatalogFixture.STANDARD_ROOM_NAME);
        assertThat(String.valueOf(order.get("room_snapshot_json"))).contains("\"roomId\": \"23001\"");
        assertThat(String.valueOf(order.get("room_snapshot_json"))).contains(OrderTestCatalogFixture.STANDARD_ROOM_NAME);
    }

    @Test
    @Timeout(60)
    void channelOrdersImport_shouldSkipNoShowRoomAndAutoArrangeCheckedOutVacantRoom() throws Exception {
        prepareChannelMapping();
        LocalDate checkIn = LocalDate.now(SHANGHAI_ZONE).plusDays(23);
        LocalDate checkOut = checkIn.plusDays(1);
        String outOrderNo = "MT202606100004";
        insertRoomOrder(
                42001L,
                "no_show",
                OrderTestCatalogFixture.STANDARD_ROOM_ID,
                OrderTestCatalogFixture.STANDARD_ROOM_NAME,
                checkIn,
                checkOut
        );
        insertRoomOrder(
                42002L,
                "completed",
                OrderTestCatalogFixture.STANDARD_CHANGE_ROOM_ID,
                OrderTestCatalogFixture.STANDARD_CHANGE_ROOM_NAME,
                checkIn,
                checkOut
        );

        mockMvc.perform(post("/channelOrders/import")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importPayload(outOrderNo, OUT_ROOM_CATEGORY_ID, checkIn, checkOut)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.created").value(true));

        var order = jdbcTemplate.queryForMap(
                """
                        SELECT room_id, room_name_snapshot
                        FROM order_main
                        WHERE camp_id = ? AND out_order_no = ?
                        """,
                CAMP_ID,
                ACCOUNT_ID + ":" + outOrderNo
        );
        assertThat(((Number) order.get("room_id")).longValue())
                .isEqualTo(OrderTestCatalogFixture.STANDARD_CHANGE_ROOM_ID);
        assertThat(order.get("room_name_snapshot")).isEqualTo(OrderTestCatalogFixture.STANDARD_CHANGE_ROOM_NAME);
    }

    @Test
    @Timeout(60)
    void channelOrdersImport_shouldFailClearlyWhenExternalRoomCategoryIsNotMapped() throws Exception {
        prepareChannelMapping();
        LocalDate checkIn = LocalDate.now(SHANGHAI_ZONE).plusDays(19);
        LocalDate checkOut = checkIn.plusDays(1);
        String missingOutRoomCategoryId = "mt-room-missing";
        String missingOutOrderNo = "MT202606100404";

        mockMvc.perform(post("/channelOrders/import")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importPayload(missingOutOrderNo, missingOutRoomCategoryId, checkIn, checkOut)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().encoding("UTF-8"))
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("未找到渠道房型映射，请检查 outPoiId 和 outRoomCategoryId 是否已关联"));

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM order_main WHERE camp_id = ? AND out_order_no = ?",
                Integer.class,
                CAMP_ID,
                ACCOUNT_ID + ":" + missingOutOrderNo
        );
        var raw = jdbcTemplate.queryForMap(
                """
                        SELECT import_status, error_message
                        FROM channel_order_raw
                        WHERE account_id = ? AND out_order_no = ?
                        """,
                ACCOUNT_ID,
                missingOutOrderNo
        );
        assertThat(orderCount).isZero();
        assertThat(raw.get("import_status")).isEqualTo("failed");
        assertThat(String.valueOf(raw.get("error_message"))).contains("未找到渠道房型映射");
    }

    @Test
    @Timeout(60)
    void channelOrdersImport_shouldRejectWhenUnassignedChannelOrdersConsumeRoomCategoryStock() throws Exception {
        prepareChannelMapping();
        LocalDate checkIn = LocalDate.now(SHANGHAI_ZONE).plusDays(21);
        LocalDate checkOut = checkIn.plusDays(1);
        insertUnassignedChannelOrder(41001L, "MT-STOCK-001", checkIn, checkOut);
        insertUnassignedChannelOrder(41002L, "MT-STOCK-002", checkIn, checkOut);
        insertUnassignedChannelOrder(41003L, "MT-STOCK-003", checkIn, checkOut);

        mockMvc.perform(post("/channelOrders/import")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(importPayload("MT-STOCK-004", OUT_ROOM_CATEGORY_ID, checkIn, checkOut)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.success").value(false));

        Integer orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM order_main WHERE camp_id = ? AND out_order_no = ?",
                Integer.class,
                CAMP_ID,
                ACCOUNT_ID + ":MT-STOCK-004"
        );
        assertThat(orderCount).isZero();
    }

    private String importPayload(String outOrderNo, String outRoomCategoryId, LocalDate checkIn, LocalDate checkOut) {
        return importPayload(CHANNEL_CODE, ACCOUNT_ID, outOrderNo, outRoomCategoryId, checkIn, checkOut);
    }

    private String callbackImportPayload(
            String outOrderNo,
            String outRoomCategoryId,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        return """
                {
                  "accountId":"%s",
                  "outOrderNo":"%s",
                  "outPoiId":"%s",
                  "outRoomCategoryId":"%s",
                  "contactName":"Channel Import Guest",
                  "contactMobile":"13800138000",
                  "checkInDate":"%s",
                  "checkOutDate":"%s",
                  "quantity":1,
                  "totalPrice":28800,
                  "totalPayPrice":27600,
                  "commissionPrice":1200,
                  "paymentStatus":"paid",
                  "channelStatus":"confirmed",
                  "remark":"channel callback import test",
                  "rawPayload":{
                    "source":"third-party-callback-test",
                    "externalRoomCategoryId":"%s"
                  }
                }
                """.formatted(
                ACCOUNT_ID,
                outOrderNo,
                OUT_POI_ID,
                outRoomCategoryId,
                checkIn,
                checkOut,
                outRoomCategoryId
        );
    }

    private String importPayload(
            String channelCode,
            long accountId,
            String outOrderNo,
            String outRoomCategoryId,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        return """
                {
                  "channelCode":"%s",
                  "accountId":"%s",
                  "outOrderNo":"%s",
                  "outPoiId":"%s",
                  "outRoomCategoryId":"%s",
                  "contactName":"Channel Import Guest",
                  "contactMobile":"13800138000",
                  "checkInDate":"%s",
                  "checkOutDate":"%s",
                  "quantity":1,
                  "totalPrice":28800,
                  "totalPayPrice":27600,
                  "commissionPrice":1200,
                  "paymentStatus":"paid",
                  "channelStatus":"confirmed",
                  "remark":"channel import test",
                  "rawPayload":{
                    "source":"apifox-demo",
                    "externalRoomCategoryId":"%s"
                  }
                }
                """.formatted(
                channelCode,
                accountId,
                outOrderNo,
                OUT_POI_ID,
                outRoomCategoryId,
                checkIn,
                checkOut,
                outRoomCategoryId
        );
    }

    private String importPayloadWithRoomCategoryName(
            String outOrderNo,
            String roomCategoryName,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        return """
                {
                  "channelCode":"%s",
                  "accountId":"%s",
                  "outOrderNo":"%s",
                  "outPoiId":"%s",
                  "roomCategoryName":"%s",
                  "contactName":"Channel Import Guest",
                  "contactMobile":"13800138000",
                  "checkInDate":"%s",
                  "checkOutDate":"%s",
                  "quantity":1,
                  "totalPrice":28800,
                  "totalPayPrice":27600,
                  "commissionPrice":1200,
                  "paymentStatus":"paid",
                  "channelStatus":"confirmed",
                  "remark":"channel import test by room category name",
                  "rawPayload":{
                    "source":"apifox-demo",
                    "roomCategoryName":"%s"
                  }
                }
                """.formatted(
                CHANNEL_CODE,
                ACCOUNT_ID,
                outOrderNo,
                OUT_POI_ID,
                roomCategoryName,
                checkIn,
                checkOut,
                roomCategoryName
        );
    }

    private void prepareChannelMapping() {
        ensureChannelOrderRawTable();
        resetChannelImportData();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        jdbcTemplate.update("""
                        INSERT INTO channel_account (
                            account_id,
                            camp_id,
                            channel_id,
                            channel_name,
                            account_name,
                            out_account_id,
                            status,
                            authorized_at,
                            config_json
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), CAST(? AS JSON))
                        ON DUPLICATE KEY UPDATE
                            camp_id = VALUES(camp_id),
                            channel_id = VALUES(channel_id),
                            channel_name = VALUES(channel_name),
                            account_name = VALUES(account_name),
                            out_account_id = VALUES(out_account_id),
                            status = VALUES(status),
                            config_json = VALUES(config_json)
                        """,
                ACCOUNT_ID,
                CAMP_ID,
                CHANNEL_ID,
                CHANNEL_NAME,
                "Meituan Test Account",
                "mt-account-25351",
                "authorized",
                "{\"channelCode\":\"meituan_hotel\"}"
        );
        jdbcTemplate.update("""
                        INSERT INTO channel_poi_rel (
                            id,
                            camp_id,
                            account_id,
                            poi_id,
                            out_poi_id,
                            sync_status
                        ) VALUES (?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            camp_id = VALUES(camp_id),
                            poi_id = VALUES(poi_id),
                            out_poi_id = VALUES(out_poi_id),
                            sync_status = VALUES(sync_status)
                        """,
                2535101L,
                CAMP_ID,
                ACCOUNT_ID,
                OrderTestCatalogFixture.POI_ID,
                OUT_POI_ID,
                "synced"
        );
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
                        ON DUPLICATE KEY UPDATE
                            camp_id = VALUES(camp_id),
                            room_category_id = VALUES(room_category_id),
                            out_room_category_id = VALUES(out_room_category_id),
                            project_type = VALUES(project_type),
                            shelf_status = VALUES(shelf_status),
                            audit_status = VALUES(audit_status)
                        """,
                2535201L,
                CAMP_ID,
                ACCOUNT_ID,
                OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID,
                OUT_ROOM_CATEGORY_ID,
                "calendar_room",
                "on_shelf",
                "approved"
        );
    }

    private void prepareHomestayChannelMapping() {
        ensureChannelOrderRawTable();
        resetChannelImportData();
        OrderTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        jdbcTemplate.update("""
                        INSERT INTO channel_account (
                            account_id,
                            camp_id,
                            channel_id,
                            channel_name,
                            account_name,
                            out_account_id,
                            status,
                            authorized_at,
                            config_json
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), CAST(? AS JSON))
                        ON DUPLICATE KEY UPDATE
                            camp_id = VALUES(camp_id),
                            channel_id = VALUES(channel_id),
                            channel_name = VALUES(channel_name),
                            account_name = VALUES(account_name),
                            out_account_id = VALUES(out_account_id),
                            status = VALUES(status),
                            config_json = VALUES(config_json)
                        """,
                HOMESTAY_ACCOUNT_ID,
                CAMP_ID,
                HOMESTAY_CHANNEL_ID,
                HOMESTAY_CHANNEL_NAME,
                "Meituan Homestay Test Account",
                "mth-account-25352",
                "authorized",
                "{\"channelCode\":\"meituan_homestay\"}"
        );
        jdbcTemplate.update("""
                        INSERT INTO channel_poi_rel (
                            id,
                            camp_id,
                            account_id,
                            poi_id,
                            out_poi_id,
                            sync_status
                        ) VALUES (?, ?, ?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE
                            camp_id = VALUES(camp_id),
                            poi_id = VALUES(poi_id),
                            out_poi_id = VALUES(out_poi_id),
                            sync_status = VALUES(sync_status)
                        """,
                2535201L,
                CAMP_ID,
                HOMESTAY_ACCOUNT_ID,
                OrderTestCatalogFixture.POI_ID,
                OUT_POI_ID,
                "synced"
        );
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
                        ON DUPLICATE KEY UPDATE
                            camp_id = VALUES(camp_id),
                            room_category_id = VALUES(room_category_id),
                            out_room_category_id = VALUES(out_room_category_id),
                            project_type = VALUES(project_type),
                            shelf_status = VALUES(shelf_status),
                            audit_status = VALUES(audit_status)
                        """,
                2535202L,
                CAMP_ID,
                HOMESTAY_ACCOUNT_ID,
                OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID,
                OUT_ROOM_CATEGORY_ID,
                "calendar_room",
                "on_shelf",
                "approved"
        );
    }

    private void ensureChannelOrderRawTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS channel_order_raw (
                  raw_id BIGINT UNSIGNED NOT NULL COMMENT 'primary key',
                  camp_id BIGINT UNSIGNED NOT NULL COMMENT 'camp id',
                  account_id BIGINT UNSIGNED NOT NULL COMMENT 'channel account id',
                  channel_id BIGINT UNSIGNED NOT NULL COMMENT 'channel id',
                  channel_code VARCHAR(32) NOT NULL COMMENT 'channel code',
                  out_order_no VARCHAR(128) NOT NULL COMMENT 'external order no',
                  pms_order_id BIGINT UNSIGNED DEFAULT NULL COMMENT 'PMS order id',
                  import_status VARCHAR(32) NOT NULL DEFAULT 'processing' COMMENT 'import status',
                  raw_payload JSON DEFAULT NULL COMMENT 'raw payload',
                  error_message VARCHAR(255) DEFAULT NULL COMMENT 'error message',
                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'created time',
                  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'updated time',
                  PRIMARY KEY (raw_id),
                  UNIQUE KEY uk_channel_order_raw_account_order (account_id, out_order_no),
                  KEY idx_channel_order_raw_pms_order (pms_order_id),
                  KEY idx_channel_order_raw_camp_status (camp_id, import_status)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='channel order raw payload'
                """);
    }

    private void resetChannelImportData() {
        jdbcTemplate.update("DELETE FROM channel_order_raw WHERE account_id IN (?, ?)", ACCOUNT_ID, HOMESTAY_ACCOUNT_ID);
        jdbcTemplate.update(
                "DELETE FROM distribution_order WHERE camp_id = ? AND channel_id IN (?, ?)",
                CAMP_ID,
                ACCOUNT_ID,
                HOMESTAY_ACCOUNT_ID
        );
        jdbcTemplate.update(
                "DELETE FROM order_guest WHERE order_id IN (SELECT order_id FROM order_main WHERE camp_id = ? AND (out_order_no LIKE ? OR out_order_no LIKE ?))",
                CAMP_ID,
                ACCOUNT_ID + ":%",
                HOMESTAY_ACCOUNT_ID + ":%"
        );
        jdbcTemplate.update(
                "DELETE FROM ledger_entry WHERE camp_id = ? AND order_id IN (SELECT order_id FROM order_main WHERE camp_id = ? AND (out_order_no LIKE ? OR out_order_no LIKE ?))",
                CAMP_ID,
                CAMP_ID,
                ACCOUNT_ID + ":%",
                HOMESTAY_ACCOUNT_ID + ":%"
        );
        jdbcTemplate.update(
                "DELETE FROM order_payment_record WHERE camp_id = ? AND order_id IN (SELECT order_id FROM order_main WHERE camp_id = ? AND (out_order_no LIKE ? OR out_order_no LIKE ?))",
                CAMP_ID,
                CAMP_ID,
                ACCOUNT_ID + ":%",
                HOMESTAY_ACCOUNT_ID + ":%"
        );
        jdbcTemplate.update(
                "DELETE FROM order_main WHERE camp_id = ? AND (out_order_no LIKE ? OR out_order_no LIKE ?)",
                CAMP_ID,
                ACCOUNT_ID + ":%",
                HOMESTAY_ACCOUNT_ID + ":%"
        );
        jdbcTemplate.update("DELETE FROM channel_room_category_rel WHERE account_id IN (?, ?)", ACCOUNT_ID, HOMESTAY_ACCOUNT_ID);
        jdbcTemplate.update("DELETE FROM channel_poi_rel WHERE account_id IN (?, ?)", ACCOUNT_ID, HOMESTAY_ACCOUNT_ID);
        jdbcTemplate.update("DELETE FROM channel_account WHERE account_id IN (?, ?)", ACCOUNT_ID, HOMESTAY_ACCOUNT_ID);
    }

    private void insertUnassignedChannelOrder(long orderId, String externalOutOrderNo, LocalDate checkIn, LocalDate checkOut) {
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
                            channel_order_no_snapshot,
                            room_snapshot_json,
                            remark,
                            created_at,
                            updated_at,
                            created_by,
                            updated_by,
                            is_deleted,
                            version_no
                        ) VALUES (?, ?, ?, ?, NULL, ?, NULL, ?, ?, 'daily_room', 'booked', ?, ?, ?, ?, ?, ?, 0, ?, 0, 0, 0, 0, 0, ?, 'paid', 17101, 17206, 'channel', ?, ?, NULL, 'daily_room', ?, ?, CAST(? AS JSON), ?, NOW(), NOW(), 12001, 12001, 0, 0)
                        """,
                orderId,
                CAMP_ID,
                OrderTestCatalogFixture.POI_ID,
                OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID,
                ACCOUNT_ID,
                "CHANNEL-" + orderId,
                ACCOUNT_ID + ":" + externalOutOrderNo,
                "Unassigned Channel Guest",
                "13800138000",
                checkIn.atTime(14, 0),
                checkOut.atTime(12, 0),
                Math.max(1, (int) (checkOut.toEpochDay() - checkIn.toEpochDay())),
                28800,
                27600,
                27600,
                "Order Test Poi",
                OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_NAME,
                CHANNEL_NAME,
                externalOutOrderNo,
                """
                        [{"roomCategoryId":"%s","roomCategoryName":"%s","quantity":1}]
                        """.formatted(
                        OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID,
                        OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_NAME
                ),
                "stock consumed by unassigned channel order"
        );
    }

    private void insertRoomOrder(
            long orderId,
            String status,
            long roomId,
            String roomName,
            LocalDate checkIn,
            LocalDate checkOut
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
                            channel_order_no_snapshot,
                            room_snapshot_json,
                            remark,
                            created_at,
                            updated_at,
                            created_by,
                            updated_by,
                            is_deleted,
                            version_no
                        ) VALUES (?, ?, ?, ?, ?, ?, NULL, ?, ?, 'daily_room', ?, ?, ?, ?, ?, ?, ?, 0, ?, 0, 0, 0, 0, 0, ?, 'paid', 17101, 17206, 'frontdesk', ?, ?, ?, 'daily_room', ?, ?, CAST(? AS JSON), ?, NOW(), NOW(), 12001, 12001, 0, 0)
                        """,
                orderId,
                CAMP_ID,
                OrderTestCatalogFixture.POI_ID,
                OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID,
                roomId,
                ACCOUNT_ID,
                "ROOM-" + orderId,
                "ROOM-OUT-" + orderId,
                status,
                "Room Status Guest " + orderId,
                "13800138000",
                checkIn.atTime(14, 0),
                checkOut.atTime(12, 0),
                Math.max(1, (int) (checkOut.toEpochDay() - checkIn.toEpochDay())),
                28800,
                27600,
                27600,
                "Order Test Poi",
                OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_NAME,
                roomName,
                "Front Desk",
                "ROOM-OUT-" + orderId,
                """
                        [{"roomCategoryId":"%s","roomCategoryName":"%s","roomId":"%s","roomName":"%s","quantity":1}]
                        """.formatted(
                        OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_ID,
                        OrderTestCatalogFixture.STANDARD_ROOM_CATEGORY_NAME,
                        roomId,
                        roomName
                ),
                "room status fixture"
        );
    }
}
