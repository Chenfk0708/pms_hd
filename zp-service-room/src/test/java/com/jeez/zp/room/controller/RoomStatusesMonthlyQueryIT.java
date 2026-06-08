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
                .andExpect(jsonPath("$.data.list[0].orderType").value("daily_room"))
                .andExpect(jsonPath("$.data.list[0].startAt").value("2026-05-21 14:00:00"))
                .andExpect(jsonPath("$.data.list[0].endAt").value("2026-05-22 12:00:00"))
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
    void roomStatusesOrderDetailsGet_shouldReturnLifecycleLogTimes() throws Exception {
        insertRoomCategory();
        insertRoom();
        insertChannelAccount();
        insertLifecycleLogOrderMain();

        mockMvc.perform(post("/roomStatuses/orderDetails/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "startDate":"2026-05-20",
                                  "days":3,
                                  "roomCategoryIds":["104001"],
                                  "keyword":"月房态日志客人"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].guestName").value("月房态日志客人"))
                .andExpect(jsonPath("$.data.list[0].bookingAt").value("2026-05-20 10:00:00"))
                .andExpect(jsonPath("$.data.list[0].guestRegisteredAt").value("2026-05-21 15:10:00"))
                .andExpect(jsonPath("$.data.list[0].checkedOutAt").value("2026-05-22 09:30:00"));
    }

    @Test
    @Timeout(60)
    void roomStatusesOrderDetailsGet_shouldBackfillLegacyLifecycleLogTimes() throws Exception {
        insertRoomCategory();
        insertRoom();
        insertChannelAccount();
        insertLegacyLifecycleLogOrderMain();

        mockMvc.perform(post("/roomStatuses/orderDetails/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "startDate":"2026-05-20",
                                  "days":3,
                                  "roomCategoryIds":["104001"],
                                  "keyword":"月房态旧日志客人"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].guestName").value("月房态旧日志客人"))
                .andExpect(jsonPath("$.data.list[0].bookingAt").value("2026-05-20 10:00:00"))
                .andExpect(jsonPath("$.data.list[0].guestRegisteredAt").value("2026-05-21 15:10:00"))
                .andExpect(jsonPath("$.data.list[0].checkedOutAt").value("2026-05-22 09:30:00"));
    }

    @Test
    @Timeout(60)
    void roomStatusesOrderDetailsGet_shouldReturnHourlyOrderTypeAndExactTimes() throws Exception {
        insertRoomCategory();
        insertRoom();
        insertChannelAccount();
        insertHourlyOrderMain();

        mockMvc.perform(post("/roomStatuses/orderDetails/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "startDate":"2026-05-20",
                                  "days":3,
                                  "roomCategoryIds":["104001"],
                                  "keyword":"月房态钟点房客人"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].guestName").value("月房态钟点房客人"))
                .andExpect(jsonPath("$.data.list[0].orderType").value("hourly_room"))
                .andExpect(jsonPath("$.data.list[0].startAt").value("2026-05-21 06:00:00"))
                .andExpect(jsonPath("$.data.list[0].endAt").value("2026-05-21 11:00:00"));
    }

    @Test
    @Timeout(60)
    void roomStatusesOrderDetailsGet_shouldFallbackToSourceLabelSnapshotWhenChannelAccountMissing() throws Exception {
        insertRoomCategory();
        insertRoom();
        insertRoomStatusDaily(104301L, "2026-05-20", 3, 1, 2);
        insertRoomStatusDaily(104302L, "2026-05-21", 1, 1, 0);
        insertSnapshotOrderMain(104402L, "MONTHLY-SNAPSHOT-GUEST", "13900139000", "Snapshot OTA");

        mockMvc.perform(post("/roomStatuses/orderDetails/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "startDate":"2026-05-20",
                                  "days":3,
                                  "roomCategoryIds":["104001"],
                                  "keyword":"MONTHLY-SNAPSHOT-GUEST"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].guestName").value("MONTHLY-SNAPSHOT-GUEST"))
                .andExpect(jsonPath("$.data.list[0].channelName").value("Snapshot OTA"));
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

    @Test
    @Timeout(60)
    void roomStatusesCloseSave_shouldPersistRoomBlockAndExposeItToMonthGrid() throws Exception {
        ensureRoomStatusBlockTable();
        seedMonthlyScene();

        mockMvc.perform(post("/roomStatuses/close/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "roomCategoryId":"104001",
                                  "roomId":"104101",
                                  "date":"2026-05-20",
                                  "reason":"月房态手动关房"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roomCategoryId").value(String.valueOf(ROOM_CATEGORY_ID)))
                .andExpect(jsonPath("$.data.roomId").value(String.valueOf(ROOM_ID)))
                .andExpect(jsonPath("$.data.date").value("2026-05-20"))
                .andExpect(jsonPath("$.data.message").value("关房成功"));

        mockMvc.perform(post("/roomStatuses/block/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(monthlyPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value(String.valueOf(ROOM_CATEGORY_ID)))
                .andExpect(jsonPath("$.data.list[0].roomId").value(String.valueOf(ROOM_ID)))
                .andExpect(jsonPath("$.data.list[0].date").value("2026-05-20"))
                .andExpect(jsonPath("$.data.list[0].reason").value("月房态手动关房"));

        Integer availabilityCount = jdbcTemplate.queryForObject(
                "SELECT availability_count FROM room_status_daily WHERE camp_id = ? AND room_category_id = ? AND biz_date = ?",
                Integer.class,
                CAMP_ID,
                ROOM_CATEGORY_ID,
                "2026-05-20"
        );
        Integer closeRoomCount = jdbcTemplate.queryForObject(
                "SELECT close_room_count FROM room_status_daily WHERE camp_id = ? AND room_category_id = ? AND biz_date = ?",
                Integer.class,
                CAMP_ID,
                ROOM_CATEGORY_ID,
                "2026-05-20"
        );
        assert availabilityCount != null;
        assert closeRoomCount != null;
        org.assertj.core.api.Assertions.assertThat(availabilityCount).isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(closeRoomCount).isEqualTo(1);
    }

    @Test
    @Timeout(60)
    void roomStatusesCloseSave_shouldNotOverflowWhenDailyCountersAreAlreadyZero() throws Exception {
        ensureRoomStatusBlockTable();
        insertRoomCategory();
        insertRoom();
        insertRoomStatusDaily(104303L, "2026-05-20", 0, 0, 0);

        mockMvc.perform(post("/roomStatuses/close/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(closePayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roomCategoryId").value(String.valueOf(ROOM_CATEGORY_ID)))
                .andExpect(jsonPath("$.data.roomId").value(String.valueOf(ROOM_ID)))
                .andExpect(jsonPath("$.data.date").value("2026-05-20"));

        Integer availabilityCount = jdbcTemplate.queryForObject(
                "SELECT availability_count FROM room_status_daily WHERE camp_id = ? AND room_category_id = ? AND biz_date = ?",
                Integer.class,
                CAMP_ID,
                ROOM_CATEGORY_ID,
                "2026-05-20"
        );
        Integer vacantCount = jdbcTemplate.queryForObject(
                "SELECT vacant_count FROM room_status_daily WHERE camp_id = ? AND room_category_id = ? AND biz_date = ?",
                Integer.class,
                CAMP_ID,
                ROOM_CATEGORY_ID,
                "2026-05-20"
        );
        Integer closeRoomCount = jdbcTemplate.queryForObject(
                "SELECT close_room_count FROM room_status_daily WHERE camp_id = ? AND room_category_id = ? AND biz_date = ?",
                Integer.class,
                CAMP_ID,
                ROOM_CATEGORY_ID,
                "2026-05-20"
        );
        assert availabilityCount != null;
        assert vacantCount != null;
        assert closeRoomCount != null;
        org.assertj.core.api.Assertions.assertThat(availabilityCount).isEqualTo(0);
        org.assertj.core.api.Assertions.assertThat(vacantCount).isEqualTo(0);
        org.assertj.core.api.Assertions.assertThat(closeRoomCount).isEqualTo(1);
    }

    @Test
    @Timeout(60)
    void roomStatusesOpenSave_shouldRemoveRoomBlockAndRestoreDailyCounters() throws Exception {
        ensureRoomStatusBlockTable();
        seedMonthlyScene();

        mockMvc.perform(post("/roomStatuses/close/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(closePayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/roomStatuses/open/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(closePayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roomCategoryId").value(String.valueOf(ROOM_CATEGORY_ID)))
                .andExpect(jsonPath("$.data.roomId").value(String.valueOf(ROOM_ID)))
                .andExpect(jsonPath("$.data.date").value("2026-05-20"))
                .andExpect(jsonPath("$.data.message").value("开房成功"));

        mockMvc.perform(post("/roomStatuses/block/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(monthlyPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(0));

        Integer availabilityCount = jdbcTemplate.queryForObject(
                "SELECT availability_count FROM room_status_daily WHERE camp_id = ? AND room_category_id = ? AND biz_date = ?",
                Integer.class,
                CAMP_ID,
                ROOM_CATEGORY_ID,
                "2026-05-20"
        );
        Integer closeRoomCount = jdbcTemplate.queryForObject(
                "SELECT close_room_count FROM room_status_daily WHERE camp_id = ? AND room_category_id = ? AND biz_date = ?",
                Integer.class,
                CAMP_ID,
                ROOM_CATEGORY_ID,
                "2026-05-20"
        );
        assert availabilityCount != null;
        assert closeRoomCount != null;
        org.assertj.core.api.Assertions.assertThat(availabilityCount).isEqualTo(3);
        org.assertj.core.api.Assertions.assertThat(closeRoomCount).isEqualTo(0);
    }

    private String closePayload() {
        return """
                {
                  "campId":"10001",
                  "roomCategoryId":"104001",
                  "roomId":"104101",
                  "date":"2026-05-20",
                  "reason":"月房态手动关房"
                }
                """;
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
        jdbcTemplate.update("DELETE FROM room_status_block WHERE camp_id = ? AND room_id = ?", CAMP_ID, ROOM_ID);
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

    private void insertHourlyOrderMain() {
        LocalDateTime startAt = LocalDateTime.of(2026, 5, 21, 6, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 5, 21, 11, 0);
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
                104403L,
                CAMP_ID,
                POI_ID,
                ROOM_CATEGORY_ID,
                ROOM_ID,
                CHANNEL_ACCOUNT_ID,
                null,
                "ORDER-MONTHLY-104403",
                "OUT-ORDER-MONTHLY-104403",
                "hourly_room",
                "booked",
                "月房态钟点房客人",
                "13800138001",
                Timestamp.valueOf(startAt),
                Timestamp.valueOf(endAt),
                0,
                16800L,
                0L,
                16800L,
                0L,
                0L,
                0L,
                0L,
                0L,
                16800L,
                "paid",
                17101L,
                17202L,
                "channel",
                "钟点房备注",
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                14001L,
                14001L
        );
    }

    private void insertLifecycleLogOrderMain() {
        LocalDateTime startAt = LocalDateTime.of(2026, 5, 21, 14, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 5, 22, 12, 0);
        LocalDateTime bookingAt = LocalDateTime.of(2026, 5, 20, 10, 0);
        LocalDateTime guestRegisteredAt = LocalDateTime.of(2026, 5, 21, 15, 10);
        LocalDateTime checkedOutAt = LocalDateTime.of(2026, 5, 22, 9, 30);
        jdbcTemplate.update("""
                        INSERT INTO order_main (
                            order_id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            room_id,
                            channel_id,
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
                            total_pay_price_cent,
                            payment_status,
                            source_type,
                            remark,
                            created_at,
                            updated_at,
                            guest_registered_at,
                            checked_out_at,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                        """,
                104404L,
                CAMP_ID,
                POI_ID,
                ROOM_CATEGORY_ID,
                ROOM_ID,
                CHANNEL_ACCOUNT_ID,
                "ORDER-MONTHLY-104404",
                "OUT-ORDER-MONTHLY-104404",
                "daily_room",
                "completed",
                "月房态日志客人",
                "13800138004",
                Timestamp.valueOf(startAt),
                Timestamp.valueOf(endAt),
                1,
                26800L,
                26800L,
                "paid",
                "channel",
                "lifecycle log remark",
                Timestamp.valueOf(bookingAt),
                Timestamp.valueOf(bookingAt.plusMinutes(30)),
                Timestamp.valueOf(guestRegisteredAt),
                Timestamp.valueOf(checkedOutAt)
        );
    }

    private void insertLegacyLifecycleLogOrderMain() {
        LocalDateTime startAt = LocalDateTime.of(2026, 5, 21, 14, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 5, 22, 12, 0);
        LocalDateTime bookingAt = LocalDateTime.of(2026, 5, 20, 10, 0);
        LocalDateTime guestRegisteredAt = LocalDateTime.of(2026, 5, 21, 15, 10);
        LocalDateTime checkedOutAt = LocalDateTime.of(2026, 5, 22, 9, 30);
        jdbcTemplate.update("""
                        INSERT INTO order_main (
                            order_id,
                            camp_id,
                            poi_id,
                            room_category_id,
                            room_id,
                            channel_id,
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
                            total_pay_price_cent,
                            payment_status,
                            source_type,
                            remark,
                            created_at,
                            updated_at,
                            guest_registered_at,
                            checked_out_at,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, NULL, 0)
                        """,
                104405L,
                CAMP_ID,
                POI_ID,
                ROOM_CATEGORY_ID,
                ROOM_ID,
                CHANNEL_ACCOUNT_ID,
                "ORDER-MONTHLY-104405",
                "OUT-ORDER-MONTHLY-104405",
                "daily_room",
                "completed",
                "月房态旧日志客人",
                "13800138005",
                Timestamp.valueOf(startAt),
                Timestamp.valueOf(endAt),
                1,
                26800L,
                26800L,
                "paid",
                "channel",
                "legacy lifecycle log remark",
                Timestamp.valueOf(bookingAt),
                Timestamp.valueOf(checkedOutAt)
        );
        jdbcTemplate.update("""
                        INSERT INTO order_guest (
                            id,
                            order_id,
                            guest_name,
                            guest_mobile,
                            guest_id_card_type,
                            guest_id_card,
                            guest_type,
                            created_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                10440501L,
                104405L,
                "月房态旧日志客人",
                "13800138005",
                "居民身份证",
                "encrypted-id-card",
                "adult",
                Timestamp.valueOf(guestRegisteredAt)
        );
    }

    private void insertSnapshotOrderMain(long orderId, String guestName, String guestMobile, String sourceLabelSnapshot) {
        LocalDate startDate = LocalDate.of(2026, 5, 21);
        LocalDate endDate = LocalDate.of(2026, 5, 22);
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 20, 11, 0);
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
                            source_label_snapshot,
                            remark,
                            created_at,
                            updated_at,
                            created_by,
                            updated_by,
                            is_deleted,
                            version_no
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0)
                        """,
                orderId,
                CAMP_ID,
                POI_ID,
                ROOM_CATEGORY_ID,
                ROOM_ID,
                null,
                null,
                "ORDER-MONTHLY-" + orderId,
                "OUT-ORDER-MONTHLY-" + orderId,
                "daily_room",
                "booked",
                guestName,
                guestMobile,
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
                sourceLabelSnapshot,
                "monthly snapshot remark",
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                14001L,
                14001L
        );
    }
}
