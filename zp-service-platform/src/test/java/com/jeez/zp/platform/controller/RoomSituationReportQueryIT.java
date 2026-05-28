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
class RoomSituationReportQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CURRENT_USER_ID = 12001L;
    private static final long ORIGINAL_CAMP_ID = 10001L;
    private static final long ISOLATED_CAMP_ID = 19701L;
    private static final long ISOLATED_POI_ID = 19721L;
    private static final long ROOM_CATEGORY_A_ID = 19731L;
    private static final long ROOM_CATEGORY_B_ID = 19732L;
    private static final long ROOM_A_1_ID = 19741L;
    private static final long ROOM_A_2_ID = 19742L;
    private static final long ROOM_A_3_ID = 19743L;
    private static final long ROOM_B_1_ID = 19744L;
    private static final long ROOM_B_2_ID = 19745L;
    private static final LocalDate TARGET_DATE = LocalDate.of(2026, 5, 18);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void dailyRoomStatusGet_shouldReturnCurrentFrontendShape() throws Exception {
        seedRoomSituationScene();

        mockMvc.perform(post("/report/dailyRoomStatus/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19701",
                                  "date":"2026-05-18",
                                  "poiIds":["19721"],
                                  "pageNum":1,
                                  "pageSize":10
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
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value("19731"))
                .andExpect(jsonPath("$.data.list[0].roomCategoryName").value("TDD房情房型A"))
                .andExpect(jsonPath("$.data.list[0].availabilityCount").value(3))
                .andExpect(jsonPath("$.data.list[0].openRoomCount").value(1))
                .andExpect(jsonPath("$.data.list[0].roomSaleCount").value(0))
                .andExpect(jsonPath("$.data.list[0].closeRoomCount").value(3))
                .andExpect(jsonPath("$.data.list[0].userBusyNum").value(2))
                .andExpect(jsonPath("$.data.list[0].userBusyRetainNum").value(1))
                .andExpect(jsonPath("$.data.list[0].userBusyRepairNum").value(1))
                .andExpect(jsonPath("$.data.list[0].mainViceRelNum").value(0))
                .andExpect(jsonPath("$.data.list[0].totalVacantRoomCount").value(1))
                .andExpect(jsonPath("$.data.list[0].preComeNum").value(1))
                .andExpect(jsonPath("$.data.list[0].liveNum").value(0))
                .andExpect(jsonPath("$.data.list[0].preLeaveNum").value(0))
                .andExpect(jsonPath("$.data.list[0].cleanNum").value(2))
                .andExpect(jsonPath("$.data.list[0].dirtyNum").value(1))
                .andExpect(jsonPath("$.data.list[1].roomCategoryId").value("19732"))
                .andExpect(jsonPath("$.data.list[1].roomCategoryName").value("TDD房情房型B"))
                .andExpect(jsonPath("$.data.list[1].availabilityCount").value(2))
                .andExpect(jsonPath("$.data.list[1].openRoomCount").value(1))
                .andExpect(jsonPath("$.data.list[1].roomSaleCount").value(0))
                .andExpect(jsonPath("$.data.list[1].closeRoomCount").value(2))
                .andExpect(jsonPath("$.data.list[1].userBusyNum").value(1))
                .andExpect(jsonPath("$.data.list[1].userBusyRetainNum").value(0))
                .andExpect(jsonPath("$.data.list[1].userBusyRepairNum").value(0))
                .andExpect(jsonPath("$.data.list[1].mainViceRelNum").value(1))
                .andExpect(jsonPath("$.data.list[1].totalVacantRoomCount").value(1))
                .andExpect(jsonPath("$.data.list[1].preComeNum").value(0))
                .andExpect(jsonPath("$.data.list[1].liveNum").value(1))
                .andExpect(jsonPath("$.data.list[1].preLeaveNum").value(1))
                .andExpect(jsonPath("$.data.list[1].cleanNum").value(1))
                .andExpect(jsonPath("$.data.list[1].dirtyNum").value(1));
    }

    @Test
    @Timeout(60)
    void dailyRoomStatusGet_shouldFallbackInvalidCampIdAndRejectForeignCamp() throws Exception {
        seedRoomSituationScene();

        mockMvc.perform(post("/report/dailyRoomStatus/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"mock-camp-room-situation",
                                  "date":"2026-05-18",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.list[0].preComeNum").value(1));

        mockMvc.perform(post("/report/dailyRoomStatus/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "date":"2026-05-18",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Timeout(60)
    void forwardRoomStatusGet_shouldReturnForwardStatusRows() throws Exception {
        seedRoomSituationScene();

        mockMvc.perform(post("/report/forwardRoomStatus/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19701",
                                  "startDate":"2026-05-18",
                                  "endDate":"2026-05-21",
                                  "poiIds":["19721"],
                                  "pageNum":1,
                                  "pageSize":10
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
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].roomCategoryId").value("19731"))
                .andExpect(jsonPath("$.data.list[0].availabilityCount").value(3))
                .andExpect(jsonPath("$.data.list[0].forwardRoomStatusList.length()").value(3))
                .andExpect(jsonPath("$.data.list[0].forwardRoomStatusList[0].roomSaleCount").value(0))
                .andExpect(jsonPath("$.data.list[0].forwardRoomStatusList[0].occupationCount").value(1))
                .andExpect(jsonPath("$.data.list[0].forwardRoomStatusList[1].roomSaleCount").value(0))
                .andExpect(jsonPath("$.data.list[0].forwardRoomStatusList[1].occupationCount").value(1))
                .andExpect(jsonPath("$.data.list[0].forwardRoomStatusList[2].roomSaleCount").value(1))
                .andExpect(jsonPath("$.data.list[0].forwardRoomStatusList[2].occupationCount").value(0))
                .andExpect(jsonPath("$.data.list[1].roomCategoryId").value("19732"))
                .andExpect(jsonPath("$.data.list[1].availabilityCount").value(2))
                .andExpect(jsonPath("$.data.list[1].forwardRoomStatusList.length()").value(3))
                .andExpect(jsonPath("$.data.list[1].forwardRoomStatusList[0].roomSaleCount").value(0))
                .andExpect(jsonPath("$.data.list[1].forwardRoomStatusList[0].occupationCount").value(1))
                .andExpect(jsonPath("$.data.list[1].forwardRoomStatusList[1].roomSaleCount").value(1))
                .andExpect(jsonPath("$.data.list[1].forwardRoomStatusList[1].occupationCount").value(0))
                .andExpect(jsonPath("$.data.list[1].forwardRoomStatusList[2].roomSaleCount").value(1))
                .andExpect(jsonPath("$.data.list[1].forwardRoomStatusList[2].occupationCount").value(0));
    }

    private void seedRoomSituationScene() {
        insertCamp();
        rebindCurrentUserCamp();
        insertPoi();
        insertRoomCategory(ROOM_CATEGORY_A_ID, "TDD房情房型A", 10, 3);
        insertRoomCategory(ROOM_CATEGORY_B_ID, "TDD房情房型B", 20, 2);
        insertRoom(ROOM_A_1_ID, ROOM_CATEGORY_A_ID, "A-101", "normal", "clean", 1);
        insertRoom(ROOM_A_2_ID, ROOM_CATEGORY_A_ID, "A-102", "retain", "clean", 2);
        insertRoom(ROOM_A_3_ID, ROOM_CATEGORY_A_ID, "A-103", "repair", "dirty", 3);
        insertRoom(ROOM_B_1_ID, ROOM_CATEGORY_B_ID, "B-201", "normal", "dirty", 1);
        insertRoom(ROOM_B_2_ID, ROOM_CATEGORY_B_ID, "B-202", "linked", "clean", 2);

        insertOrderMain(19761L, ROOM_CATEGORY_A_ID, ROOM_A_1_ID, "booked", "paid",
                "今日预抵客人", "13900001001", TARGET_DATE, TARGET_DATE.plusDays(1), TARGET_DATE.minusDays(1).atTime(9, 0), "room situation arrival");
        insertOrderMain(19762L, ROOM_CATEGORY_B_ID, ROOM_B_1_ID, "checked_in", "paid",
                "今日预离客人", "13900001002", TARGET_DATE.minusDays(1), TARGET_DATE, TARGET_DATE.minusDays(2).atTime(10, 0), "room situation leaving");
    }

    private void rebindCurrentUserCamp() {
        jdbcTemplate.update("UPDATE pms_member SET camp_id = ? WHERE user_id = ?", ISOLATED_CAMP_ID, CURRENT_USER_ID);
    }

    private void insertCamp() {
        jdbcTemplate.update("""
                        INSERT INTO pms_camp (
                            camp_id,
                            name,
                            type,
                            city_name,
                            address,
                            contact_number,
                            status,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                ISOLATED_CAMP_ID,
                "TDD房情门店",
                1,
                "深圳",
                "南山区测试路 18 号",
                "0755-1970101",
                1,
                0
        );
    }

    private void insertPoi() {
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
                ISOLATED_POI_ID,
                ISOLATED_CAMP_ID,
                "TDD房情门店",
                1,
                1,
                1,
                0
        );
    }

    private void insertRoomCategory(long roomCategoryId, String name, int sortNo, int roomCount) {
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
                roomCategoryId,
                ISOLATED_CAMP_ID,
                ISOLATED_POI_ID,
                name,
                name,
                roomCount,
                2,
                26800L,
                28800L,
                30800L,
                14,
                23,
                12,
                name + "亮点",
                name + "周边",
                name + "图文",
                sortNo,
                1,
                0
        );
    }

    private void insertRoom(long roomId, long roomCategoryId, String roomName, String lockStatus, String cleanStatus, int sortNo) {
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
                ISOLATED_CAMP_ID,
                ISOLATED_POI_ID,
                roomCategoryId,
                roomName,
                lockStatus,
                "overnight",
                cleanStatus,
                1,
                sortNo,
                0
        );
    }

    private void insertOrderMain(
            long orderId,
            long roomCategoryId,
            long roomId,
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
                ISOLATED_CAMP_ID,
                ISOLATED_POI_ID,
                roomCategoryId,
                roomId,
                null,
                null,
                "ORDER-" + orderId,
                "OUT-ORDER-" + orderId,
                "daily_room",
                status,
                guestName,
                guestMobile,
                Timestamp.valueOf(startDate.atTime(LocalTime.of(14, 0))),
                Timestamp.valueOf(endDate.atTime(LocalTime.of(12, 0))),
                Math.max(1, (int) (endDate.toEpochDay() - startDate.toEpochDay())),
                30000L,
                0L,
                30000L,
                0L,
                0L,
                0L,
                0L,
                0L,
                30000L,
                paymentStatus,
                17101L,
                17202L,
                "channel",
                remark,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                CURRENT_USER_ID,
                CURRENT_USER_ID
        );
    }
}
