package com.jeez.zp.platform.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Timeout(60)
    void authMe_withoutGatewayHeaders_shouldReturn401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @Timeout(60)
    void login_withSeedMobile_shouldReturnToken() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"mobile":"13800000001","password":"demo-login"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.loginMode").value("demo-direct"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenName").value("Authorization"))
                .andExpect(jsonPath("$.data.userId").value(12001))
                .andExpect(jsonPath("$.data.memberId").value(14001))
                .andExpect(jsonPath("$.data.campId").value(10001))
                .andExpect(jsonPath("$.data.roleCode").value("admin"))
                .andExpect(jsonPath("$.data.roleName").value("系统管理员"));
    }

    @Test
    @Timeout(60)
    void login_withUnknownMobile_shouldReturnBusinessError() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"mobile":"13999999999","password":"demo-login"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.message").value("账号不存在"));
    }

    @Test
    @Timeout(60)
    void authMe_response_shouldContainTraceIdAndCurrentUserContext() throws Exception {
        mockMvc.perform(get("/auth/me")
                        .header("X-Auth-Verified", "true")
                        .header("X-User-Id", "12001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.data.userId").value(12001))
                .andExpect(jsonPath("$.data.memberId").value(14001))
                .andExpect(jsonPath("$.data.campId").value(10001))
                .andExpect(jsonPath("$.data.campName").value("路客云演示租户"))
                .andExpect(jsonPath("$.data.poiId").value(11001))
                .andExpect(jsonPath("$.data.poiName").value("路客云演示门店"))
                .andExpect(jsonPath("$.data.roleCode").value("admin"))
                .andExpect(jsonPath("$.data.roleName").value("系统管理员"))
                .andExpect(jsonPath("$.data.permissionCodes[0]").value("dashboard:view"));
    }

    @Test
    @Timeout(60)
    void logout_withValidToken_shouldClearTokenSession() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"mobile":"13800000001","password":"demo-login"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.at("/data/token").asText();

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Auth-Verified", "true")
                        .header("X-User-Id", "12001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        org.junit.jupiter.api.Assertions.assertNull(StpUtil.getLoginIdByToken(token));
    }
}
