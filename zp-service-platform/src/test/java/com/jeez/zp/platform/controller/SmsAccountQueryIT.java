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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SmsAccountQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String CAMP_ID = "10001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void smsAccountGet_shouldReturnStoredSmsBalanceSummary() throws Exception {
        jdbcTemplate.update("""
                        INSERT INTO sms_channel_account (
                            sms_channel_account_id,
                            camp_id,
                            provider_code,
                            provider_name,
                            sign_name,
                            balance_num,
                            total_num,
                            status,
                            config_json
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                19001L,
                10001L,
                "aliyun",
                "阿里云短信",
                "路客云",
                50,
                100,
                1,
                null
        );

        mockMvc.perform(post("/smsAccount/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"%s"}
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.id").value("19001"))
                .andExpect(jsonPath("$.data.campId").value(CAMP_ID))
                .andExpect(jsonPath("$.data.totalSmsCount").value("100"))
                .andExpect(jsonPath("$.data.curSmsCount").value("50"));
    }

    @Test
    @Timeout(60)
    void smsAccountGet_shouldFallbackToCurrentCampAndReturnZeroSummaryWhenMissing() throws Exception {
        mockMvc.perform(post("/smsAccount/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":""}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.campId").value(CAMP_ID))
                .andExpect(jsonPath("$.data.totalSmsCount").value("0"))
                .andExpect(jsonPath("$.data.curSmsCount").value("0"));
    }
}
