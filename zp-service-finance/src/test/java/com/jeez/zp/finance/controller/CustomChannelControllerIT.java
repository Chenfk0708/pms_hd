package com.jeez.zp.finance.controller;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CustomChannelControllerIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final long CAMP_ID = 10001L;
    private static final long USER_ID = 12001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @Timeout(60)
    void customChannelList_shouldReturnSystemChannelsAndCustomChannelRowsFromRealTables() throws Exception {
        mockMvc.perform(post("/channels/custom/list")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.systemChannels.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.systemChannels[0].name").exists())
                .andExpect(jsonPath("$.data.systemChannels[*].name").value(hasItem("自来客")))
                .andExpect(jsonPath("$.data.systemChannels[*].name").value(hasItem("路客云聚合")))
                .andExpect(jsonPath("$.data.systemChannels[*].name").value(hasItem("Hotelbeds")))
                .andExpect(jsonPath("$.data.customChannels[?(@.id == '25501')].name").value(hasItem("企业协议客户")))
                .andExpect(jsonPath("$.data.customChannels[?(@.id == '25501')].enabled").value(hasItem(true)));
    }

    @Test
    @Timeout(60)
    void customChannelMutations_shouldCreateUpdateToggleAndDeleteRows() throws Exception {
        mockMvc.perform(post("/channels/custom/create")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "name":"TDD自定义渠道",
                                  "color":"#2563eb",
                                  "colorName":"晴空蓝"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.customChannels[?(@.name == 'TDD自定义渠道')].colorName").value(hasItem("晴空蓝")));

        Long customChannelId = jdbcTemplate.queryForObject(
                "SELECT custom_channel_id FROM custom_channel WHERE camp_id = ? AND channel_name = ? AND is_deleted = 0 LIMIT 1",
                Long.class,
                CAMP_ID,
                "TDD自定义渠道"
        );

        mockMvc.perform(post("/channels/custom/update")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "channelId":"%s",
                                  "name":"TDD自定义渠道升级",
                                  "color":"#14b8a6",
                                  "colorName":"松石绿"
                                }
                                """.formatted(customChannelId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.customChannels[?(@.id == '%s')].name".formatted(customChannelId)).value(hasItem("TDD自定义渠道升级")));

        mockMvc.perform(post("/channels/custom/update")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "channelId":"%s",
                                  "enabled":0
                                }
                                """.formatted(customChannelId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.customChannels[?(@.id == '%s')].enabled".formatted(customChannelId)).value(hasItem(false)));

        mockMvc.perform(post("/channels/custom/delete")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "channelId":"%s"
                                }
                                """.formatted(customChannelId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/channels/custom/list")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customChannels[?(@.name == 'TDD自定义渠道升级')]").isEmpty());
    }

    @Test
    @Timeout(60)
    void customChannelSystemSave_shouldPersistEnabledFlagsToSystemConfig() throws Exception {
        mockMvc.perform(post("/channels/custom/update")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, String.valueOf(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"10001",
                                  "systemChannels":[
                                    {"id":"system-001","enabled":false},
                                    {"id":"system-002","enabled":true}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.systemChannels[?(@.id == 'system-001')].enabled").value(hasItem(false)));

        String rawConfig = jdbcTemplate.queryForObject(
                "SELECT config_value FROM system_config WHERE camp_id = ? AND config_key = ? AND config_scope = 'camp' LIMIT 1",
                String.class,
                CAMP_ID,
                "hudson.channels.custom.systemChannelStates"
        );
        org.junit.jupiter.api.Assertions.assertNotNull(rawConfig);
        Map<?, ?> parsed = objectMapper.readValue(rawConfig, Map.class);
        org.junit.jupiter.api.Assertions.assertEquals(Boolean.FALSE, parsed.get("system-001"));
    }
}
