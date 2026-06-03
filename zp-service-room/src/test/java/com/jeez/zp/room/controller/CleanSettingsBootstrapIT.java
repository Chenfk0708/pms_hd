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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CleanSettingsBootstrapIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void cleanSettingsBootstrap_shouldReturnFrontendDashboardFromRealTables() throws Exception {
        seedCleanSettings();
        seedCleanTasks();

        mockMvc.perform(post("/cleanSettings/bootstrap")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "businessDate":"2026-05-18",
                                  "storeId":"all",
                                  "projectId":"all",
                                  "status":"all",
                                  "page":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stores[0].value").value("all"))
                .andExpect(jsonPath("$.data.stores[0].label").value("\u5168\u90E8\u95E8\u5E97"))
                .andExpect(jsonPath("$.data.stores[*].value", hasItem("11001")))
                .andExpect(jsonPath("$.data.projects[*].value", hasItem("daily-clean")))
                .andExpect(jsonPath("$.data.projects[*].value", hasItem("deep-clean")))
                .andExpect(jsonPath("$.data.statusOptions[*].value", hasItem("enabled")))
                .andExpect(jsonPath("$.data.statusOptions[*].value", hasItem("paused")))
                .andExpect(jsonPath("$.data.metrics[*].key", hasItem("todayTasks")))
                .andExpect(jsonPath("$.data.metrics[*].key", hasItem("enabledRules")))
                .andExpect(jsonPath("$.data.metrics[0].value").value("2"))
                .andExpect(jsonPath("$.data.metrics[1].value").value("1"))
                .andExpect(jsonPath("$.data.policyRules.length()").value(2))
                .andExpect(jsonPath("$.data.policyRules[*].id", hasItem("policy-auto-checkout-it")))
                .andExpect(jsonPath("$.data.policyRules[*].status", hasItem("paused")))
                .andExpect(jsonPath("$.data.priceRules.length()").value(2))
                .andExpect(jsonPath("$.data.priceRules[*].id", hasItem("price-daily-clean-it")))
                .andExpect(jsonPath("$.data.reminders").isArray())
                .andExpect(jsonPath("$.data.schedule").isArray())
                .andExpect(jsonPath("$.data.schedule[0].label").value("09:00-12:00"))
                .andExpect(jsonPath("$.data.schedule[0].value").value("1 \u95F4"))
                .andExpect(jsonPath("$.data.schedule[1].label").value("12:00-16:00"))
                .andExpect(jsonPath("$.data.schedule[1].value").value("1 \u95F4"))
                .andExpect(jsonPath("$.data.schedule[2].label").value("16:00-20:00"))
                .andExpect(jsonPath("$.data.schedule[2].value").value("0 \u95F4"))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(20))
                .andExpect(jsonPath("$.data.pagination.total").value(2))
                .andExpect(jsonPath("$.data.requestedAt").isNotEmpty());
    }

    @Test
    @Timeout(60)
    void cleanSettingsBootstrap_shouldFallbackCurrentCampAndRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/cleanSettings/bootstrap")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"",
                                  "businessDate":"2026-05-18",
                                  "storeId":"all",
                                  "projectId":"all",
                                  "status":"all",
                                  "page":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.stores[0].value").value("all"));

        mockMvc.perform(post("/cleanSettings/bootstrap")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "businessDate":"2026-05-18",
                                  "storeId":"all",
                                  "projectId":"all",
                                  "status":"all",
                                  "page":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    private void seedCleanSettings() {
        resetCleanSettings();
        jdbcTemplate.update("""
                        INSERT INTO clean_setting (
                            setting_id,
                            camp_id,
                            config_key,
                            config_value,
                            updated_by,
                            updated_at
                        ) VALUES (?, ?, ?, CAST(? AS JSON), ?, ?)
                        """,
                130401L,
                CAMP_ID,
                "policy_rules",
                """
                        [
                          {
                            "id":"policy-auto-checkout-it",
                            "name":"Auto checkout clean",
                            "storeId":"11001",
                            "storeName":"IT Store",
                            "projectId":"daily-clean",
                            "roomScope":"All rooms",
                            "trigger":"After checkout",
                            "cleanerGroup":"Group A",
                            "status":"enabled",
                            "updatedAt":"2026-05-18 09:00",
                            "detail":"Create task from checkout event"
                          },
                          {
                            "id":"policy-deep-clean-it",
                            "name":"Deep clean review",
                            "storeId":"11001",
                            "storeName":"IT Store",
                            "projectId":"deep-clean",
                            "roomScope":"Long stay rooms",
                            "trigger":"Every Monday",
                            "cleanerGroup":"Group B",
                            "status":"paused",
                            "updatedAt":"2026-05-17 18:00",
                            "detail":"Manual review before dispatch"
                          }
                        ]
                        """,
                12001L,
                "2026-05-18 09:00:00"
        );
        jdbcTemplate.update("""
                        INSERT INTO clean_setting (
                            setting_id,
                            camp_id,
                            config_key,
                            config_value,
                            updated_by,
                            updated_at
                        ) VALUES (?, ?, ?, CAST(? AS JSON), ?, ?)
                        """,
                130402L,
                CAMP_ID,
                "price_rules",
                """
                        [
                          {
                            "id":"price-daily-clean-it",
                            "name":"Daily clean price",
                            "projectId":"daily-clean",
                            "cleanType":"Daily clean",
                            "amount":"18.00",
                            "settlementMode":"Per room",
                            "status":"enabled"
                          },
                          {
                            "id":"price-deep-clean-it",
                            "name":"Deep clean price",
                            "projectId":"deep-clean",
                            "cleanType":"Deep clean",
                            "amount":"68.00",
                            "settlementMode":"Per task",
                            "status":"paused"
                          }
                        ]
                        """,
                12001L,
                "2026-05-18 09:00:00"
        );
    }

    private void seedCleanTasks() {
        resetCleanTasks();
        RoomCleanTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        insertCleanTask(130501L, "pending", "2026-05-18 10:00:00");
        insertCleanTask(130502L, "processing", "2026-05-18 14:30:00");
        insertCleanTask(130503L, "done", "2026-05-18 17:30:00");
    }

    private void resetCleanSettings() {
        jdbcTemplate.update("DELETE FROM clean_setting WHERE camp_id = ? AND setting_id IN (130401, 130402)", CAMP_ID);
    }

    private void resetCleanTasks() {
        jdbcTemplate.update("DELETE FROM clean_task WHERE camp_id = ? AND clean_task_id IN (130501, 130502, 130503)", CAMP_ID);
    }

    private void insertCleanTask(long cleanTaskId, String taskStatus, String deadlineAt) {
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
                11001L,
                23002L,
                22001L,
                null,
                "daily_clean",
                taskStatus,
                deadlineAt,
                0,
                "clean settings integration test",
                deadlineAt,
                deadlineAt
        );
    }
}
