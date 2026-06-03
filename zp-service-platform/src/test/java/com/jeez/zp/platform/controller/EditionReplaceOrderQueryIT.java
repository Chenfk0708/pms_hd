
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

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EditionReplaceOrderQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CURRENT_USER_ID = 12001L;
    private static final long CAMP_ID = 10001L;
    private static final long POI_ID = 11001L;
    private static final long ROOM_CATEGORY_ID = 98501L;
    private static final long ROOM_ID = 98502L;
    private static final long CHANNEL_ACCOUNT_ID = 98503L;
    private static final long CHANNEL_ID = 98504L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void editionReplaceOrderGet_shouldReturnRealDistributionReplaceOrdersAndSummary() throws Exception {
        seedReplaceOrders();

        mockMvc.perform(post("/edition/replace/order/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "receiverStartTime":1779033600000,
                                  "receiverEndTime":1779120000000,
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.summary.pendingReplaceAmount").value(286000))
                .andExpect(jsonPath("$.data.summary.completedReplaceAmount").value(842000))
                .andExpect(jsonPath("$.data.list[0].replaceOrderId").value("replace-98511"))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("ORDER-98511"))
                .andExpect(jsonPath("$.data.list[0].channelOrderNo").value("OUT-REPLACE-98511"))
                .andExpect(jsonPath("$.data.list[0].replaceMonth").value("2026-05"))
                .andExpect(jsonPath("$.data.list[0].channelName").value("TDD Replace OTA"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("TDD Replace Room"))
                .andExpect(jsonPath("$.data.list[0].roomName").value("TDD-REPLACE-501"))
                .andExpect(jsonPath("$.data.list[0].contactName").value("Replace Guest A"))
                .andExpect(jsonPath("$.data.list[0].contactMobile").value("13800009851"))
                .andExpect(jsonPath("$.data.list[0].stayStatus").value("living"))
                .andExpect(jsonPath("$.data.list[0].stayStatusName").value("入住中"))
                .andExpect(jsonPath("$.data.list[0].settlementStatus").value("pending"))
                .andExpect(jsonPath("$.data.list[0].settlementStatusName").value("待置换"))
                .andExpect(jsonPath("$.data.list[0].checkInDate").value("2026-05-18"))
                .andExpect(jsonPath("$.data.list[0].checkOutDate").value("2026-05-19"))
                .andExpect(jsonPath("$.data.list[0].settlementDate").value("2026-05-18"))
                .andExpect(jsonPath("$.data.list[0].settlementAmount").value(468000))
                .andExpect(jsonPath("$.data.list[0].replaceAmount").value(286000))
                .andExpect(jsonPath("$.data.list[0].remark").value("pending replace order"))
                .andExpect(jsonPath("$.data.list[1].replaceOrderId").value("replace-98512"))
                .andExpect(jsonPath("$.data.list[1].stayStatus").value("checkedOut"))
                .andExpect(jsonPath("$.data.list[1].settlementStatus").value("completed"));
    }

    @Test
    @Timeout(60)
    void editionReplaceOrderGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/edition/replace/order/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    private void seedReplaceOrders() {
        resetReplaceOrders();
        insertRoomCategory();
        insertRoom();
        insertChannelAccount();
        insertOrderMain(
                98511L,
                "Replace Guest A",
                "13800009851",
                LocalDate.of(2026, 5, 18),
                LocalDate.of(2026, 5, 19),
                LocalDateTime.of(2026, 5, 18, 10, 0),
                468000L,
                286000L,
                "booked",
                "pending replace order"
        );
        insertOrderMain(
                98512L,
                "Replace Guest B",
                "13800009852",
                LocalDate.of(2026, 5, 16),
                LocalDate.of(2026, 5, 18),
                LocalDateTime.of(2026, 5, 18, 11, 0),
                842000L,
                842000L,
                "checked_out",
                "completed replace order"
        );
        insertOrderMain(
                98513L,
                "Replace Guest C",
                "13800009853",
                LocalDate.of(2026, 5, 20),
                LocalDate.of(2026, 5, 21),
                LocalDateTime.of(2026, 5, 20, 9, 0),
                300000L,
                300000L,
                "booked",
                "outside date range"
        );
    }

    private void resetReplaceOrders() {
        jdbcTemplate.update("DELETE FROM order_main WHERE order_id BETWEEN 98511 AND 98519");
        jdbcTemplate.update("DELETE FROM room WHERE room_id = ?", ROOM_ID);
        jdbcTemplate.update("DELETE FROM channel_account WHERE account_id = ?", CHANNEL_ACCOUNT_ID);
        jdbcTemplate.update("DELETE FROM room_category WHERE room_category_id = ?", ROOM_CATEGORY_ID);
    }

    private void insertRoomCategory() {
        jdbcTemplate.update("""
                        INSERT INTO room_category (
                            room_category_id,
                            camp_id,
                            poi_id,
                            name,
                            display_name,
                            room_count,
                            guest_count,
                            weekday_price_cent,
                            weekend_price_cent,
                            holiday_price_cent,
                            earliest_check_in_hour,
                            latest_check_in_hour,
                            latest_check_out_hour,
                            highlight_description,
                            nearby_description,
                            article_description,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                ROOM_CATEGORY_ID,
                CAMP_ID,
                POI_ID,
                "TDD Replace Room",
                "TDD Replace Room",
                1,
                2,
                468000L,
                468000L,
                468000L,
                14,
                23,
                12,
                "replace room",
                "replace nearby",
                "replace article",
                980,
                1,
                0
        );
    }

    private void insertRoom() {
        jdbcTemplate.update("""
                        INSERT INTO room (
                            room_id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            room_name,
                            room_no,
                            lock_status,
                            sale_type,
                            clean_status,
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                ROOM_ID,
                CAMP_ID,
                POI_ID,
                ROOM_CATEGORY_ID,
                "TDD-REPLACE-501",
                "TDD-REPLACE-501",
                "online",
                "normal",
                "clean",
                1,
                980,
                0
        );
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
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                CHANNEL_ACCOUNT_ID,
                CAMP_ID,
                CHANNEL_ID,
                "TDD Replace OTA",
                "TDD Replace Account",
                "OUT-REPLACE",
                "authorized"
        );
    }

    private void insertOrderMain(
            long orderId,
            String guestName,
            String guestMobile,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createdAt,
            long settlementAmountCent,
            long replaceAmountCent,
            String status,
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
                POI_ID,
                ROOM_CATEGORY_ID,
                ROOM_ID,
                CHANNEL_ACCOUNT_ID,
                null,
                "ORDER-" + orderId,
                "OUT-REPLACE-" + orderId,
                "daily_room",
                status,
                guestName,
                guestMobile,
                Timestamp.valueOf(startDate.atTime(LocalTime.of(14, 0))),
                Timestamp.valueOf(endDate.atTime(LocalTime.of(12, 0))),
                Math.max(1, (int) (endDate.toEpochDay() - startDate.toEpochDay())),
                settlementAmountCent,
                0,
                settlementAmountCent,
                0,
                0,
                0,
                0,
                replaceAmountCent,
                settlementAmountCent,
                "paid",
                17101L,
                17202L,
                "distribution_replace",
                remark,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(20)),
                CURRENT_USER_ID,
                CURRENT_USER_ID
        );
    }
}
