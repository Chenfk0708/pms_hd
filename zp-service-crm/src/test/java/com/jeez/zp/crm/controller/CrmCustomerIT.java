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

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CrmCustomerIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void crmServicePing_shouldNotRequireGatewayAuth() throws Exception {
        mockMvc.perform(get("/crm-service/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("zp-service-crm"))
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    @Timeout(60)
    void customersPageAndDetail_shouldReadRealCustomers() throws Exception {
        seedCustomers();

        mockMvc.perform(post("/customers/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "pageNum":1,
                                  "pageSize":10,
                                  "keyword":"CRM Alpha"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].customerId").value("41001"))
                .andExpect(jsonPath("$.data.list[0].name").value("CRM Alpha"))
                .andExpect(jsonPath("$.data.list[0].mobile").value("13941001001"))
                .andExpect(jsonPath("$.data.list[0].status").value(1));

        mockMvc.perform(post("/customers/detail/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "customerId":"41001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.customerId").value("41001"))
                .andExpect(jsonPath("$.data.name").value("CRM Alpha"))
                .andExpect(jsonPath("$.data.mobile").value("13941001001"))
                .andExpect(jsonPath("$.data.profileJson").exists());
    }

    @Test
    @Timeout(60)
    void customersSave_shouldUpsertCustomerAndRejectForeignCamp() throws Exception {
        resetCustomers();

        mockMvc.perform(post("/customers/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId":"41003",
                                  "campId":"10001",
                                  "name":"CRM Saved",
                                  "mobile":"13941003003",
                                  "profileJson":"{\\"level\\":\\"gold\\"}"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.customerId").value("41003"))
                .andExpect(jsonPath("$.data.message").value("客户保存成功"));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM crm_customer WHERE camp_id = ? AND customer_id = ? AND name = ?",
                Integer.class,
                CAMP_ID,
                41003L,
                "CRM Saved"
        );
        assertThat(count).isEqualTo(1);

        mockMvc.perform(post("/customers/page/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10002",
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
    void customersSave_shouldRejectInvalidNameAndMobileBeforeInsert() throws Exception {
        resetCustomers();

        mockMvc.perform(post("/customers/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId":"41003",
                                  "campId":"10001",
                                  "name":"客户1",
                                  "mobile":"13941003003",
                                  "profileJson":"{\\"level\\":\\"gold\\"}"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("姓名格式不正确，请输入 2-30 个中文或英文字母"));

        mockMvc.perform(post("/customers/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId":"41004",
                                  "campId":"10001",
                                  "name":"客户测试",
                                  "mobile":"12000000000",
                                  "profileJson":"{\\"level\\":\\"gold\\"}"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("手机号格式不正确"));

        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM crm_customer
                WHERE camp_id = ?
                  AND customer_id IN (41003, 41004)
                """, Integer.class, CAMP_ID)).isZero();
    }

    private void seedCustomers() {
        resetCustomers();
        insertCustomer(41001L, "CRM Alpha", "13941001001");
        insertCustomer(41002L, "CRM Beta", "13941002002");
    }

    private void resetCustomers() {
        jdbcTemplate.update("DELETE FROM crm_customer_tag_rel WHERE customer_id BETWEEN 41001 AND 41004");
        jdbcTemplate.update("DELETE FROM crm_customer WHERE camp_id = ? AND customer_id BETWEEN 41001 AND 41004", CAMP_ID);
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
                        ) VALUES (?, ?, NULL, ?, ?, NULL, CAST(? AS JSON), ?, 1, ?, ?, 0)
                        """,
                customerId,
                CAMP_ID,
                name,
                mobile,
                "{\"source\":\"it\"}",
                Timestamp.valueOf(LocalDateTime.now().minusDays(1)),
                Timestamp.valueOf(LocalDateTime.now().minusDays(2)),
                Timestamp.valueOf(LocalDateTime.now().minusDays(1))
        );
    }
}
