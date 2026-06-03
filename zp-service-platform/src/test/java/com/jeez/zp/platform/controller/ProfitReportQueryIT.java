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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProfitReportQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CURRENT_USER_ID = 12001L;
    private static final long ORIGINAL_CAMP_ID = 10001L;
    private static final long ISOLATED_CAMP_ID = 19801L;
    private static final long ISOLATED_POI_ID = 19821L;
    private static final long ROOM_CATEGORY_ID = 19831L;
    private static final long ROOM_ID_1 = 19841L;
    private static final long ROOM_ID_2 = 19842L;
    private static final long CLEAN_STAFF_ID = 19851L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void reportProfitGetV2_shouldReturnPagedFrontendShape() throws Exception {
        seedProfitScene();

        mockMvc.perform(post("/report/profit/get/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19801",
                                  "startDate":"2026-05-10",
                                  "endDate":"2026-05-12",
                                  "pageNum":1,
                                  "pageSize":2,
                                  "isCleanCost":0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.total").value(4))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.pages").value(2))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].date").value("合计"))
                .andExpect(jsonPath("$.data.list[0].isTotal").value(1))
                .andExpect(jsonPath("$.data.list[0].roomFeeMinusCommission").value(closeTo(540.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].ticketPrice").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].cateringPrice").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].otherOrderExpense").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].writeDownIncome").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].totalIncome").value(closeTo(540.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].writeDownExpenses").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].cleanCost").value(closeTo(0.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].profitPrice").value(closeTo(540.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].profitRate").value("100.00%"))
                .andExpect(jsonPath("$.data.list[1].date").value("2026-05-10"))
                .andExpect(jsonPath("$.data.list[1].isTotal").value(0))
                .andExpect(jsonPath("$.data.list[1].roomFeeMinusCommission").value(closeTo(180.00, 0.001)))
                .andExpect(jsonPath("$.data.list[1].totalIncome").value(closeTo(180.00, 0.001)))
                .andExpect(jsonPath("$.data.list[1].profitPrice").value(closeTo(180.00, 0.001)))
                .andExpect(jsonPath("$.data.list[1].profitRate").value("100.00%"));
    }

    @Test
    @Timeout(60)
    void reportProfitGetV2_shouldApplyCleanCostWhenRequested() throws Exception {
        seedProfitScene();

        mockMvc.perform(post("/report/profit/get/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19801",
                                  "startDate":"2026-05-10",
                                  "endDate":"2026-05-12",
                                  "pageNum":1,
                                  "pageSize":4,
                                  "isCleanCost":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(4))
                .andExpect(jsonPath("$.data.list.length()").value(4))
                .andExpect(jsonPath("$.data.list[0].date").value("合计"))
                .andExpect(jsonPath("$.data.list[0].cleanCost").value(closeTo(127.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].profitPrice").value(closeTo(413.00, 0.001)))
                .andExpect(jsonPath("$.data.list[0].profitRate").value("76.48%"))
                .andExpect(jsonPath("$.data.list[2].date").value("2026-05-11"))
                .andExpect(jsonPath("$.data.list[2].cleanCost").value(closeTo(66.00, 0.001)))
                .andExpect(jsonPath("$.data.list[2].profitPrice").value(closeTo(204.00, 0.001)))
                .andExpect(jsonPath("$.data.list[2].profitRate").value("75.56%"))
                .andExpect(jsonPath("$.data.list[3].date").value("2026-05-12"))
                .andExpect(jsonPath("$.data.list[3].cleanCost").value(closeTo(61.00, 0.001)))
                .andExpect(jsonPath("$.data.list[3].profitPrice").value(closeTo(29.00, 0.001)))
                .andExpect(jsonPath("$.data.list[3].profitRate").value("32.22%"));
    }

    @Test
    @Timeout(60)
    void statisticsProfitReportExport_shouldReturnCsvExportTaskFromRealProfitRows() throws Exception {
        seedProfitScene();

        mockMvc.perform(post("/statistics/profit-report/export")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"19801",
                                  "startDate":"2026-05-10",
                                  "endDate":"2026-05-12",
                                  "pageNum":1,
                                  "pageSize":9999,
                                  "current":1,
                                  "breakTemp":false,
                                  "isCleanCost":1,
                                  "exportExcelMenuId":"profit-report"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.data.taskId").value("PROFIT-REPORT-EXPORT-19801-20260510-20260512"))
                .andExpect(jsonPath("$.data.fileName").value("profit_report_20260510_20260512.csv"))
                .andExpect(jsonPath("$.data.contentType").value("text/csv"))
                .andExpect(jsonPath("$.data.downloadUrl").value("/api/statistics/profit-report/export/download?campId=19801&startDate=2026-05-10&endDate=2026-05-12&isCleanCost=1"))
                .andExpect(jsonPath("$.data.total").value(4))
                .andExpect(jsonPath("$.data.rows.length()").value(4))
                .andExpect(jsonPath("$.data.rows[0].date").value("\u5408\u8ba1"))
                .andExpect(jsonPath("$.data.rows[0].isTotal").value(1))
                .andExpect(jsonPath("$.data.rows[0].cleanCost").value(closeTo(127.00, 0.001)))
                .andExpect(jsonPath("$.data.rows[0].profitPrice").value(closeTo(413.00, 0.001)))
                .andExpect(jsonPath("$.data.rows[0].profitRate").value("76.48%"));
    }


    @Test
    @Timeout(60)
    void statisticsProfitReportExportDownload_shouldReturnCsvFromRealProfitRows() throws Exception {
        seedProfitScene();

        mockMvc.perform(get("/statistics/profit-report/export/download")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .param("campId", "19801")
                        .param("startDate", "2026-05-10")
                        .param("endDate", "2026-05-12")
                        .param("isCleanCost", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=profit_report_20260510_20260512.csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("date,roomFeeMinusCommission,totalIncome,cleanCost,profitPrice,profitRate")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\u5408\u8ba1,540.00,540.00,127.00,413.00,76.48%")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2026-05-12,90.00,90.00,61.00,29.00,32.22%")));
    }

    @Test
    @Timeout(60)
    void reportProfitGetV2_shouldFallbackInvalidCampIdAndRejectForeignCamp() throws Exception {
        seedProfitScene();

        mockMvc.perform(post("/report/profit/get/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"mock-camp-profit",
                                  "startDate":"2026-05-10",
                                  "endDate":"2026-05-12",
                                  "pageNum":1,
                                  "pageSize":4,
                                  "isCleanCost":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list[0].profitPrice").value(closeTo(413.00, 0.001)));

        mockMvc.perform(post("/report/profit/get/v2")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(CURRENT_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "startDate":"2026-05-10",
                                  "endDate":"2026-05-12",
                                  "pageNum":1,
                                  "pageSize":4,
                                  "isCleanCost":1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    private void seedProfitScene() {
        insertCamp();
        rebindCurrentUserCamp();
        insertPoi();
        insertRoomCategory();
        insertRoom(ROOM_ID_1, "P-101", 1);
        insertRoom(ROOM_ID_2, "P-102", 2);
        insertCleanStaff();

        insertOrderMain(19861L, ROOM_ID_1, LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 12),
                40000L, 40000L, 4000L, 36000L, LocalDateTime.of(2026, 5, 9, 10, 0));
        insertOrderMain(19862L, ROOM_ID_2, LocalDate.of(2026, 5, 11), LocalDate.of(2026, 5, 13),
                20000L, 20000L, 2000L, 18000L, LocalDateTime.of(2026, 5, 10, 11, 0));

        insertCleanTask(19881L, ROOM_ID_1, "checkout_clean", "DONE", LocalDateTime.of(2026, 5, 11, 9, 0));
        insertCleanTask(19882L, ROOM_ID_2, "daily_clean", "DONE", LocalDateTime.of(2026, 5, 12, 10, 0));
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
                "TDD利润门店",
                1,
                "深圳",
                "南山区利润路 18 号",
                "0755-1980101",
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
                "TDD利润门店",
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
                "TDD利润房型",
                "TDD利润房型",
                2,
                2,
                26800L,
                28800L,
                30800L,
                14,
                23,
                12,
                "利润房型亮点",
                "利润房型周边",
                "利润房型图文",
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

    private void insertCleanStaff() {
        jdbcTemplate.update("""
                        INSERT INTO clean_staff (
                            clean_staff_id,
                            camp_id,
                            name,
                            mobile,
                            status,
                            remark,
                            is_deleted
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                CLEAN_STAFF_ID,
                ISOLATED_CAMP_ID,
                "保洁阿姨",
                "13800000001",
                1,
                "profit-report-test",
                0
        );
    }

    private void insertCleanTask(long cleanTaskId, long roomId, String taskType, String taskStatus, LocalDateTime deadlineAt) {
        jdbcTemplate.update("""
                        INSERT INTO clean_task (
                            clean_task_id,
                            camp_id,
                            poi_id,
                            room_id,
                            room_category_id,
                            clean_staff_id,
                            task_type,
                            task_status,
                            deadline_at,
                            notify_count,
                            remark,
                            created_at,
                            updated_at
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                cleanTaskId,
                ISOLATED_CAMP_ID,
                ISOLATED_POI_ID,
                roomId,
                ROOM_CATEGORY_ID,
                CLEAN_STAFF_ID,
                taskType,
                taskStatus,
                Timestamp.valueOf(deadlineAt),
                0,
                "profit clean task",
                Timestamp.valueOf(deadlineAt.minusHours(1)),
                Timestamp.valueOf(deadlineAt)
        );
    }

    private void insertOrderMain(
            long orderId,
            long roomId,
            LocalDate startDate,
            LocalDate endDate,
            long totalPriceCent,
            long totalPayPriceCent,
            long distributionCommissionCent,
            long settlementAmountCent,
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
                ISOLATED_CAMP_ID,
                ISOLATED_POI_ID,
                ROOM_CATEGORY_ID,
                roomId,
                null,
                null,
                "ORDER-" + orderId,
                "OUT-ORDER-" + orderId,
                "daily_room",
                "checked_in",
                "利润客人" + orderId,
                "1390000" + orderId,
                Timestamp.valueOf(startDate.atTime(LocalTime.of(14, 0))),
                Timestamp.valueOf(endDate.atTime(LocalTime.of(12, 0))),
                Math.max(1, (int) (endDate.toEpochDay() - startDate.toEpochDay())),
                totalPriceCent,
                0L,
                totalPayPriceCent,
                0L,
                distributionCommissionCent,
                0L,
                0L,
                distributionCommissionCent,
                settlementAmountCent,
                "paid",
                17101L,
                17202L,
                "channel",
                "profit report test",
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(createdAt.plusMinutes(30)),
                CURRENT_USER_ID,
                CURRENT_USER_ID
        );
    }
}
