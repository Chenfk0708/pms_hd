package com.jeez.zp.platform.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class DepositChannelQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Timeout(60)
    void calChannel4DepositGet_shouldReturnSupportedChannelCatalog() throws Exception {
        mockMvc.perform(post("/select/calChannel4Deposit/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.select.length()").value(16))
                .andExpect(jsonPath("$.data.select[0].channelId").value("1"))
                .andExpect(jsonPath("$.data.select[0].channelName").value("爱彼迎"))
                .andExpect(jsonPath("$.data.select[0].channelImageLogo").isNotEmpty())
                .andExpect(jsonPath("$.data.select[0].channelImageOpen").isNotEmpty())
                .andExpect(jsonPath("$.data.select[0].channelImageClose").isNotEmpty())
                .andExpect(jsonPath("$.data.select[0].isOpen").value(1))
                .andExpect(jsonPath("$.data.select[14].channelId").value("71"))
                .andExpect(jsonPath("$.data.select[14].channelName").value("同程民宿"))
                .andExpect(jsonPath("$.data.select[14].isOpen").value(0))
                .andExpect(jsonPath("$.data.select[15].channelId").value("86"))
                .andExpect(jsonPath("$.data.select[15].channelName").value("视频号"))
                .andExpect(jsonPath("$.data.select[15].isOpen").value(0));
    }

    @Test
    @Timeout(60)
    void calChannel4DepositGet_shouldFallbackCurrentCampWhenCampIdBlank() throws Exception {
        mockMvc.perform(post("/select/calChannel4Deposit/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":""}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.select.length()").value(16));
    }

    @Test
    @Timeout(60)
    void calChannel4DepositGet_shouldRejectForeignCampAccess() throws Exception {
        mockMvc.perform(post("/select/calChannel4Deposit/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10002"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
