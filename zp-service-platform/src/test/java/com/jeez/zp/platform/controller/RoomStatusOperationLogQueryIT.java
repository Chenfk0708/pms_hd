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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoomStatusOperationLogQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long POI_ID = 98121L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void roomStatusOperationLogPageGetV2_shouldReturnPagedRowsForCurrentFrontendShape() throws Exception {
        seedRoomStatusLogs();

        mockMvc.perform(post("/roomStatusOperationLog/page/get/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.hasNextPage").value(false))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].roomStatusOperationLogId").value("98622"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("TDD房态日志B"))
                .andExpect(jsonPath("$.data.list[0].roomName").value("B-1001"))
                .andExpect(jsonPath("$.data.list[0].startDate").value("2026-05-19"))
                .andExpect(jsonPath("$.data.list[0].endDate").value("2026-05-20"))
                .andExpect(jsonPath("$.data.list[0].operationContent").value("渠道库存变更"))
                .andExpect(jsonPath("$.data.list[0].adjustContent").value("系统调整"))
                .andExpect(jsonPath("$.data.list[0].userName").value("系统管理员"))
                .andExpect(jsonPath("$.data.list[0].createTime").value("2026-05-19 10:05:32"))
                .andExpect(jsonPath("$.data.list[0].channelRoomStatusOperationLogViews.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].channelRoomStatusOperationLogViews[0].channelName").value("携程"))
                .andExpect(jsonPath("$.data.list[0].channelRoomStatusOperationLogViews[0].channelRoomCategoryProductName").value("TDD房态日志B"))
                .andExpect(jsonPath("$.data.list[0].channelRoomStatusOperationLogViews[0].stockContent").value("关"))
                .andExpect(jsonPath("$.data.list[0].channelRoomStatusOperationLogViews[0].isSuccess").value(1))
                .andExpect(jsonPath("$.data.list[1].roomStatusOperationLogId").value("98621"))
                .andExpect(jsonPath("$.data.list[1].adjustContent").value("手动调整"))
                .andExpect(jsonPath("$.data.list[1].channelRoomStatusOperationLogViews[0].channelName").value("自来客"));
    }

    @Test
    @Timeout(60)
    void roomStatusOperationLogPageGetV2_shouldFilterByAdjustTypeChannelKeywordDateAndOperator() throws Exception {
        seedRoomStatusLogs();

        mockMvc.perform(post("/roomStatusOperationLog/page/get/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1,
                                  "adjustType":2,
                                  "channelId":"5",
                                  "keyword":"库存",
                                  "startDate":"2026-05-19",
                                  "endDate":"2026-05-20",
                                  "createStartTime":"2026-05-19",
                                  "createEndTime":"2026-05-19",
                                  "userName":"系统管理"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].roomStatusOperationLogId").value("98622"))
                .andExpect(jsonPath("$.data.list[0].channelRoomStatusOperationLogViews[0].channelName").value("携程"));
    }

    @Test
    @Timeout(60)
    void roomStatusOperationLogPageGetV2_shouldFallbackCurrentCampForInvalidCampIdAndRejectForeignCamp() throws Exception {
        seedRoomStatusLogs();

        mockMvc.perform(post("/roomStatusOperationLog/page/get/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"mock-camp-main",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1,
                                  "adjustType":1,
                                  "channelId":"0"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].roomStatusOperationLogId").value("98621"));

        mockMvc.perform(post("/roomStatusOperationLog/page/get/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "pageNum":1,
                                  "pageSize":20,
                                  "current":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    private void seedRoomStatusLogs() {
        resetOrders();
        insertPoi(POI_ID, "TDD房态日志门店", 21);
        insertRoomCategory(98221L, "TDD房态日志A", 10);
        insertRoomCategory(98222L, "TDD房态日志B", 20);
        insertRoom(98421L, 98221L, "A-901", 1);
        insertRoom(98422L, 98222L, "B-1001", 2);
        insertChannelAccount(98521L, 5L, "携程");

        insertOrderMain(98621L, null, 98221L, 98421L, "frontdesk", "booked", "paid",
                "张三", "13900000001", LocalDate.of(2026, 5, 18), LocalDate.of(2026, 5, 18),
                LocalDateTime.of(2026, 5, 18, 9, 20, 16), "手动锁房");
        insertOrderMain(98622L, 98521L, 98222L, 98422L, "channel", "checked_in", "paid",
                "李四", "13900000002", LocalDate.of(2026, 5, 19), LocalDate.of(2026, 5, 20),
                LocalDateTime.of(2026, 5, 19, 10, 5, 32), "渠道自动同步");
    }

    private void resetOrders() {
        jdbcTemplate.update("DELETE FROM order_payment_record WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("""
                DELETE le
                FROM ledger_entry le
                JOIN order_main om ON om.order_id = le.order_id
                WHERE om.camp_id = ?
                """, CAMP_ID);
        jdbcTemplate.update("""
                DELETE og
                FROM order_guest og
                JOIN order_main om ON om.order_id = og.order_id
                WHERE om.camp_id = ?
                """, CAMP_ID);
        jdbcTemplate.update("DELETE FROM distribution_order WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("DELETE FROM order_main WHERE camp_id = ?", CAMP_ID);
    }

    private void insertPoi(long poiId, String poiName, int sortNo) {
        jdbcTemplate.update("""
                        INSERT INTO pms_poi (
                            poi_id,
                            camp_id,
                            poi_name,
                            is_availability,
                            sort_no,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                poiId,
                CAMP_ID,
                poiName,
                1,
                sortNo,
                1,
                0
        );
    }

    private void insertRoomCategory(long roomCategoryId, String name, int sortNo) {
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
                roomCategoryId,
                CAMP_ID,
                POI_ID,
                name,
                name,
                1,
                1,
                sortNo,
                0
        );
    }

    private void insertRoom(long roomId, long roomCategoryId, String roomName, int sortNo) {
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
                roomId,
                CAMP_ID,
                POI_ID,
                roomCategoryId,
                roomName,
                "online",
                "normal",
                "clean",
                1,
                sortNo,
                0
        );
    }

    private void insertChannelAccount(long accountId, long channelId, String channelName) {
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
                accountId,
                CAMP_ID,
                channelId,
                channelName,
                channelName + "账号",
                "OUT-" + accountId,
                "authorized"
        );
    }

    private void insertOrderMain(
            long orderId,
            Long channelAccountId,
            long roomCategoryId,
            long roomId,
            String sourceType,
            String status,
            String paymentStatus,
            String guestName,
            String guestMobile,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createdAt,
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
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                orderId,
                CAMP_ID,
                POI_ID,
                roomCategoryId,
                roomId,
                channelAccountId,
                "ORDER-" + orderId,
                "OUT-ORDER-" + orderId,
                "daily_room",
                status,
                guestName,
                guestMobile,
                Timestamp.valueOf(startDate.atTime(LocalTime.of(14, 0))),
                Timestamp.valueOf(endDate.plusDays(1).atTime(LocalTime.of(12, 0))),
                Math.max(1, (int) (endDate.toEpochDay() - startDate.toEpochDay() + 1)),
                26800L,
                0L,
                26800L,
                0L,
                0L,
                0L,
                0L,
                0L,
                26800L,
                paymentStatus,
                17101L,
                17202L,
                sourceType,
                remark,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                12001L,
                12001L,
                0,
                0
        );
    }
}
