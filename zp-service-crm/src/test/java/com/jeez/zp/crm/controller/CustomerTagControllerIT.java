package com.jeez.zp.crm.controller;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CustomerTagControllerIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void memberTagGroupPageGet_shouldReturnTagGroupsWithRealAggregatesAndKeywordFilter() throws Exception {
        seedCustomerTags();

        mockMvc.perform(post("/memberTagGroup/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "tagGroupName":"VIP",
                                  "pageNum":1,
                                  "pageSize":10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.pagination.page").value(1))
                .andExpect(jsonPath("$.data.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.data.pagination.total").value(1))
                .andExpect(jsonPath("$.data.summary.groupCount").value(1))
                .andExpect(jsonPath("$.data.summary.tagCount").value(2))
                .andExpect(jsonPath("$.data.summary.coveredMembers").value(2))
                .andExpect(jsonPath("$.data.summary.syncingGroups").value(0))
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].tagGroupId").value("42101"))
                .andExpect(jsonPath("$.data.list[0].tagGroupName").value("CRM VIP Tags"))
                .andExpect(jsonPath("$.data.list[0].tagNames", hasItem("High Value")))
                .andExpect(jsonPath("$.data.list[0].tagNames", hasItem("Birthday")))
                .andExpect(jsonPath("$.data.list[0].memberCount").value(2))
                .andExpect(jsonPath("$.data.list[0].recentlyAddedCount").value(1))
                .andExpect(jsonPath("$.data.list[0].source").value("manual"))
                .andExpect(jsonPath("$.data.list[0].status").value("enabled"));
    }

    @Test
    @Timeout(60)
    void memberTagGroupSave_shouldUpsertGroupAndTagsAndRejectForeignCampAccess() throws Exception {
        resetCustomerTags();

        mockMvc.perform(post("/memberTagGroup/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "tagGroupId":"42103",
                                  "tagGroupName":"CRM Saved Tags",
                                  "tagNames":["Saved A","Saved B"],
                                  "source":"manual"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tagGroupId").value("42103"))
                .andExpect(jsonPath("$.data.tagNames.length()").value(2))
                .andExpect(jsonPath("$.data.message").value("customer tag group saved"));

        Integer tagCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(1)
                        FROM crm_customer_tag t
                        JOIN crm_customer_tag_group g ON g.tag_group_id = t.tag_group_id
                        WHERE g.camp_id = ?
                          AND g.tag_group_id = ?
                          AND t.tag_name IN ('Saved A', 'Saved B')
                        """,
                Integer.class,
                CAMP_ID,
                42103L
        );
        assertThat(tagCount).isEqualTo(2);

        mockMvc.perform(post("/memberTagGroup/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
                                  "tagGroupId":"42104",
                                  "tagGroupName":"Foreign Tags",
                                  "tagNames":["Blocked"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Timeout(60)
    void memberTagGroupExportAndWxCpOpenAccountsGet_shouldUseRealCrmAndMemberTables() throws Exception {
        seedCustomerTags();
        jdbcTemplate.update("""
                        UPDATE pms_member
                        SET wecom_user_id = ?, updated_at = NOW()
                        WHERE camp_id = ?
                          AND user_id = ?
                          AND is_deleted = 0
                        """,
                "wecom-12001",
                CAMP_ID,
                12001L
        );

        mockMvc.perform(post("/memberTagGroup/export")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "tagGroupName":"CRM",
                                  "pageNum":1,
                                  "pageSize":20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.taskId").value("MEMBER-TAG-GROUP-EXPORT-10001"))
                .andExpect(jsonPath("$.data.fileName").value("member_tag_groups_10001.csv"))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.rows.length()").value(2));

        mockMvc.perform(post("/wxCpOpen/accounts/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.accounts[0].accountId").exists())
                .andExpect(jsonPath("$.data.accounts[0].wecomUserId").value("wecom-12001"))
                .andExpect(jsonPath("$.data.accounts[0].status").value("authorized"));
    }

    private void seedCustomerTags() {
        resetCustomerTags();
        insertCustomer(42191L, "CRM Tag Customer A", "13942191001");
        insertCustomer(42192L, "CRM Tag Customer B", "13942192002");
        insertTagGroup(42101L, "CRM VIP Tags", "manual", 1);
        insertTag(42111L, 42101L, "High Value", 1);
        insertTag(42112L, 42101L, "Birthday", 2);
        insertTagRel(42181L, 42191L, 42111L, "NOW()");
        insertTagRel(42182L, 42192L, 42112L, "DATE_SUB(NOW(), INTERVAL 10 DAY)");

        insertTagGroup(42102L, "CRM WeCom Tags", "wechat", 2);
        insertTag(42121L, 42102L, "WeCom Friend", 1);
    }

    private void resetCustomerTags() {
        jdbcTemplate.update("DELETE FROM crm_customer_tag_rel WHERE id BETWEEN 42180 AND 42199");
        jdbcTemplate.update("DELETE FROM crm_customer_tag WHERE tag_group_id BETWEEN 42101 AND 42104");
        jdbcTemplate.update("DELETE FROM crm_customer_tag_group WHERE tag_group_id BETWEEN 42101 AND 42104");
        jdbcTemplate.update("DELETE FROM crm_customer WHERE camp_id = ? AND customer_id BETWEEN 42191 AND 42199", CAMP_ID);
    }

    private void insertCustomer(long customerId, String name, String mobile) {
        jdbcTemplate.update("""
                        INSERT INTO crm_customer (
                            customer_id,
                            camp_id,
                            member_id,
                            name,
                            mobile,
                            source_channel_id,
                            profile_json,
                            last_active_at,
                            status,
                            created_at,
                            updated_at,
                            is_deleted
                        ) VALUES (?, ?, NULL, ?, ?, NULL, CAST(? AS JSON), NOW(), 1, NOW(), NOW(), 0)
                        """,
                customerId,
                CAMP_ID,
                name,
                mobile,
                "{\"source\":\"tag-it\"}"
        );
    }

    private void insertTagGroup(long tagGroupId, String name, String sourceType, int sortNo) {
        jdbcTemplate.update("""
                        INSERT INTO crm_customer_tag_group (
                            tag_group_id,
                            camp_id,
                            name,
                            source_type,
                            sort_no,
                            status
                        ) VALUES (?, ?, ?, ?, ?, 1)
                        """,
                tagGroupId,
                CAMP_ID,
                name,
                sourceType,
                sortNo
        );
    }

    private void insertTag(long tagId, long tagGroupId, String tagName, int sortNo) {
        jdbcTemplate.update("""
                        INSERT INTO crm_customer_tag (
                            tag_id,
                            tag_group_id,
                            tag_name,
                            sort_no,
                            status
                        ) VALUES (?, ?, ?, ?, 1)
                        """,
                tagId,
                tagGroupId,
                tagName,
                sortNo
        );
    }

    private void insertTagRel(long id, long customerId, long tagId, String createdAtExpression) {
        jdbcTemplate.update("""
                        INSERT INTO crm_customer_tag_rel (
                            id,
                            customer_id,
                            tag_id,
                            created_at
                        ) VALUES (?, ?, ?, %s)
                        """.formatted(createdAtExpression),
                id,
                customerId,
                tagId
        );
    }
}
