package com.jeez.zp.platform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiKeysControllerIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String CAMP_ID = "10001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanApiKeys() {
        jdbcTemplate.update("DELETE FROM company_api_key WHERE camp_id = ?", Long.valueOf(CAMP_ID));
    }

    @Test
    @Timeout(60)
    void userSecretGet_shouldReturnEmptyWhenNoActiveKey() throws Exception {
        mockMvc.perform(post("/user/secret/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"%s"}
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.keyRecord").doesNotExist())
                .andExpect(jsonPath("$.data.activityLog").isArray());
    }

    @Test
    @Timeout(60)
    void userSecretGenerate_shouldCreateActiveApiKeyAndGetIt() throws Exception {
        JsonNode generated = generateKey();
        JsonNode keyRecord = generated.path("keyRecord");

        assertThat(keyRecord.path("appId").asText()).startsWith("locals-ai-");
        assertThat(keyRecord.path("accessKeyId").asText()).startsWith("ak_local_");
        assertThat(keyRecord.path("secretKeyPreview").asText()).contains("****************");
        assertThat(keyRecord.path("createdAt").asText()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");
        assertThat(keyRecord.path("status").asText()).isEqualTo("active");
        assertThat(keyRecord.path("scopes")).hasSizeGreaterThan(0);
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM company_api_key
                WHERE camp_id = 10001 AND access_key_id = ? AND status = 1 AND is_deleted = 0
                """, Integer.class, keyRecord.path("accessKeyId").asText())).isEqualTo(1);

        mockMvc.perform(post("/user/secret/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"%s"}
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.keyRecord.accessKeyId").value(keyRecord.path("accessKeyId").asText()))
                .andExpect(jsonPath("$.data.activityLog[0].id").exists());
    }

    @Test
    @Timeout(60)
    void userSecretGenerate_shouldRotatePreviousActiveKey() throws Exception {
        String firstAccessKeyId = generateKey().path("keyRecord").path("accessKeyId").asText();
        String secondAccessKeyId = generateKey().path("keyRecord").path("accessKeyId").asText();

        assertThat(secondAccessKeyId).isNotEqualTo(firstAccessKeyId);
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM company_api_key
                WHERE camp_id = 10001 AND status = 1 AND is_deleted = 0
                """, Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM company_api_key
                WHERE camp_id = 10001 AND access_key_id = ? AND is_deleted = 1
                """, Integer.class, firstAccessKeyId)).isEqualTo(1);
    }

    @Test
    @Timeout(60)
    void userSecretGet_shouldRejectOtherCamp() throws Exception {
        mockMvc.perform(post("/user/secret/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10002"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301));
    }

    private JsonNode generateKey() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/secret/generate")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"%s"}
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data");
    }
}
