package com.jeez.zp.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeez.zp.platform.entity.SystemConfig;
import com.jeez.zp.platform.mapper.SystemConfigMapper;
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
class SystemConfigQueryIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String CAMP_ID = "10001";
    private static final String CHECK_IN_GUIDE_SHOW_KEY = "hudson.basic.checkInGuideShowStrategy";
    private static final String CHECK_WIFI_SHOW_KEY = "hudson.basic.checkWifiShowStrategy";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Timeout(60)
    void checkInGuideShowStrategyGet_shouldReturnDefaultCampConfig() throws Exception {
        mockMvc.perform(post("/systemConfig/checkInGuideShowStrategy/get")
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
                .andExpect(jsonPath("$.data.configKey").value(CHECK_IN_GUIDE_SHOW_KEY))
                .andExpect(jsonPath("$.data.configValue").value("0"))
                .andExpect(jsonPath("$.data.configScope").value("camp"))
                .andExpect(jsonPath("$.data.source").value("system"));
    }

    @Test
    @Timeout(60)
    void checkInGuideShowStrategyGet_shouldReturnStoredCampConfig() throws Exception {
        SystemConfig config = new SystemConfig();
        config.setSystemConfigId(IdWorker.getId());
        config.setCampId(Long.valueOf(CAMP_ID));
        config.setConfigKey(CHECK_IN_GUIDE_SHOW_KEY);
        config.setConfigScope("camp");
        config.setConfigValue(objectMapper.writeValueAsString("1"));
        config.setValueType("json");
        config.setSource("platform");
        config.setUpdatedBy(12001L);
        systemConfigMapper.insert(config);

        mockMvc.perform(post("/systemConfig/checkInGuideShowStrategy/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"%s"}
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.configKey").value(CHECK_IN_GUIDE_SHOW_KEY))
                .andExpect(jsonPath("$.data.configValue").value("1"))
                .andExpect(jsonPath("$.data.configScope").value("camp"))
                .andExpect(jsonPath("$.data.source").value("platform"));
    }

    @Test
    @Timeout(60)
    void checkWifiShowStrategyGet_shouldReturnDefaultCampConfig() throws Exception {
        mockMvc.perform(post("/systemConfig/checkWifiShowStrategy/get")
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
                .andExpect(jsonPath("$.data.configKey").value(CHECK_WIFI_SHOW_KEY))
                .andExpect(jsonPath("$.data.configValue.isWifiDisplayEnabled").value(0))
                .andExpect(jsonPath("$.data.configScope").value("camp"))
                .andExpect(jsonPath("$.data.source").value("system"));
    }

    @Test
    @Timeout(60)
    void checkWifiShowStrategyGet_shouldReturnStoredCampConfig() throws Exception {
        SystemConfig config = new SystemConfig();
        config.setSystemConfigId(IdWorker.getId());
        config.setCampId(Long.valueOf(CAMP_ID));
        config.setConfigKey(CHECK_WIFI_SHOW_KEY);
        config.setConfigScope("camp");
        config.setConfigValue(objectMapper.writeValueAsString(objectMapper.readTree("""
                {"isWifiDisplayEnabled":1}
                """)));
        config.setValueType("json");
        config.setSource("platform");
        config.setUpdatedBy(12001L);
        systemConfigMapper.insert(config);

        mockMvc.perform(post("/systemConfig/checkWifiShowStrategy/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"%s"}
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.configKey").value(CHECK_WIFI_SHOW_KEY))
                .andExpect(jsonPath("$.data.configValue.isWifiDisplayEnabled").value(1))
                .andExpect(jsonPath("$.data.configScope").value("camp"))
                .andExpect(jsonPath("$.data.source").value("platform"));
    }
}
