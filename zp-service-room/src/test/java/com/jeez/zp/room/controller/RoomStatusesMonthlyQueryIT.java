package com.jeez.zp.room.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoomStatusesMonthlyQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long POI_ID = 11001L;
    private static final long ROOM_CATEGORY_ID = 104001L;
    private static final long ROOM_ID = 104101L;
    private static final long CHANNEL_ACCOUNT_ID = 104201L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void roomStatusesInvGet_shouldReturnDailyInventoryRowsForMonthGrid() throws Exception {
        seedMonthlyScene();

        mockMvc.perform(post("/roomStatuses/inv/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(monthlyPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value(String.valueOf(ROOM_CATEGORY_ID)))
                .andExpect(jsonPath("$.data.list[0].date").value("2026-05-20"))
                .andExpect(jsonPath("$.data.list[0].inventory").value(3))
                .andExpect(jsonPath("$.data.list[1].date").value("2026-05-21"))
                .andExpect(jsonPath("$.data.list[1].inventory").value(1));
    }

    @Test
    @Timeout(60)
    void roomStatusesDailyMonitorGet_shouldReturnRemainColumnsForMonthGrid() throws Exception {
        seedMonthlyScene();

        mockMvc.perform(post("/roomStatuses/dailyMonitor/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(monthlyPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].date").value("2026-05-20"))
                .andExpect(jsonPath("$.data.list[0].remainNum").value(3))
                .andExpect(jsonPath("$.data.list[0].remain").value("余3间"))
                .andExpect(jsonPath("$.data.list[1].date").value("2026-05-21"))
                .andExpect(jsonPath("$.data.list[1].remainNum").value(1));
    }

    @Test
    @Timeout(60)
    void roomStatusesOrderDetailsGet_shouldReturnOrderCardsForMonthGrid() throws Exception {
        seedMonthlyScene();

        mockMvc.perform(post("/roomStatuses/orderDetails/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(monthlyPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value(String.valueOf(ROOM_CATEGORY_ID)))
                .andExpect(jsonPath("$.data.list[0].roomId").value(String.valueOf(ROOM_ID)))
                .andExpect(jsonPath("$.data.list[0].date").value("2026-05-21"))
                .andExpect(jsonPath("$.data.list[0].guestName").value("月房态客人"))
                .andExpect(jsonPath("$.data.list[0].channelName").value("月房态渠道"))
                .andExpect(jsonPath("$.data.list[0].roomFee").value(268.0))
                .andExpect(jsonPath("$.data.list[0].totalIncome").value(268.0))
                .andExpect(jsonPath("$.data.list[0].stayRange").value("2026-05-21-05-22"))
                .andExpect(jsonPath("$.data.list[0].phone").value("13800138000"))
                .andExpect(jsonPath("$.data.list[0].remark").value("有备注"))
                .andExpect(jsonPath("$.data.list[0].hasRemark").value(true))
                .andExpect(jsonPath("$.data.list[0].liveStatusName").value("待入住"))
                .andExpect(jsonPath("$.data.orderArrangementInfos.length()").value(0))
                .andExpect(jsonPath("$.data.pagination.total").value(1));
    }

    @Test
    @Timeout(60)
    void roomStatusesOccBlockAndRedDotGet_shouldReturnFrontendListEnvelope() throws Exception {
        seedMonthlyScene();

        mockMvc.perform(post("/roomStatuses/occ/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(monthlyPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].date").value("2026-05-20"))
                .andExpect(jsonPath("$.data.list[0].availabilityCount").value(3))
                .andExpect(jsonPath("$.data.list[0].openRoomCount").value(1));

        mockMvc.perform(post("/roomStatuses/block/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(monthlyPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(0));

        mockMvc.perform(post("/roomStatuses/redDot/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(monthlyPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(0));
    }

    private String monthlyPayload() {
        return """
                {
                  "campId":"10001",
                  "startDate":"2026-05-20",
                  "days":3,
                  "roomCategoryIds":["104001"]
                }
                """;
    }

    private void seedMonthlyScene() {
        insertRoomCategory();
        insertRoom();
        insertRoomStatusDaily(104301L, "2026-05-20", 3, 1, 2);
        insertRoomStatusDaily(104302L, "2026-05-21", 1, 1, 0);
        insertChannelAccount();
        insertOrderMain();
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
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                ROOM_CATEGORY_ID,
                CAMP_ID,
                POI_ID,
                "TDD月房态剩余接口房型",
                "TDD月房态剩余接口房型",
                1,
                1,
                10,
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
                            lock_status,
                            sale_type,
                            clean_status,
                            status,
                            sort_no,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                ROOM_ID,
                CAMP_ID,
                POI_ID,
                ROOM_CATEGORY_ID,
                "M-2101",
                "online",
                "normal",
                "clean",
                1,
                1,
                0
        );
    }

    private void insertRoomStatusDaily(long id, String bizDate, int availabilityCount, int openRoomCount, int vacantCount) {
        jdbcTemplate.update("""
                        INSERT INTO room_status_daily (
                            id,
                            camp_id,
                            poi_id,
                            biz_date,
                            room_category_id,
                            availability_count,
                            open_room_count,
                            room_sale_count,
                            close_room_count,
                            user_busy_count,
                            retain_count,
                            repair_count,
                            vacant_count,
                            pre_come_count,
                            live_count,
                            pre_leave_count,
                            clean_count,
                            dirty_count
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                id,
                CAMP_ID,
                POI_ID,
                bizDate,
                ROOM_CATEGORY_ID,
                availabilityCount,
                openRoomCount,
                openRoomCount,
                0,
                0,
                0,
                0,
                vacantCount,
                0,
                openRoomCount,
                0,
                availabilityCount,
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
                1042L,
                "月房态渠道",
                "月房态渠道账号",
                "OUT-MONTHLY-1042",
                "authorized"
        );
    }

    private void insertOrderMain() {
        LocalDate startDate = LocalDate.of(2026, 5, 21);
        LocalDate endDate = LocalDate.of(2026, 5, 22);
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 20, 10, 0);
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
                104401L,
                CAMP_ID,
                POI_ID,
                ROOM_CATEGORY_ID,
                ROOM_ID,
                CHANNEL_ACCOUNT_ID,
                null,
                "ORDER-MONTHLY-104401",
                "OUT-ORDER-MONTHLY-104401",
                "daily_room",
                "booked",
                "月房态客人",
                "13800138000",
                Timestamp.valueOf(startDate.atTime(LocalTime.of(14, 0))),
                Timestamp.valueOf(endDate.atTime(LocalTime.of(12, 0))),
                1,
                26800L,
                0L,
                26800L,
                0L,
                0L,
                0L,
                0L,
                0L,
                26800L,
                "paid",
                17101L,
                17202L,
                "channel",
                "有备注",
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                14001L,
                14001L
        );
    }
}