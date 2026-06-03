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
class WorkspaceOrdersQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    private static final long CAMP_ID = 10001L;
    private static final long CHANNEL_ACCOUNT_ID = 29411L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void ordersGet_shouldReturnWorkspaceOrdersByTabAndKeyword() throws Exception {
        seedOrders();

        mockMvc.perform(post("/orders/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "orderType":"11",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "keyword":"Workspace"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.data.pagination.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].channelName").value("Workspace OTA"))
                .andExpect(jsonPath("$.data.list[0].guestName").value("Workspace Arrive"))
                .andExpect(jsonPath("$.data.list[0].guestMobile").value("13939301001"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").exists())
                .andExpect(jsonPath("$.data.list[0].roomName").value(OrderTestCatalogFixture.STANDARD_ROOM_NAME))
                .andExpect(jsonPath("$.data.list[0].dayNum").value(1))
                .andExpect(jsonPath("$.data.list[0].startTime").isNumber())
                .andExpect(jsonPath("$.data.list[0].endTime").isNumber())
                .andExpect(jsonPath("$.data.list[0].orderDetailDisplayStateName").value("待入住"))
                .andExpect(jsonPath("$.data.list[0].statusName").value("待入住"));

        mockMvc.perform(post("/orders/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "orderType":"12",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "keyword":"Stay"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].guestName").value("Workspace Stay"))
                .andExpect(jsonPath("$.data.list[0].orderDetailDisplayStateName").value("在住"));

        mockMvc.perform(post("/orders/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "orderType":"13",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "keyword":"Leave"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].guestName").value("Workspace Leave"))
                .andExpect(jsonPath("$.data.list[0].orderDetailDisplayStateName").value("待退房"));
    }



    @Test
    @Timeout(60)
    void ordersGet_shouldKeepCheckingOutGuestOnlyInDepartingTabWhenOrderEndsToday() throws Exception {
        seedOrders();

        mockMvc.perform(post("/orders/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "orderType":"12",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "keyword":"Workspace Leave"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(0));

        mockMvc.perform(post("/orders/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "orderType":"13",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "keyword":"Workspace Leave"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].guestName").value("Workspace Leave"))
                .andExpect(jsonPath("$.data.list[0].orderDetailDisplayStateName").value("待退房"));
    }

    @Test
    @Timeout(60)
    void ordersGet_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedOrders();

        mockMvc.perform(post("/orders/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "orderType":"11",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "keyword":"Workspace"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].guestName").value("Workspace Arrive"));

        mockMvc.perform(post("/orders/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "orderType":"11",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "keyword":"Workspace"
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
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        insertOrderMain(
                39301L,
                "booked",
                "Workspace Arrive",
                "13939301001",
                today,
                tomorrow,
                today.atTime(8, 0)
        );
        insertOrderMain(
                39302L,
                "checked_in",
                "Workspace Stay",
                "13939302002",
                yesterday,
                tomorrow,
                yesterday.atTime(9, 0)
        );
        insertOrderMain(
                39303L,
                "checked_in",
                "Workspace Leave",
                "13939303003",
                yesterday,
                today,
                yesterday.atTime(10, 0)
        );
    }

    private void resetOrders() {
        jdbcTemplate.update("DELETE FROM order_payment_record WHERE camp_id = ? AND order_id BETWEEN 39301 AND 39303", CAMP_ID);
        jdbcTemplate.update("DELETE FROM distribution_order WHERE camp_id = ? AND source_order_id BETWEEN 39301 AND 39303", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_guest WHERE order_id BETWEEN 39301 AND 39303");
        jdbcTemplate.update("DELETE FROM ledger_entry WHERE camp_id = ? AND order_id BETWEEN 39301 AND 39303", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_main WHERE camp_id = ? AND order_id BETWEEN 39301 AND 39303", CAMP_ID);
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
                18L,
                "Workspace OTA",
                "Workspace Test Account",
                "TEST-WORKSPACE-29411"
        );
    }

    private void insertOrderMain(
            long orderId,
            String status,
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
                CHANNEL_ACCOUNT_ID,
                null,
                "ORDER-WORKSPACE-" + orderId,
                "OUT-WORKSPACE-" + orderId,
                "daily_room",
                status,
                guestName,
                guestMobile,
                Timestamp.valueOf(startDate.atTime(LocalTime.of(14, 0))),
                Timestamp.valueOf(endDate.atTime(LocalTime.of(12, 0))),
                Math.max(1, (int) (endDate.toEpochDay() - startDate.toEpochDay())),
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
                "workspace orders test",
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                12001L,
                12001L
        );
    }
}
