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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CleanSettingActionIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void cleanSettingOverview_shouldReturnDashboardFromCleanManagePath() throws Exception {
        seedCleanSettings();
        seedCleanTasks();

        mockMvc.perform(post("/cleanManage/cleanSetting/overview")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "businessDate":"2026-05-22",
                                  "storeId":"all",
                                  "projectId":"all",
                                  "status":"all",
                                  "page":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.metrics[0].value").value("2"))
                .andExpect(jsonPath("$.data.policyRules[*].id", hasItem("policy-overview-it")))
                .andExpect(jsonPath("$.data.priceRules[*].id", hasItem("price-overview-it")))
                .andExpect(jsonPath("$.data.schedule[0].value").value("1 \u95F4"));
    }

    @Test
    @Timeout(60)
    void cleanSettingRuleSave_shouldUpsertPolicyRuleJson() throws Exception {
        resetCleanSettings();

        mockMvc.perform(post("/cleanManage/cleanSetting/rule/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "rule":{
                                    "id":"policy-save-it",
                                    "name":"Saved clean policy",
                                    "storeId":"11001",
                                    "storeName":"IT Store",
                                    "projectId":"daily-clean",
                                    "roomScope":"Action rooms",
                                    "trigger":"After checkout",
                                    "cleanerGroup":"Action Group",
                                    "status":"enabled",
                                    "updatedAt":"2026-05-22 10:30",
                                    "detail":"Saved from clean setting action IT"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.rule.id").value("policy-save-it"))
                .andExpect(jsonPath("$.data.rule.name").value("Saved clean policy"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.message").value("\u4FDD\u6D01\u7B56\u7565\u4FDD\u5B58\u6210\u529F"));

        Integer rowCount = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                        FROM clean_setting
                        WHERE camp_id = ?
                          AND config_key = 'policy_rules'
                          AND JSON_SEARCH(config_value, 'one', 'policy-save-it') IS NOT NULL
                          AND JSON_SEARCH(config_value, 'one', 'Saved clean policy') IS NOT NULL
                        """,
                Integer.class,
                CAMP_ID
        );
        assertEquals(1, rowCount);
    }

    @Test
    @Timeout(60)
    void cleanSettingExport_shouldReturnFilteredRuleAndPriceRows() throws Exception {
        seedCleanSettings();

        mockMvc.perform(post("/cleanManage/cleanSetting/export")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "businessDate":"2026-05-22",
                                  "storeId":"11001",
                                  "projectId":"daily-clean",
                                  "status":"enabled",
                                  "page":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.fileName").value("clean_setting_2026-05-22.csv"))
                .andExpect(jsonPath("$.data.contentType").value("text/csv"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.policyRules.length()").value(1))
                .andExpect(jsonPath("$.data.policyRules[0].id").value("policy-overview-it"))
                .andExpect(jsonPath("$.data.priceRules.length()").value(1))
                .andExpect(jsonPath("$.data.priceRules[0].id").value("price-overview-it"));
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
                131401L,
                CAMP_ID,
                "policy_rules",
                """
                        [
                          {
                            "id":"policy-overview-it",
                            "name":"Overview clean policy",
                            "storeId":"11001",
                            "storeName":"IT Store",
                            "projectId":"daily-clean",
                            "roomScope":"All rooms",
                            "trigger":"After checkout",
                            "cleanerGroup":"Group A",
                            "status":"enabled",
                            "updatedAt":"2026-05-22 09:00",
                            "detail":"Overview policy"
                          },
                          {
                            "id":"policy-paused-it",
                            "name":"Paused clean policy",
                            "storeId":"11001",
                            "storeName":"IT Store",
                            "projectId":"deep-clean",
                            "roomScope":"Long stay rooms",
                            "trigger":"Every Monday",
                            "cleanerGroup":"Group B",
                            "status":"paused",
                            "updatedAt":"2026-05-21 09:00",
                            "detail":"Paused policy"
                          }
                        ]
                        """,
                12001L,
                "2026-05-22 09:00:00"
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
                131402L,
                CAMP_ID,
                "price_rules",
                """
                        [
                          {
                            "id":"price-overview-it",
                            "name":"Overview price",
                            "projectId":"daily-clean",
                            "cleanType":"Daily clean",
                            "amount":"18.00",
                            "settlementMode":"Per room",
                            "status":"enabled"
                          },
                          {
                            "id":"price-paused-it",
                            "name":"Paused price",
                            "projectId":"deep-clean",
                            "cleanType":"Deep clean",
                            "amount":"68.00",
                            "settlementMode":"Per task",
                            "status":"paused"
                          }
                        ]
                        """,
                12001L,
                "2026-05-22 09:00:00"
        );
    }

    private void seedCleanTasks() {
        resetCleanTasks();
        RoomCleanTestCatalogFixture.ensureBaseCatalog(jdbcTemplate);
        insertCleanTask(131501L, "pending", "2026-05-22 10:00:00");
        insertCleanTask(131502L, "processing", "2026-05-22 14:30:00");
        insertCleanTask(131503L, "done", "2026-05-22 17:30:00");
    }

    private void resetCleanSettings() {
        jdbcTemplate.update("DELETE FROM clean_setting WHERE camp_id = ? AND config_key IN ('policy_rules', 'price_rules')", CAMP_ID);
    }

    private void resetCleanTasks() {
        jdbcTemplate.update("DELETE FROM clean_task WHERE camp_id = ? AND clean_task_id IN (131501, 131502, 131503)", CAMP_ID);
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
                "clean setting action integration test",
                deadlineAt,
                deadlineAt
        );
    }
}
