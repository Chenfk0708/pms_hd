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

import static org.hamcrest.Matchers.hasItem;
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
class CleanTaskStatisticsIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void cleanTaskStatistics_shouldAggregateRealCleanTasksAndSupportFilters() throws Exception {
        seedCleanStatisticsData();

        mockMvc.perform(post("/cleanTask/statistics")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "storeId":"11001",
                                  "roomIds":["23002"],
                                  "cleanerIds":["129101"],
                                  "cleanStartTime":1778947200000,
                                  "cleanEndTime":1779033599999,
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.list.length()").value(2))
                .andExpect(jsonPath("$.data.list[0].cleanTime").value("合计"))
                .andExpect(jsonPath("$.data.list[0].countNum").value(1))
                .andExpect(jsonPath("$.data.list[0].countCost").value(6600))
                .andExpect(jsonPath("$.data.list[0].cleanTypeThreeNum").value(1))
                .andExpect(jsonPath("$.data.list[0].cleanTypeThreeCost").value(6600))
                .andExpect(jsonPath("$.data.list[1].cleanTime").value("2026-05-17"))
                .andExpect(jsonPath("$.data.list[1].countNum").value(1))
                .andExpect(jsonPath("$.data.list[1].cleanTypeThreeNum").value(1));
    }

    @Test
    @Timeout(60)
    void cleanTaskStatistics_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        seedCleanStatisticsData();

        mockMvc.perform(post("/cleaner/list/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[*].cleanerId", hasItem("129101")))
                .andExpect(jsonPath("$.data[*].cleanerName", hasItem("Statistics Cleaner A")));

        mockMvc.perform(post("/cleanTask/statistics")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
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
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").value("无权访问当前门店保洁统计"));
    }


    @Test
    @Timeout(60)
    void cleanStatisticsDashboard_shouldReturnFrontendContractFromRealCleanTables() throws Exception {
        seedCleanStatisticsData();

        mockMvc.perform(post("/clean/statistics/dashboard")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "storeId":"11001",
                                  "roomIds":["23002"],
                                  "cleanerIds":["129101"],
                                  "cleanStartTime":1778947200000,
                                  "cleanEndTime":1779033599999,
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.statistics.pagination.total").value(2))
                .andExpect(jsonPath("$.data.statistics.list[0].cleanTime").value("合计"))
                .andExpect(jsonPath("$.data.statistics.list[0].countNum").value(1))
                .andExpect(jsonPath("$.data.statistics.list[0].cleanTypeThreeCost").value(6600))
                .andExpect(jsonPath("$.data.statistics.detailList.length()").value(1))
                .andExpect(jsonPath("$.data.statistics.detailList[0].id").value("129201"))
                .andExpect(jsonPath("$.data.statistics.detailList[0].roomName").value("标准大床房 / TDD-CLEAN-102"))
                .andExpect(jsonPath("$.data.statistics.detailList[0].cleanType").value("退房保洁"))
                .andExpect(jsonPath("$.data.statistics.detailList[0].fee").value(6600))
                .andExpect(jsonPath("$.data.statistics.metrics[0].id").value("month-count"))
                .andExpect(jsonPath("$.data.statistics.metrics[0].value").value("1"))
                .andExpect(jsonPath("$.data.statistics.todos[0].id").value("today-checkout"))
                .andExpect(jsonPath("$.data.stores[*].id", hasItem("11001")))
                .andExpect(jsonPath("$.data.rooms[*].id", hasItem("23002")))
                .andExpect(jsonPath("$.data.cleaners[*].id", hasItem("129101")));
    }

    @Test
    @Timeout(60)
    void cleanStatisticsExport_shouldCreateExportPayloadFromRealCleanTables() throws Exception {
        seedCleanStatisticsData();

        mockMvc.perform(post("/clean/statistics/export")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "storeId":"11001",
                                  "roomIds":["23002"],
                                  "cleanerIds":["129101"],
                                  "cleanStartTime":1778947200000,
                                  "cleanEndTime":1779033599999,
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.taskId").value("CLEAN-STAT-EXPORT-20260517"))
                .andExpect(jsonPath("$.data.fileName").value("clean_statistics_20260517.csv"))
                .andExpect(jsonPath("$.data.contentType").value("text/csv"))
                .andExpect(jsonPath("$.data.downloadUrl").value("/api/clean/statistics/export/download?campId=10001&storeId=11001&roomIds=23002&cleanerIds=129101&cleanStartTime=1778947200000&cleanEndTime=1779033599999"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.rows[0].id").value("129201"))
                .andExpect(jsonPath("$.data.rows[0].cleanType").value("退房保洁"));
    }


    @Test
    @Timeout(60)
    void cleanStatisticsExportDownload_shouldReturnCsvFromRealCleanTables() throws Exception {
        seedCleanStatisticsData();

        mockMvc.perform(get("/clean/statistics/export/download")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .param("campId", "10001")
                        .param("storeId", "11001")
                        .param("roomIds", "23002")
                        .param("cleanerIds", "129101")
                        .param("cleanStartTime", "1778947200000")
                        .param("cleanEndTime", "1779033599999"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=clean_statistics_20260517.csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id,cleanDate,roomName,cleanerName,cleanType,fee,status")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("129201,2026-05-17,\u6807\u51c6\u5927\u5e8a\u623f / TDD-CLEAN-102,Statistics Cleaner A,\u9000\u623f\u4fdd\u6d01,6600,\u5df2\u5b8c\u6210")));
    }

    private void seedCleanStatisticsData() {
        resetCleanStatisticsData();
        RoomCleanTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        insertCleanStaff(129101L, "Statistics Cleaner A", "13800002901");
        insertCleanStaff(129102L, "Statistics Cleaner B", "13800002902");
        insertCleanTask(129201L, 11001L, 23002L, 22001L, 129101L, "checkout_clean", "done", "102 checkout clean done", "2026-05-17 14:00:00");
        insertCleanTask(129202L, 11001L, 23005L, 22002L, 129102L, "daily_clean", "done", "203 stay clean done", "2026-05-17 16:30:00");
        insertCleanTask(129203L, 11001L, 23003L, 22001L, null, "deep_clean", "pending", "201 deep clean pending", "2026-05-18 09:00:00");
    }

    private void resetCleanStatisticsData() {
        jdbcTemplate.update("DELETE FROM clean_task WHERE camp_id = ?", CAMP_ID);
        jdbcTemplate.update("DELETE FROM clean_staff WHERE camp_id = ?", CAMP_ID);
    }

    private void insertCleanStaff(long cleanStaffId, String name, String mobile) {
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
                cleanStaffId,
                CAMP_ID,
                name,
                mobile,
                1,
                "clean-task-statistics-it",
                0
        );
    }

    private void insertCleanTask(
            long cleanTaskId,
            long poiId,
            long roomId,
            long roomCategoryId,
            Long cleanStaffId,
            String taskType,
            String taskStatus,
            String remark,
            String deadlineAt
    ) {
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
                CAMP_ID,
                poiId,
                roomId,
                roomCategoryId,
                cleanStaffId,
                taskType,
                taskStatus,
                deadlineAt,
                0,
                remark,
                deadlineAt,
                deadlineAt
        );
    }
}
