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
import java.time.YearMonth;
import java.time.ZoneId;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ComprehensiveMonthlyReportQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CURRENT_USER_ID = 12001L;
    private static final long ORIGINAL_CAMP_ID = 10001L;
    private static final long ISOLATED_CAMP_ID = 19001L;
    private static final long ISOLATED_POI_ID = 197121L;
    private static final long ROOM_CATEGORY_ID = 197221L;
    private static final long ROOM_ID_1 = 197321L;
    private static final long ROOM_ID_2 = 197322L;
    private static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void reportMonthlyPageGet_shouldReturnMonthlyAggregatesUsingFrontendShape() throws Exception {
        seedMonthlyReportScene();

        mockMvc.perform(post("/report/monthly/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19001",
                                  "startDate":"2026-03-01",
                                  "endDate":"2026-04-30",
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].date").value("2026年4月"))
                .andExpect(jsonPath("$.data.list[0].startDate").value(monthStartEpochMillis(2026, 4)))
                .andExpect(jsonPath("$.data.list[0].endDate").value(monthEndEpochMillis(2026, 4)))
                .andExpect(jsonPath("$.data.list[0].includeCommissionRoomPrice").value(closeTo(825.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].orderOtherExpense").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].writeDownIncome").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].businessIncome").value(closeTo(675.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].occ").value(closeTo(8.33, 0.001)))
                .andExpect(jsonPath("$.data.list[0].adr").value(closeTo(135.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].revPar").value(closeTo(11.25, 0.001)))
                .andExpect(jsonPath("$.data.list[0].inventory").value(60))
                .andExpect(jsonPath("$.data.list[0].openRoomCount").value(5))
                .andExpect(jsonPath("$.data.list[0].createTime").value(epochMillis(LocalDateTime.of(2026, 5, 1, 9, 41, 53))))
                .andExpect(jsonPath("$.data.list[0].userName").isString())
                .andExpect(jsonPath("$.data.list[1].date").value("2026年3月"))
                .andExpect(jsonPath("$.data.list[1].startDate").value(monthStartEpochMillis(2026, 3)))
                .andExpect(jsonPath("$.data.list[1].endDate").value(monthEndEpochMillis(2026, 3)))
                .andExpect(jsonPath("$.data.list[1].includeCommissionRoomPrice").value(closeTo(220.00, 0.001)))
                .andExpect(jsonPath("$.data.list[1].businessIncome").value(closeTo(180.00, 0.001)))
                .andExpect(jsonPath("$.data.list[1].occ").value(closeTo(3.23, 0.001)))
                .andExpect(jsonPath("$.data.list[1].adr").value(closeTo(90.00, 0.001)))
                .andExpect(jsonPath("$.data.list[1].revPar").value(closeTo(2.90, 0.001)))
                .andExpect(jsonPath("$.data.list[1].inventory").value(62))
                .andExpect(jsonPath("$.data.list[1].openRoomCount").value(2));
    }

    @Test
    @Timeout(60)
    void reportMonthlyPageGet_shouldSupportPageAliasForPagination() throws Exception {
        seedMonthlyReportScene();

        mockMvc.perform(post("/report/monthly/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19001",
                                  "startDate":"2026-03-01",
                                  "endDate":"2026-04-30",
                                  "page":2,
                                  "pageSize":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.current").value(2))
                .andExpect(jsonPath("$.data.pageNum").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].date").value("2026年3月"));
    }

    @Test
    @Timeout(60)
    void reportMonthlyPageGet_shouldFallbackInvalidCampIdAndRejectForeignCamp() throws Exception {
        seedMonthlyReportScene();

        mockMvc.perform(post("/report/monthly/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"mock-camp-main",
                                  "startDate":"2026-03-01",
                                  "endDate":"2026-04-30",
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(2));

        mockMvc.perform(post("/report/monthly/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "startDate":"2026-03-01",
                                  "endDate":"2026-04-30",
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    private void seedMonthlyReportScene() {
        insertCamp();
        rebindCurrentUserCamp();
        insertPoi();
        insertRoomCategory();
        insertRoom(ROOM_ID_1, "A-101", 1);
        insertRoom(ROOM_ID_2, "A-102", 2);

        insertOrderMain(197621L, ROOM_ID_1, LocalDate.of(2026, 3, 15), LocalDate.of(2026, 3, 17),
                LocalDateTime.of(2026, 4, 1, 10, 0, 0), 20000, 2000, 18000);
        insertOrderMain(197622L, ROOM_ID_1, LocalDate.of(2026, 4, 3), LocalDate.of(2026, 4, 5),
                LocalDateTime.of(2026, 5, 1, 8, 15, 0), 30000, 3000, 27000);
        insertOrderMain(197623L, ROOM_ID_2, LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 13),
                LocalDateTime.of(2026, 5, 1, 9, 41, 53), 45000, 4500, 40500);
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
                "TDD综合月报门店",
                1,
                "深圳",
                "南山区测试路 1 号",
                "0755-1000001",
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
                "TDD综合月报门店",
                1,
                1,
                1,
                0
        );
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
                ISOLATED_CAMP_ID,
                ISOLATED_POI_ID,
                "TDD综合月报房型",
                "TDD综合月报房型",
                2,
                2,
                16800L,
                18800L,
                20800L,
                14,
                23,
                12,
                "高区景观",
                "近地铁",
                "综合月报测试房型",
                1,
                1,
                0
        );
    }

    private void insertRoom(long roomId, String roomName, int sortNo) {
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
                ROOM_CATEGORY_ID,
                roomName,
                "normal",
                "overnight",
                "clean",
                1,
                sortNo,
                0
        );
    }

    private void insertOrderMain(
            long orderId,
            long roomId,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createdAt,
            long totalPriceCent,
            long commissionPriceCent,
            long settlementAmountCent
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
                ROOM_CATEGORY_ID,
                roomId,
                null,
                null,
                "MONTHLY-" + orderId,
                "OUT-MONTHLY-" + orderId,
                "daily_room",
                "checked_in",
                "测试客人" + orderId,
                "139" + String.format("%08d", orderId % 100000000L),
                Timestamp.valueOf(startDate.atTime(LocalTime.of(14, 0))),
                Timestamp.valueOf(endDate.atTime(LocalTime.of(12, 0))),
                Math.max(1, (int) (endDate.toEpochDay() - startDate.toEpochDay())),
                totalPriceCent,
                0,
                settlementAmountCent,
                0,
                commissionPriceCent,
                0,
                0,
                commissionPriceCent,
                settlementAmountCent,
                "paid",
                17101L,
                17202L,
                "frontdesk",
                "综合月报测试订单",
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(10)),
                CURRENT_USER_ID,
                CURRENT_USER_ID
        );
    }

    private long monthStartEpochMillis(int year, int month) {
        return epochMillis(LocalDate.of(year, month, 1).atStartOfDay());
    }

    private long monthEndEpochMillis(int year, int month) {
        return epochMillis(YearMonth.of(year, month).atEndOfMonth().atStartOfDay());
    }

    private long epochMillis(LocalDateTime value) {
        return value.atZone(SHANGHAI_ZONE).toInstant().toEpochMilli();
    }
}
